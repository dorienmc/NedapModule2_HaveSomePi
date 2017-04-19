package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.Sender;
import com.nedap.university.fileTranser.UDPPacket;
import com.nedap.university.statistics.Statistics;

/**
 * Protocol that sends one packet at a time and waits until it is acked.
 * Uses timeout timers to resend if no ack has been received after a certain period of time.
 * Created by dorien.meijercluwen on 15/04/2017.
 */
public class StopAndWaitProtocol extends Protocol {
  public static final int TIMEOUT = 5000; //ms
  TimeOutHandler timeOutHandler;
  private volatile int lastAckRec;    //All packets below this sequence number have been acked.
  private volatile int lastPacketRec; //All packets (of the other host) with this sequence number or smaller have been received.
  private volatile int lastPacketSend;//Sequence number of the last send packet, all packets with this sequence number or smaller have been send at least once.

  public StopAndWaitProtocol(Sender sender, Receiver receiver, byte requestId, Handler handler) {
    super(sender, receiver, requestId, TIMEOUT, handler);
    timeOutHandler = new TimeOutHandler(this, TIMEOUT);
    this.lastAckRec = 0;
    this.lastPacketRec = -1;
    this.lastPacketSend = -1;
  }

  /**
   *  Return next packet that should be start. Or null if there are none currently.
   *  Gives priority over timeout packets. The packet is added to the list of unacked packets
   *  and given a timeout timer.
   **/
  @Override
  public UDPPacket getNextPacket() {
    if(status.equals(Status.PAUSED)) {
      return null;
    }

    UDPPacket packet = null;

    if(resendBuffer.size() > 0) {
      packet = resendBuffer.pollFirst();
      //Drop timed out packet if its not in the start window
      if(!isInSendWindow(packet.getSequenceNumber())) {
        packet = null;
      } else {
        statistics.logRetransmission();
      }
    } else if(sendBuffer.size() > 0) {
      //Peek at packet and check if we are already allowed to start it
      UDPPacket peekedPacket = sendBuffer.peekFirst();
      if(isInSendWindow(peekedPacket.getSequenceNumber())) {
        packet = sendBuffer.pollFirst();
        setLastPacketSend(packet.getSequenceNumber());
      }
    }

    if(packet != null) {
      //Set timer, except for last packet
      if(!packet.isFlagSet(Flag.LAST)) {
        printDebug("Create timer for packet with seq " + packet.getSequenceNumber()
            + " of request " + packet.getRequestId());
        timeOutHandler.addTimer(packet);
      } else {
        printDebug("Last packet, so start stopping.");
        super.setStatus(Status.STOPPING);
      }
      //Set ack to the next expected packet.
      packet.setHeaderSetting(HeaderField.ACK_NUMBER, getLastPacketRec() + 1);

      printDebug("Status: " + getInfo());
    }

    return packet;
  }

  /**
   *  Return true if either the sendBuffer or resendbuffer are empty or when there are still unacked packets.
   **/
  @Override
  public boolean busy() {
    return !getStatus().equals(Status.STOPPING);
//    return (sendBuffer.size() > 0 || resendBuffer.size() > 0
//        || timeOutHandler.getNumberOfUnackedPackets() > 0);
  }

  /**
   * Send data in sender buffer according to protocol
   **/
  @Override
  public void run() {
    timeOutHandler.start();
    setStatus(Status.RUNNING);
    printDebug("Start sending/receiving.");

    while(busy()) {
      Utils.sleep(100);
    }

    if(getStatus().equals(Status.STOPPING)) {
      //Wait for 1/2 TIMEOUT, if we are still stopping then, then stop.
      Utils.sleep(TIMEOUT/2);
    }

    printDebug("Done sending/receiving.");
    timeOutHandler.stopHandler();
  }

  /**
   *  Tell if received packet is expected, if not it should be dropped .
   *  A packet is only allowed if its sequence number is equal to the expected ack number.
   **/
  @Override
  public boolean isExpected(UDPPacket packet) {
    //Check if sequence number is expected
    int receivedSeq = packet.getSequenceNumber();
    int receivedAck = packet.getAckNumber();
    printDebug(String.format("Packet seq: %d, ack: %d. [LAR: %d, LFS: %d], [LFR: %d, LAF: %d]",
        receivedSeq, receivedAck, getLastAckRec(), getLastPacketSend(),
        getLastPacketRec(), getLastPacketRec() + 1 ));

    if(isInRecWindow(receivedSeq)) {
      //update the last frame received and last ack received
      setLastPacketRec(receivedSeq);
      setLastAckRec(receivedAck);

      //If so, stop the timer of the packet that this packet acks.
      //Note: ACK tells the next packet that is expected, so the previous packet is acked.
      timeOutHandler.stopTimer(receivedAck - 1);
      printDebug("Stopping timer of packet with seq: " + (receivedAck - 1));


      //If this was the last packet, go to stopping state
      if(packet.isFlagSet(Flag.LAST)) {
        printDebug("Receiver: Last packet, so start stopping.");
        setStatus(Status.STOPPING);
      }

      printDebug("Status: " + getInfo());

      return true;
    }
    //Otherwise return false
    return false;
  }

  @Override
  public int getLastAck() {
    return lastAckRec;
  }

  /** Give status info that the Handler can than represent to the user */
  public String getInfo() {
    if(getStatus().equals(Status.PAUSED)) {
      return Status.PAUSED.toString();
    } else {
      return String.format("[LAR: %d, LFS: %d], [LFR: %d, LAF: %d]",
          getLastAckRec(), getLastPacketSend(),
          getLastPacketRec(), getLastPacketRec() + 1);
    }
  }

  /** A packet can be send if its sequence number equals LastAckRec,
   * as that is the next packet that the other side expects. */
  public boolean isInSendWindow(int seq) {
    //getLastAckRec() <= seq && seq < getLastAckRec() + getSendWindowSize();
    return seq == getLastAckRec();
  }

  /** A packet is in the receive window of the stop-and-wait if its the one
   * after the lastFrameRec.
   */
  public boolean isInRecWindow(int seq) {
    return seq == getLastPacketRec() + 1;
  }

  public int getLastAckRec() {
    return lastAckRec;
  }

  public void setLastAckRec(int lastAckRec) {
    this.lastAckRec = lastAckRec;
  }

  public int getLastPacketRec() {
    return lastPacketRec;
  }

  public void setLastPacketRec(int lastPacketRec) {
    this.lastPacketRec = lastPacketRec;
  }

  public int getLastPacketSend() {
    return lastPacketSend;
  }

  public void setLastPacketSend(int lastPacketSend) {
    this.lastPacketSend = lastPacketSend;
  }
}
