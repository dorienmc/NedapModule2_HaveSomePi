package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MDNSdata;
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
  public static final String SERVER_ADDRESS = "192.168.40.8";

  public ConnectCommandServer(DatagramPacket connectPacket) {
    super(Keyword.CONNECT, "Establish connection with client");
    this.connectPacket = connectPacket;
  }

  @Override
  public void execute(Handler handler) {
    //Try to create Reliable UDP channel
    createReliableUDPchannel(handler);

    //If the RUDP is setup, create an mDNS message (portIn + portOut) for the client
    if(handler != null) {
      try {
        MDNSdata mdnSdata = new MDNSdata(handler.getInPort(),
            handler.getOutPort(), "Client");
        handler.print("mDNS data " + mdnSdata);
        handler.getChannel().sendAndReceive(mdnSdata.getData(), Flag.CONNECT);
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

    InetAddress address = connectPacket.getAddress();
    handler.setAddress(address);
    int clientPortIn = 0, clientPortOut = 0;

    try {
      MDNSdata mdnSdata = new MDNSdata(udpPacket.getData());
      clientPortIn = mdnSdata.getInPort();
      clientPortOut = mdnSdata.getOutPort();
    } catch (IndexOutOfBoundsException e) {
      handler.print(e.getMessage());
      return;
    }

    //Create sockets
    try {
      DatagramSocket socketIn = new DatagramSocket(handler.getInPort());
      DatagramSocket socketOut = new DatagramSocket(handler.getOutPort());

      //Create Reliable Udp channel
      handler.setChannel(socketIn, socketOut, address, clientPortIn, clientPortOut, false);

    } catch (SocketException e) {
      handler.print(String.format("Could not create connection to %s on ports %d and %d, %s",address, clientPortIn, clientPortOut, e.getMessage()));
    }

  }



}