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
      socket.setSoTimeout(3000);
    } catch (IOException e) {
      handler.print(String.format("Could not create socket on %s:%d, %s",
          broadcastAddress,Server.MULTI_DNS_PORT));
      return;
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
      return;
    } catch (IOException e) {
      handler.print("IO Expection: Could not find host.");
      socket.close();
      return;
    }

    //Create socket for receiving data.
    DatagramSocket socketIn = null;
    try {
      socketIn = new DatagramSocket(handler.getInPort());
      socketIn.setSoTimeout(5000);
    } catch (SocketException e) {
      e.printStackTrace();
    }

    //Wait for response from server
    System.out.println("Waiting for response from server on port " + socketIn.getLocalPort());
    DatagramPacket response = new DatagramPacket(new byte[Protocol.MAX_BUFFER],Protocol.MAX_BUFFER);

    try {
      socketIn.receive(response);
    } catch (SocketTimeoutException e) {
      handler.print(String.format("Time out exceeded"));
      socketIn.close();
      socket.close();
      return;
    } catch (IOException e) {
      handler.print(String.format("Could not create connection to %s %s",hostName, e.getMessage()));
      socketIn.close();
      socket.close();
      return;
    }

    //Check if response packet is an mDSN packet
    //TODO

    System.out.println("Response: " + new UDPPacket(response));

    //Create Reliable UDP channel
    createReliableUDPchannel(response, handler, socketIn); //TODO what if we got an exception?

    //Close 'broadcast' channel
    socket.close();
  }

  private void createReliableUDPchannel(DatagramPacket response, Handler handler, DatagramSocket socketIn){
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

    //Create sockets
    try {
      DatagramSocket socketOut = new DatagramSocket(handler.getOutPort());
      socketIn.setSoTimeout(0); //Set time out to inf. again

      //Create Reliable Udp channel
      handler.setChannel(socketIn, socketOut, address,serverPortIn, serverPortOut, true);

    } catch (SocketException e) {
      handler.print(String.format("Could not create connection to %s: %s",hostName, e.getMessage()));
    }
  }



}
