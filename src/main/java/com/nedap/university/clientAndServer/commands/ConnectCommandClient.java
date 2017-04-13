package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MDNSdata;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import com.nedap.university.fileTranser.UDPPacket;
import com.nedap.university.fileTranser.ARQProtocol.Protocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ConnectCommandClient extends Command {
  String hostName;
  private InetAddress broadcastAddress;
  DatagramSocket socketIn;

  public ConnectCommandClient(String hostName){
    super(Keyword.CONNECT, "Connect to PiServer");
    this.hostName = hostName;
  }

  @Override
  public void execute(Handler handler) {
    //TODO use ReliableChannel?
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
    createReliableUDPchannel(response, handler); //TODO what if we got an exception?
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

    //Shout out over multicast
    UDPPacket multiShout = new UDPPacket(socket.getLocalPort(), Server.MULTI_DNS_PORT,0, 0);
    multiShout.setFlags(Flag.CONNECT.getValue());
    MDNSdata mdnSdata = new MDNSdata(handler.getInPort(), handler.getOutPort(), hostName);
    multiShout.setData(mdnSdata.getData());
    handler.print("Trying to connect to host " + hostName);
    handler.print("Send mDNS packet " + multiShout);
    handler.print("mDNS data " + mdnSdata);

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
      socketIn.setSoTimeout(5000);
    } catch (SocketException e) {
      e.printStackTrace();
    }

    //Wait for response from server
    System.out.println("Waiting for response from server on port " + handler.getInPort());
    DatagramPacket response = new DatagramPacket(new byte[Protocol.MAX_BUFFER],Protocol.MAX_BUFFER);

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
    System.out.println("Response: " + new UDPPacket(response));
    if(isConnectionPacket(response,handler)) {
      return response;
    }

    return null;
  }

  private boolean isConnectionPacket(DatagramPacket packet, Handler handler) {
    UDPPacket udpPacket;
    try {
      udpPacket = new UDPPacket(packet);
    } catch (ArrayIndexOutOfBoundsException e) {
      handler.print(e.getMessage());
      return false;
    }

    //Has connection flag set
    if(!udpPacket.isFlagSet(Flag.CONNECT)) {
      return false;
    }

    //Is asking for me
    try {
      MDNSdata mdnSdata = new MDNSdata(udpPacket.getData());
      if(mdnSdata.getHostname() != null && mdnSdata.getHostname().equals("Client")) {
        return true;
      }
    } catch (IndexOutOfBoundsException e) {
      handler.print(e.getMessage());
      return false;
    }

    return false;
  }

  private void createReliableUDPchannel(DatagramPacket response, Handler handler){
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
      return;
    }

    //Create Reliable Udp channel
    try {
      handler.setChannel(handler.getInPort(), handler.getOutPort(), address,serverPortIn, serverPortOut, true);

    } catch (SocketException e) {
      handler.print(String.format("Could not create connection to %s: %s",hostName, e.getMessage()));
    }
  }



}
