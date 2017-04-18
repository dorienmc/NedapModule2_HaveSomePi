package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.Utils;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.Sender;
import com.nedap.university.fileTranser.UDPPacket;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by dorien.meijercluwen on 18/04/2017.
 */
public class SlidingWindowProtocol extends Protocol {
  public static final int TIMEOUT = 5000; //ms
  private static final int sendWindowSize = 10;
  private static final int receiveWindowSize = 10;

  TimeOutHandler timeOutHandler;
  private volatile int lastAckRec;    //All packets below this sequence number have been acked.
  private volatile int lastPacketRec; //All packets (of the other host) with this sequence number or smaller have been received.
  private volatile int lastPacketSend;//Sequence number of the last send packet, all packets with this sequence number or smaller have been send at least once.
  ConcurrentLinkedDeque<Integer> receivedFrames; //Place to store the seq.number of received frames for when the arrive out of order.

  public SlidingWindowProtocol(Sender sender, Receiver receiver, byte requestId) {
    super(sender, receiver, requestId, TIMEOUT);
    timeOutHandler = new TimeOutHandler(this, TIMEOUT);
    this.lastAckRec = 0;
    this.lastPacketRec = -1;
    this.lastPacketSend = -1;
    this.receivedFrames = new ConcurrentLinkedDeque<>();
  }

  @Override
  public UDPPacket getNextPacket() {
    UDPPacket packet = null;
    if(status.equals(Status.PAUSED)) {
      return null;
    }

    //Timed out packets get priority
    while(resendBuffer.size() > 0) {
      packet = resendBuffer.pollFirst();
      //Drop timed out packet if its not in the start window
      if(!isInSendWindow(packet.getSequenceNumber())) {
        packet = null;
      } else {
        break;
      }
    }

    if(packet == null && sendBuffer.size() > 0) {
      //Peek at packet and check if we are already allowed to start it
      UDPPacket peekedPacket = sendBuffer.peekFirst();
      if(isInSendWindow(peekedPacket.getSequenceNumber())) {
        packet = sendBuffer.pollFirst();
        lastPacketSend = packet.getSequenceNumber();
      }
    }

    //If there is a packet to send, set its time out timer and ack number
    if(packet != null) {
      //Set timer, except for last packet
      if(!packet.isFlagSet(Flag.LAST)) {
        timeOutHandler.addTimer(packet);
      } else {
        System.out.println("Last packet, so start stopping.");
        super.setStatus(Status.STOPPING);
      }

      //The next expected packet is lastPacketReceived + 1
      packet.setHeaderSetting(HeaderField.ACK_NUMBER, lastPacketRec + 1);

      System.out.println(String.format("Send packet (seq: %d, ack %d)",
          packet.getSequenceNumber(),packet.getAckNumber()));
    }

    return packet;
  }

  @Override
  public boolean busy() {
    return !getStatus().equals(Status.STOPPING);
  }

  @Override
  public void run() {
    timeOutHandler.start();
    setStatus(Status.RUNNING);
    System.out.println("Start sending/receiving.");

    while(busy()) {
      Utils.sleep(100);
    }

    if(getStatus().equals(Status.STOPPING)) {
      //Wait for 1/2 TIMEOUT, if we are still stopping then, then stop.
      Utils.sleep(TIMEOUT/2);
    }

    System.out.println("Done sending/receiving.");
    timeOutHandler.stopHandler();
  }

  /**
   * A packet lies within the send window if its sequence number is at least
   * LastAckRec (the next packet expected by the other side) and at most
   * LastAckRec + sendWindowSize. This makes sure that at most sendWindowSize packets
   * of this host are in the pipe.
   **/
  public boolean isInSendWindow(int seq) {
    return lastAckRec <= seq && seq < lastAckRec + sendWindowSize;
  }

  /**
   * A packet lies within the send window if its sequence number bigger than
   * LastPacketRec (the last packet received from the other side) and less than
   * LastPacketRec + receiveWindowSize. This makes sure that at most receiveWindowSize packets
   * of the other host are in the pipe.
   * */
  public boolean isInRecWindow(int seq) {
    return lastPacketRec < seq && seq <= lastPacketRec + receiveWindowSize;
  }

  /**
   * Update LastAckReceived (all packets below this sequence number have been acked).
   */
  public synchronized void updateLAR(int ack) {
    if(ack > lastAckRec) {
      lastAckRec = ack;
      System.out.println("Updated lastAckRec to " + lastAckRec);
    }
  }

  /**
   * Update LastPacketReceveived (All packets (of the other host) with this
   * sequence number or smaller have been received.)
   */
  public synchronized void updateLPR(int seq) {
    if(!receivedFrames.isEmpty()) {
      while(receivedFrames.contains(lastPacketRec + 1)) {
        receivedFrames.remove(lastPacketRec + 1);
        lastPacketRec++;
      }
      System.out.println("Updated LPR to " + lastPacketRec);
    }
  }

  @Override
  public boolean isExpected(UDPPacket packet) {
    //Print debug info
    System.out.println(String.format("Received new packet (seq: %d, ack %d)",
        packet.getSequenceNumber(),packet.getAckNumber()));
    System.out.println(getInfo());

    //Check if packet lies in receiving window
    if(isInRecWindow(packet.getSequenceNumber())) {
      //Update the last packet received and last ack received
      receivedFrames.add(packet.getSequenceNumber());
      updateLPR(packet.getSequenceNumber());
      updateLAR(packet.getAckNumber());

      //Stop corresponding timer
      //Note: ACK tells the next packet that is expected, so the previous packet is acked.
      timeOutHandler.stopTimer(packet.getAckNumber() - 1);

      //Note: Add to receive buffer is done by method that calls isExpected()

      //If this was the last packet, go to stopping state
      if(packet.isFlagSet(Flag.LAST)) {
        System.out.println("Receiver: Last packet, so start stopping.");
        setStatus(Status.STOPPING);
      }

      System.out.println("Status: " + getInfo());

      return true;
    }

    return false;
  }

  /** Give status info that the Handler can than represent to the user */
  @Override
  public String getInfo() {
    if(getStatus().equals(Status.PAUSED)) {
      return Status.PAUSED.toString();
    } else {
      return String.format("Send window: [%d, %d), Receive window: (%d, %d], Last packet send: %d",
          lastAckRec, lastAckRec + sendWindowSize,
          lastPacketRec, lastPacketRec + receiveWindowSize,
          lastPacketSend);
    }
  }
}