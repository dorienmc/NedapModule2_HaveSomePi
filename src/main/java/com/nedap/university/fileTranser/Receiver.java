package com.nedap.university.fileTranser;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
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
  public void run() {
    while(!stop) {
      //Receive packets from socket
      DatagramPacket response = new DatagramPacket(new byte[UDPPacket.MAX_PACKET_SIZE], UDPPacket.MAX_PACKET_SIZE);
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
        handler.getStatistics().logUnparseableReceivedPacket();
        continue;
      }

      //Check if packet is valid (eg meant for this host, correct checksum and such)
      //Note: a received packet comes from the 'destPort' and is received in the 'sourcePort' of this socket
      if(!packet.isValid(destPort,sourcePort)) {
        handler.printDebug("Error: packet not valid!" + packet);
        //Drop packet.
        handler.getStatistics().logInvalidPacket();
        continue;
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

    //Determine to which request the packet belongs
    Command request = requests.get(new Byte(requestId));

    if (request == null) {
      //Get requestType from flag
      Keyword requestType = Keyword.fromFlags(packet.getFlags());

      //Create a new one
      if (requestType != null) {
        handler.print("Received an " + requestType + " request.");
        Command command = handler.startNewCommand(requestType, requestId);
        command.addPacketToReceiveBuffer(packet, true);
      } else {
        //Drop packet.
        handler.getStatistics().logUnrecognizedRequest();
      }
      return;
    } else {
      request.addPacketToReceiveBuffer(packet, false);
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
    requests.remove(request.getRequestId());
  }


}
