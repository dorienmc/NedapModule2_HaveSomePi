package com.nedap.university.fileTranser;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ARQProtocol.Protocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dorien.meijercluwen on 12/04/2017.
 */
public class Receiver extends Thread {
  Handler handler;
  DatagramSocket socket;
  int destPort;
  int sourcePort;
  InetAddress address;
  boolean stop;
  private ConcurrentHashMap<Byte, Command> requests; //Requests/commands which follow a specific protocol

  public Receiver(InetAddress address, int sourcePort, int destPort, Handler handler) throws SocketException{
    this.socket = new DatagramSocket(sourcePort);
    this.requests = new ConcurrentHashMap<>();
    this.address = address;
    this.sourcePort = sourcePort;
    this.destPort = destPort;
    this.handler = handler;
  }

  @Override
  public void run() { //TODO stop when no packet has arrived for a long time?
    while(!stop) {
      //Receive packets from socket
      DatagramPacket response = new DatagramPacket(new byte[Protocol.MAX_BUFFER],Protocol.MAX_BUFFER);
      try {
        socket.receive(response);
      } catch (IOException e) {
        if(stop) { //Stopped by user.
          return;
        } else {
          handler.handleSocketException(String.format("Error in receiver: %s", e.getMessage()));
        }
      }

      //Try to parse received packet
      UDPPacket packet = null;
      try {
        packet = new UDPPacket(response);
      } catch (ArrayIndexOutOfBoundsException e) {
        //Drop packet.
        //TODO list ignored packets in statistics.
        continue;
      }

      //Check if packet is valid (eg meant for this host, correct checksum and such)
      //Note: a received packet comes from the 'destPort' and is received in the 'sourcePort' of this socket
      if(!packet.isValid(destPort,sourcePort)) {
        System.out.println("Warning packet not valid!");
        //Drop packet.
        //TODO list ignored packets in statistics.
        //continue;
      }

      //Now send packet to correct Request, or let handler create a new request
      sendPacketToRequest(packet);

      //Wait
      Utils.sleep(10);
    }
  }


  /********* DEMUX received packets **********/
  public void sendPacketToRequest(UDPPacket packet) {
    //Get requestId
    byte requestId = (byte) packet.getRequestId();
    handler.print("Received packet for request " + requestId + " with sequence number " + packet
        .getSequenceNumber());

    //Determine to which request the packet belongs
    Command request = requests.get(new Byte(requestId));

    if (request == null) {
      //Get requestType from flag
      Keyword requestType = Keyword.fromFlags(packet.getFlags());

      //Create a new one
      if (requestType != null) {
        Command command = handler.startNewCommand(requestType, requestId);
        command.addPacketToReceiveBuffer(packet);
      } else {
        handler.print("Could not parse request type from flags: " + packet.getFlags());
      }
      return;
    } else {
      request.addPacketToReceiveBuffer(packet);
    }

  }

  public void shutdown() {
    stop = true;
    socket.close();
  }

  public void register(Command request) {
    requests.put(request.getRequestId(), request);
  }

  public void deregister(Command request) {
    System.out.println("Deregister " + requests.remove(request.getRequestId()) + " from receiver.");
  }

  /**
   * Log running commands.
   */
  public void log() {
    //TODO
    System.out.println("TODO log running commands in Receiver");
  }


}
