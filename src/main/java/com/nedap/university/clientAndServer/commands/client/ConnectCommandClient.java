package com.nedap.university.clientAndServer.commands.client;

import static com.nedap.university.clientAndServer.commands.helpers.ConnectionHelper.isConnectionPacket;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ARQProtocol.ProtocolFactory.Name;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.clientAndServer.commands.helpers.MDNSdata;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ConnectCommandClient extends Command {
  String hostName;
  private InetAddress broadcastAddress;
  DatagramSocket socketIn;

  public ConnectCommandClient(Handler handler, Byte requestId){
    super(Keyword.CONNECT, "(Re)connect to PiServer", handler, requestId);
  }

  @Override
  public void execute() {
    //Clear current RUDP channel
    handler.removeChannel();

    //Ask for hostname
    this.hostName = Utils.readString("To which Pi do you want to connect? ");

    //Create broadcast channel and send mDNS request.
    if(!sendBroadCast(handler)) {
      return;
    }

    DatagramPacket response = getResponse(handler);
    socketIn.close();
    if(response == null) {
      handler.removeChannel();
      return;
    }

    //Create Reliable UDP channel
    if(createReliableUDPchannel(response, handler)){
      //Register to it.
      registerToChannel(Name.DEFAULT);

      //Tell server we have connected
      UDPPacket lastPacket = protocol.createEmptyPacket();
      lastPacket.setFlags(Flag.LAST.getValue());
      protocol.addPacketToSendBuffer(lastPacket);
      addPacketToReceiveBuffer(new UDPPacket(response), true); //To let protocol know that this was the first packet

    }

    shutdown();
  }

  public String getHostName() {
    return hostName;
  }

  private boolean sendBroadCast(Handler handler) {
    //Create DatagramSocket for multicast, with a time out of 10 sec. (10000ms)
    MulticastSocket socket = null;
    try {
      socket = new MulticastSocket(Server.MULTI_DNS_PORT);
      broadcastAddress = InetAddress.getByName("192.168.40.255");
      socket.setSoTimeout(3000);
    } catch (IOException e) {
      handler.print(String.format("Could not create socket on %s:%d, %s",
          broadcastAddress,Server.MULTI_DNS_PORT));
      socket.close();
      return false;
    }

    //Request free port(s)
    int port = Utils.getFreePort(Client.FIRST_CLIENT_UDP_PORT);
    handler.setInPort(port);
    handler.setOutPort(port + 1);

    //Shout out over multicast
    UDPPacket multiShout = new UDPPacket(socket.getLocalPort(), Server.MULTI_DNS_PORT,0, 0);
    multiShout.setFlags(Flag.CONNECT.getValue());
    MDNSdata mdnSdata = new MDNSdata(handler.getInPort(), handler.getOutPort(), hostName);
    multiShout.setData(mdnSdata.getData());
    handler.print("Trying to connect to host " + hostName);
    handler.print("mDNS data: " + mdnSdata);

    try {
      DatagramPacket packet = multiShout.toDatagram(broadcastAddress);
      packet.setPort(Server.MULTI_DNS_PORT);
      packet.setAddress(broadcastAddress);
      socket.send(packet);
    } catch (SocketTimeoutException e) {
      handler.print("Socket TimeOut: Could not find host.");
      socket.close();
      return false;
    } catch (IOException e) {
      handler.print("IO Expection: Could not find host.");
      socket.close();
      return false;
    }

    return true;
  }

  private DatagramPacket getResponse(Handler handler) {
    //Create socket for receiving data.
    try {
      socketIn = new DatagramSocket(handler.getInPort());
      socketIn.setSoTimeout(10000);
    } catch (SocketException e) {
      handler.print(e.getMessage());
    }

    //Wait for response from server
    handler.print("Waiting for response from server on port " + socketIn.getLocalPort());
    DatagramPacket response = new DatagramPacket(new byte[UDPPacket.MAX_PACKET_SIZE], UDPPacket.MAX_PACKET_SIZE);

    try {
      socketIn.receive(response);
    } catch (SocketTimeoutException e) {
      handler.print(String.format("Time out exceeded"));
      return null;
    } catch (IOException e) {
      handler.print(String.format("Could not create connection to %s %s",hostName, e.getMessage()));
      return null;
    }

    //Check if response packet is an mDSN packet
    if(isConnectionPacket(response,handler)) {
      return response;
    }

    return null;
  }

  private boolean createReliableUDPchannel(DatagramPacket response, Handler handler){
    //Retrieve address and portIn/portOut of server
    UDPPacket input = new UDPPacket(response);
    int serverPortIn = 0;
    int serverPortOut = 0;

    InetAddress address = response.getAddress();
    handler.setAddress(address);

    try {
      MDNSdata mdnSdata = new MDNSdata(input.getData());
      serverPortIn = mdnSdata.getInPort();
      serverPortOut = mdnSdata.getOutPort();

    } catch (IndexOutOfBoundsException e) {
      handler.print(e.getMessage());
      return false;
    }

    //Create Reliable Udp channel
    try {
      handler.setChannel(handler.getInPort(), handler.getOutPort(), address,serverPortIn, serverPortOut);
      return true;
    } catch (SocketException e) {
      handler.print(String.format("Could not create connection to %s: %s",hostName, e.getMessage()));
      return false;
    }
  }


}
