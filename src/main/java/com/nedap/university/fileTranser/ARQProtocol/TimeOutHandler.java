package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.Utils;
import com.nedap.university.fileTranser.UDPPacket;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 15/04/2017.
 */
public class TimeOutHandler extends Thread {
  ConcurrentHashMap<UDPPacket, Timer> timers;
  Protocol protocol;
  long timeOut;
  volatile boolean stop;

  public class Timer {
    long startTime;
    long maxTime;

    /* Create timer which starts now and elapses in 'maxTime' ms. */
    public Timer(long maxTime) {
      this.maxTime = maxTime;
      this.startTime = System.currentTimeMillis();
    }

    public boolean hasElapsed() {
      return System.currentTimeMillis() - startTime > maxTime;
    }
  }

  public TimeOutHandler(Protocol protocol, long timeOut) {
    this.timers = new ConcurrentHashMap<>();
    this.protocol = protocol;
    this.timeOut = timeOut;
    this.stop = false;
  }

  //TODO overwrite old entry or keep older?
  public void addTimer(UDPPacket packet) {
    System.out.println("Create timer for packet with seq " + packet.getSequenceNumber()
        + " of request " + packet.getRequestId());
    timers.put(packet, new Timer(timeOut));
  }

  /* Stop timer of packet with the given sequence number */
  public boolean stopTimer(int sequenceNumber) {
    for(UDPPacket packet: timers.keySet()) {
      if(packet.getSequenceNumber() == sequenceNumber) {
        System.out.println("Stopping timer of packet with seq: " + sequenceNumber);
        timers.remove(packet);
        return true;
      }
    }
    return false;
  }

  /* Stop timer of given packet */
  private boolean stopTimer(UDPPacket packet) {
    return (timers.remove(packet) != null);
  }

  public synchronized void stopHandler() {
    this.stop = true;
  }

  public int getNumberOfUnackedPackets() {
    return timers.size();
  }

  @Override
  public void run() {
    while(!stop) {
      for(Entry<UDPPacket,Timer> entry: timers.entrySet()) {
        //Check if timer has elapsed.
        if(entry.getValue().hasElapsed()) {
          //Add to resend buffer
          UDPPacket packet = entry.getKey();
          System.out.println("Time out of packet with seqNumber " + packet.getSequenceNumber() + " has elapsed.");
          stopTimer(packet); //TODO check if removing from map is possible during iterating over it!
          protocol.addPacketToResendBuffer(packet);
        }
      }

      Utils.sleep(10);
    }

    //Stop all timers
    for(Entry<UDPPacket,Timer> entry: timers.entrySet()) {
      UDPPacket packet = entry.getKey();
      stopTimer(packet);
      protocol.addPacketToReceiverBuffer(packet);

  }

}
