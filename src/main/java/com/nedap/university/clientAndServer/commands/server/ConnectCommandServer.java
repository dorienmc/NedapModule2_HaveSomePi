package com.nedap.university.clientAndServer.commands.server;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ARQProtocol.ProtocolFactory;
import com.nedap.university.fileTranser.ARQProtocol.ProtocolFactory.Name;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.clientAndServer.commands.helpers.MDNSdata;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ConnectCommandServer extends Command {
  DatagramPacket connectPacket;
  public static final String SERVER_ADDRESS = "192.168.40.8";

  public ConnectCommandServer(DatagramPacket connectPacket, Handler handler, Byte requestId) {
    super(Keyword.CONNECT, "Establish connection with client", handler, requestId);
    this.connectPacket = connectPacket;
  }

  @Override
  public void execute() {
    //Try to create Reliable UDP channel
    createReliableUDPchannel(handler);

    //If the RUDP is setup, create an mDNS message (portIn + portOut) for the client
    if(handler.getChannel() != null) {
      registerToChannel(Name.DEFAULT);

      //Wait shortly so protocol is registered and until client has started listening to correct channel.
      Utils.sleep(500);


      MDNSdata mDNSdata = new MDNSdata(handler.getInPort(), handler.getOutPort(), "Client");
      handler.printDebug("My mDNS data " + mDNSdata);
      protocol.sendRequest(mDNSdata.getData(), Flag.CONNECT, false);


    } else {
      handler.shutdown();
    }

    //Wait until protocol is not busy anymore, then deregister the connect command.
    shutdown(handler.getChannel() != null);
  }

  private void createReliableUDPchannel(Handler handler){
    //Retrieve address and portIn/portOut of client
    UDPPacket udpPacket = new UDPPacket(connectPacket);
    handler.printDebug("Create reliable UDP channel from" + udpPacket);

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

    //Create Reliable Udp channel
    try {
      handler.setChannel(handler.getInPort(), handler.getOutPort(), address, clientPortIn, clientPortOut);

    } catch (SocketException e) {
      handler.print(String.format("Could not create connection to %s on ports %d and %d, %s",address, clientPortIn, clientPortOut, e.getMessage()));
    }

  }



}
