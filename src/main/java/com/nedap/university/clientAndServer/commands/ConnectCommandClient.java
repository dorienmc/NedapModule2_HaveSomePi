package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.fileTranser.Flag;
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

  public ConnectCommandClient(String hostName){
    super(Keyword.CONNECT, "Connect to PiServer");
    this.hostName = hostName;
  }

  @Override
  public void execute(Handler handler) { //TODO use ReliableChannel?
    //Create DatagramSocket for multicast, with a time out of 10 sec. (10000ms)
    MulticastSocket socket = null;
    try {
      socket = new MulticastSocket(Server.MULTI_DNS_PORT);
      broadcastAddress = InetAddress.getByName("192.168.40.255");
      socket.setSoTimeout(10000);
    } catch (IOException e) {
      handler.print(String.format("Could not create socket on %s:%d, %s",
          broadcastAddress,Server.MULTI_DNS_PORT));
      return;
    }

    //Shout out over multicast
    UDPPacket multiShout = new UDPPacket(socket.getLocalPort(), Server.MULTI_DNS_PORT,0, 0);
    multiShout.setFlags(Flag.CONNECT.getValue());
    multiShout.setData(createMDNSData());
    handler.print("Trying to connect to host " + hostName);
    handler.print("Send mDNS packet " + multiShout);

    try {
      DatagramPacket packet = multiShout.toDatagram(broadcastAddress);
      packet.setPort(Server.MULTI_DNS_PORT);
      packet.setAddress(broadcastAddress);
      socket.send(packet);
    } catch (SocketTimeoutException e) {
      handler.print("Socket TimeOut: Could not find host.");
      socket.close();
      return;
    } catch (IOException e) {
      handler.print("IO Expection: Could not find host.");
      socket.close();
      return;
    }

    //Wait for response (timeout?)
    DatagramPacket response = new DatagramPacket(new byte[Protocol.MAX_BUFFER],Protocol.MAX_BUFFER);
    DatagramSocket socketIn = null;
    try {
      socketIn = new DatagramSocket(ReliableUdpChannel.DEFAULT_PORT_IN);
    } catch (SocketException e) {
      e.printStackTrace();
    }

    System.out.println("Waiting for response from server on port " + socketIn.getLocalPort());

    try {
      socketIn.receive(response);

    } catch (IOException e) {
      handler.print(String.format("Could not create connection to %s %s",hostName, e.getMessage()));
      socket.close();
      return;
    }

    System.out.println("Response: " + new UDPPacket(response));

    //Create Reliable UDP channel
    createReliableUDPchannel(response, handler); //TODO what if we got an exception?

    //Close 'broadcast' channel
    socket.close();
  }

  private byte[] createMDNSData() {
    ByteBuffer mDNSData = ByteBuffer.allocate(2 * 4);
    mDNSData.putInt(ReliableUdpChannel.DEFAULT_PORT_IN);
    mDNSData.putInt(ReliableUdpChannel.DEFAULT_PORT_OUT);
    return mDNSData.array();
  }

  private void createReliableUDPchannel(DatagramPacket response, Handler handler){
    //Retrieve address and portIn/portOut of server
    int serverPortIn = 0;
    int serverPortOut = 0;
    UDPPacket input = new UDPPacket(response);
    ByteBuffer buffer = ByteBuffer.allocate(input.getData().length).put(input.getData());

    InetAddress address = response.getAddress();
    try {
      serverPortIn = buffer.getInt(0);
      serverPortOut = buffer.getInt(4);
      System.out.println(serverPortIn + " " + serverPortOut);

    } catch (IndexOutOfBoundsException e) {
      handler.print("Could not parse " + input.getData() + " to mDNS data.");
      return;
    }

    //Create sockets
    try {
      DatagramSocket socketIn = new DatagramSocket();
      DatagramSocket socketOut = new DatagramSocket();

      //Create Reliable Udp channel
      handler.setChannel(socketIn, socketOut, address,serverPortIn, serverPortOut, true);

    } catch (SocketException e) {
      handler.print(String.format("Could not create connection to %s: %s",hostName, e.getMessage()));
    }
  }



}
