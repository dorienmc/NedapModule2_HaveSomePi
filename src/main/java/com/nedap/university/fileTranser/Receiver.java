package com.nedap.university.fileTranser;

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
  int portIn;
  int portOut;
  InetAddress address;
  private ConcurrentLinkedDeque<UDPPacket> receiveBuffer; //Packets that have arrived
  boolean isBlocked;
  boolean stop;
  int currentAckNumber; //Sequence number of next expected packet from receiver.

  public Receiver(InetAddress address, int portIn, int portOut) throws SocketException{
    this.socket = new DatagramSocket(portIn);
    this.receiveBuffer = new ConcurrentLinkedDeque<>();
    this.isBlocked = false;
    this.stop = false;
    this.currentAckNumber = 0;
    this.address = address;
    this.portIn = portIn;
    this.portOut = portOut;
  }

  public int getCurrentAckNumber() {
    return currentAckNumber;
  }

  public int getPortIn() {
    return portIn;
  }

  public int getPortOut() {
    return portOut;
  }

  /* Retrieve next packet from the receiveBuffer, null there are none.
      * Note: also removes this packet from the buffer! */
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

        UDPPacket packet = new UDPPacket(response);
        System.out.println("Received new packet " + packet);
        currentAckNumber = packet.getAckNumber();
        receiveBuffer.add(packet);

        //TODO handle acks?
      }

      //Wait
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public void shutdown() {
    stop = true;
    socket.close();
  }
}
