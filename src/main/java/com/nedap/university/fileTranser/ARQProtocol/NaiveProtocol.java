package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.Utils;
import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.Sender;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;

/**
 * Very naive protocol that just send data and does send acks.
 * Doesnt change the sequence number either. Also sends all data at once.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class NaiveProtocol extends Protocol{
  public NaiveProtocol(Sender sender, Receiver receiver, byte requestId) {
    super(sender, receiver, requestId);
  }

  /**
   *  Return next packet that should be send. Or null if there are none currently.
   * Internally update the seq., add it to unacked, create timer etc.
   **/
  @Override
  public UDPPacket getNextPacket() {
    if (getStatus().equals(Status.RUNNING)) {
      if(sendBuffer.size() > 0) {
        UDPPacket packet = sendBuffer.pollFirst();
        System.out.println("Sending packet " + packet.getSequenceNumber() + ", offset " + packet.getOffset());
        return packet;
      }
    }
    return null;
  }

  /**
   *  Return true if protocol is still sending packets and/or waiting for acks
   *  Naive protocol is busy as long as there are still packets in the sendBuffer
   **/
  @Override
  public boolean busy() {
    return sendBuffer.size() > 0;
  }

  /**
   * Send data in sender buffer according to protocol
   **/
  @Override
  public void run() {
    setStatus(Status.RUNNING);

    while(busy()) {
      Utils.sleep(100);
    }
  }

  /**
   *  Tell if received packet is expected, if not it should be dropped .
   *  The naive protocol allows all packets.
   **/
  @Override
  public boolean isExpected(UDPPacket packet) {
    return true;
  }

  /** Give status info that the Handler can than represent to the user */
  public String getInfo() {
    return "Current seq: " + getSeqNumber();
  }
}
