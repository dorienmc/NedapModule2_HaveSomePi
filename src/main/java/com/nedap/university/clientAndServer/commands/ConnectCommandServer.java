package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ConnectCommandServer extends Command{
  DatagramPacket connectPacket;
  int inPort;
  int outPort;
  public static final String SERVER_ADDRESS = "192.168.40.8";

  public ConnectCommandServer(DatagramPacket connectPacket, int inPort, int outPort) {
    super(Keyword.CONNECT, "Establish connection with client");
    this.connectPacket = connectPacket;
    this.inPort = inPort;
    this.outPort = outPort;
  }

  @Override
  public void execute(Handler handler) {
    //Try to create Reliable UDP channel
    createReliableUDPchannel(handler);

    //If the RUDP is setup, create an mDNS message (portIn + portOut) for the client
    if(handler != null) {
      try {
        handler.getChannel().sendAndReceive(createMDNSData(handler));
      } catch (IOException e) {
        handler.print(e.getMessage());
        handler.shutdown();
      }
    } else {
      handler.shutdown();
    }
  }

  private void createReliableUDPchannel(Handler handler){
    //Retrieve address and portIn/portOut of client
    UDPPacket udpPacket = new UDPPacket(connectPacket);
    System.out.println("Create reliable UDP channel from" + udpPacket);

    ByteBuffer buffer = ByteBuffer.allocate(udpPacket.getData().length).put(udpPacket.getData());
    InetAddress address = connectPacket.getAddress();
    int clientPortIn = 0, clientPortOut = 0;

    try {
      clientPortIn = buffer.getInt(0);
      clientPortOut = buffer.getInt(4);
    } catch (IndexOutOfBoundsException e) {
      handler.print("Could not parse " + udpPacket.getData() + " to mDNS data.");
      return;
    }

    //Create sockets
    try {
      DatagramSocket socketIn = new DatagramSocket(inPort);
      DatagramSocket socketOut = new DatagramSocket(outPort);

      //Create Reliable Udp channel
      handler.setChannel(socketIn, socketOut, address, clientPortIn, clientPortOut);

    } catch (SocketException e) {
      handler.print(String.format("Could not create connection to %s on ports %d and %d, %s",address, clientPortIn, clientPortOut, e.getMessage()));
    }

  }

  private byte[] createMDNSData(Handler handler) {
    ByteBuffer mDNSData = ByteBuffer.allocate(2 * 4);
    mDNSData.putInt(handler.getChannel().getReceivePort());
    mDNSData.putInt(handler.getChannel().getSendPort());
    return mDNSData.array();
  }



}
