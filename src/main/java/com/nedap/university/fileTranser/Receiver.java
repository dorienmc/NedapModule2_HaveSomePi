package com.nedap.university.fileTranser;

import com.nedap.university.Utils;
import com.nedap.university.fileTranser.ARQProtocol.Protocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by dorien.meijercluwen on 12/04/2017.
 */
public class Receiver extends Thread {
  DatagramSocket socket;
  int destPort;
  int sourcePort;
  InetAddress address;
  private ConcurrentLinkedDeque<UDPPacket> receiveBuffer; //Packets that have arrived
  boolean isBlocked;
  boolean stop;
  int currentAckNumber; //Sequence number of next expected packet from receiver.

  public Receiver(InetAddress address, int sourcePort, int destPort) throws SocketException{
    this.socket = new DatagramSocket(sourcePort);
    this.receiveBuffer = new ConcurrentLinkedDeque<>();
    this.isBlocked = false;
    this.stop = false;
    this.currentAckNumber = 0;
    this.address = address;
    this.sourcePort = sourcePort;
    this.destPort = destPort;
  }

  public int getCurrentAckNumber() {
    return currentAckNumber;
  }

  public int getDestPort() {
    return destPort;
  }

  public int getSourcePort() {
    return sourcePort;
  }

  /**
  * Retrieve next packet from the receiveBuffer, null there are none.
  * Note: also removes this packet from the buffer!
  **/
  public UDPPacket retrievePacket() {
    return receiveBuffer.pollFirst();
  }

  public void blockReceiver() { //TODO needed?
    isBlocked = true;
  }

  public void unBlockReceiver() {
    isBlocked = false;
  }

  @Override
  public void run() {

    while(!stop) {
      //Receive packets from socket when not blocked
      if(!isBlocked) {
        DatagramPacket response = new DatagramPacket(new byte[Protocol.MAX_BUFFER],Protocol.MAX_BUFFER);
        try {
          socket.receive(response);
        } catch (IOException e) {
          System.out.println(String.format("Error in receiver: %s",e.getMessage()));
          continue;
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

        System.out.println("Received new packet " + packet);
        currentAckNumber = packet.getAckNumber();
        receiveBuffer.add(packet);
        //TODO handle acks? (Remove timer of corresponding send packet)
      }

      //Wait
      Utils.sleep(10);
    }
  }

  public void shutdown() {
    stop = true;
    socket.close();
  }
}
