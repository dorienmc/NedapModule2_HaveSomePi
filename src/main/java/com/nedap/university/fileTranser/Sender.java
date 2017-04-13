package com.nedap.university.fileTranser;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by dorien.meijercluwen on 12/04/2017.
 */
public class Sender extends Thread {
  DatagramSocket socket;
  int destPort;
  int sourcePort;
  InetAddress address;
  private ConcurrentLinkedDeque<UDPPacket> sendBuffer; //Packets that still need to be send
  private ConcurrentHashMap<Integer,UDPPacket> unAckedPackets; //Packets that are still unacked.
  boolean isBlocked;
  boolean stop;
  int currentSeqNumber; //Sequence number of last packet that was send.

  /*
  * Create new sender, starts blocked
   */
  public Sender(InetAddress address, int sourcePort, int destPort) throws SocketException {
    this.socket = new DatagramSocket(sourcePort);
    this.sendBuffer = new ConcurrentLinkedDeque<>();
    this.unAckedPackets = new ConcurrentHashMap<>();
    this.isBlocked = true;
    this.stop = false;
    this.address = address;
    this.destPort = destPort;
    this.sourcePort = sourcePort;
    this.currentSeqNumber = 0;
    System.out.println(socket.getLocalPort());
  }

  public int getCurrentSeqNumber() {
    return currentSeqNumber;
  }

  public int getSourcePort() {
    return sourcePort;
  }

  public int getDestPort() {
    return destPort;
  }

  public void addPacketToBuffer(UDPPacket packet) { //TODO add sequence number here?
    sendBuffer.add(packet);
  }

  public void blockSender() {
    isBlocked = true;
  }

  public void unBlockSender() {
    isBlocked = false;
  }

  @Override
  public void run() {

    while(!stop) {
      //Send packets when there are packets in the queue and the sender is not blocked
      if(!isBlocked) {
        //Send packet from buffer
        try {
          sendPacket(sendBuffer.pollFirst());
        } catch (IOException e) {
          System.out.println("WARNING could not send packet with seq. number" + currentSeqNumber);
          Thread.currentThread().interrupt();
        }
      }

      //Wait
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void sendPacket(UDPPacket packet) throws IOException {
    if (packet != null) {
      System.out.println("Try to send packet to " + address + ":" + packet.getDestPort());

      //Update current seq number
      currentSeqNumber = packet.getSequenceNumber();

      //Add to unacked (and (re)set timeout?) //TODO reset timeout?
      unAckedPackets.put(new Integer(packet.getSequenceNumber()), packet);

      //Try to send
      socket.send(packet.toDatagram(address));
    }
  }

  public void forceSend(UDPPacket packet) {
    try {
      sendPacket(packet);
    } catch (IOException e) {
      System.out.println("Warning, could not force send packet " + packet);
    }
  }

  public void shutdown() {
    stop = true;
    socket.close();
  }



}
