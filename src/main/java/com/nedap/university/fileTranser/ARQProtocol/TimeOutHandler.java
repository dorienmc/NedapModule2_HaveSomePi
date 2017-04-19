package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.Utils;
import com.nedap.university.fileTranser.UDPPacket;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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

  /** Add new timer for the given packet, overwrites old timer if there is one **/
  public void addTimer(UDPPacket packet) {
    timers.put(packet, new Timer(timeOut));
  }

  /* Stop timer of packet with the given sequence number */
  public boolean stopTimer(int sequenceNumber) {
    for(UDPPacket packet: timers.keySet()) {
      if(packet.getSequenceNumber() == sequenceNumber) {
        timers.remove(packet);
        return true;
      }
    }
    return false;
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
      Iterator<Entry<UDPPacket,Timer>> iterator = timers.entrySet().iterator();
      while(iterator.hasNext()) {
        Map.Entry<UDPPacket,Timer> entry = iterator.next();
        UDPPacket packet = entry.getKey();
        Timer timer = entry.getValue();

        //Check if timer has elapsed.
        if(timer.hasElapsed()) {
          //Remove entry from map
          iterator.remove();

          //Add packet to resend buffer
          protocol.addPacketToResendBuffer(packet);
        }
      }


      Utils.sleep(10);
    }

    //Stop all timers
    Iterator<Entry<UDPPacket,Timer>> iterator = timers.entrySet().iterator();
    while(iterator.hasNext()) {
      Map.Entry<UDPPacket,Timer> entry = iterator.next();
      UDPPacket packet = entry.getKey();

      //Remove entry from map
      iterator.remove();

      //Add packet to resend buffer
      protocol.addPacketToResendBuffer(packet);
    }
  }

}
