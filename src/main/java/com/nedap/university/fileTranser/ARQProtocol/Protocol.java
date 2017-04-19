package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.Main;
import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.Sender;
import com.nedap.university.fileTranser.UDPPacket;
import com.nedap.university.statistics.Statistics;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;

/**
 * Abstract class for an ARQ protocol
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public abstract class Protocol extends Thread {
  public static final int LOG_TIME = 10;       //Time between logs, in sec.
  private Sender sender;
  private Receiver receiver;
  private byte requestId;
  ConcurrentLinkedDeque<UDPPacket> sendBuffer;        //Packets that still need to be send
  ConcurrentHashMap<Integer,UDPPacket> receiveBuffer; //Packets need to be processed, mapped by seq. number
  ConcurrentLinkedDeque<UDPPacket> resendBuffer;      //Packets that have timed out
  private int seqNumber;          //Sequence number of next packet that is to be send.
  private int offset;             //Offset of the last packet that has been acked.
  private int expectedNumberOfPackets; //Expected number packets that will be send.
  Status status;
  private int timeOut;            //Time out in ms.
  protected Statistics statistics;
  protected Handler handler;

  public enum Status {
    CREATED, PAUSED, RUNNING, STOPPING;
  }

  public Protocol(Sender sender, Receiver receiver, byte requestId, int timeOut, Handler handler) {
    this.sender = sender;
    this.receiver = receiver;
    this.requestId = requestId;
    this.seqNumber = 0;
    this.sendBuffer = new ConcurrentLinkedDeque<>();
    this.receiveBuffer = new ConcurrentHashMap<>();
    this.resendBuffer = new ConcurrentLinkedDeque<>();
    this.status = Status.CREATED;
    this.timeOut = timeOut;
    this.statistics = handler.getStatistics();
    this.handler = handler;
  }

  /********** Abstract methods ***********/

  /**
   *  Return next packet that should be send. Or null if there are none currently.
   * Internally update the seq., add it to unacked, create timer etc.
   **/
  public abstract UDPPacket getNextPacket();

  /**
   *  Return true if protocol is still sending packets and/or waiting for acks
   **/
  public abstract boolean busy();

  /**
   * Send data in sender buffer and receive data in receive buffer according to protocol
   **/
  public abstract void run();

  /**
   *  Tell if received packet is expected, if not it should be dropped .
   **/
  public abstract boolean isExpected(UDPPacket packet);


  /********** Add/Remove from Buffers **********/
  public void addPacketToSendBuffer(UDPPacket packet) {
    packet.setHeaderSetting(HeaderField.SEQ_NUMBER, seqNumber);
    seqNumber++;
    printDebug(String.format("Add packet to send buffer of request %d with seqno: %d", packet.getRequestId(), packet.getSequenceNumber()));
    sendBuffer.add(packet);
  }

  public void addPacketToReceiverBuffer(UDPPacket packet) {
    printDebug(String.format("Add packet to receive buffer with seq: %d, ack: %d, offset:%d",
        packet.getSequenceNumber(), packet.getAckNumber(), packet.getOffset()));
    receiveBuffer.put(packet.getSequenceNumber(),packet);
  }

  void addPacketToResendBuffer(UDPPacket packet) {
    printDebug(String.format("Time out of packet with seqno: %d has elapsed.", packet.getSequenceNumber()));
    resendBuffer.add(packet);
  }

  /********** Send request ***********/
  /**
   * Tell protocol to send given request.
   * @param keyword Type of request
   * @param addEOR add End-of-Request packet if true
   */
  public void sendRequest(Keyword keyword, boolean addEOR) {
    Flag flag = keyword.toFlag();
    if(flag != null) {
      sendRequest(new byte[0], flag, addEOR);
    }
  }

  /**
   * Tell protocol to send given request.
   * @param data Payload to be start
   * @param flag Flag that is set
   * @param addEOR add End-of-Request packet if true
   */
  public void sendRequest(byte[] data, Flag flag, boolean addEOR) {
    sendRequest(data, flag.getValue(), addEOR);
  }

  /**
   * Tell protocol to send given request.
   * @param data Payload to be start
   * @param flags Flags that are set
   * @param addEOR add End-of-Request packet if true
   */
  private void sendRequest(byte[] data, int flags, boolean addEOR) {
    UDPPacket packet = createEmptyPacket();
    packet.setData(data);
    packet.setFlags(flags);
    addPacketToSendBuffer(packet);

    //Add End-of-Request packet
    if(addEOR) {
      sendEndOfRequestPacket();
    }

    if(!this.isAlive()) {
      start();
    }
  }

  /**
   * Send end-of-request packet.
   */
  public void sendEndOfRequestPacket() {
    UDPPacket lastPacket = createEmptyPacket();
    lastPacket.setFlags(Flag.LAST.getValue());
    addPacketToSendBuffer(lastPacket);
  }

  /**
   * Send end-of-request packet, with data
   */
  public void sendEndOfRequestPacket(byte[] data) {
    UDPPacket lastPacket = createEmptyPacket();
    lastPacket.setFlags(Flag.LAST.getValue());
    lastPacket.setData(data);
    addPacketToSendBuffer(lastPacket);
  }

  /********** Send ack *********/
  /**
   * Send packet with flags or data, only for acknowledgement.
   */
  public void sendAck() {
    UDPPacket packet = createEmptyPacket();
    addPacketToSendBuffer(packet);
  }

  /********** Send data **********/
  /**
   * Send data (not from file), small enough to fit in 1 packet
   * @param data Payload to be start
   * @param addEOR add End-of-Request packet if true
   **/
  public void sendData(byte[] data, boolean addEOR) {
    UDPPacket packet = createEmptyPacket();
    packet.setData(data);
    addPacketToSendBuffer(packet);

    //Add End-of-Request packet
    if(addEOR) {
      sendEndOfRequestPacket();
    }

    if(!this.isAlive()) {
      start();
    }
  }



  /********** Receive data **********/
  /** Wait for next packet over socket
   * @param maxTimeOut maximum time the method will wait for a packet
   *  Set to -1 for infinite time out, set to 0 for default timeout of the protocol.
   * @throws TimeoutException when no packet has arrived after 'maxTimeOut' ms
   **/
  public UDPPacket receivePacket(int maxTimeOut) throws TimeoutException {
    maxTimeOut = (maxTimeOut == -1) ? getTimeOut() : maxTimeOut;
    int time = 0;
    while(receiveBuffer.isEmpty()) {
      //Wait
      while(getStatus().equals(Status.PAUSED)) {
        Utils.sleep(100);
      }
      Utils.sleep(10);
      time += 10;

      //Log last ack received (every LOG_TIME seconds)
      log(time);

      if(maxTimeOut > 0 && time > maxTimeOut) {
        throw new TimeoutException("Protocol.receivePacket: Exceeded timeOut of " + maxTimeOut + "ms.");
      }
    }

    Integer seqNumber = receiveBuffer.keys().nextElement();
    return receiveBuffer.remove(seqNumber);
  }

  /** Wait for next packet over socket
   * @param seqNumber  sequence number of the packet that we wish to retrieve
   * @param maxTimeOut maximum time the method will wait for a packet
   *  Set to -1 for infinite time out, set to 0 for default timeout of the protocol.
   * @throws TimeoutException when the requested packet has not arrived after 'maxTimeOut' ms
   **/
  public UDPPacket receivePacket(int seqNumber, int maxTimeOut) throws TimeoutException {
    maxTimeOut = (maxTimeOut == -1) ? getTimeOut() : maxTimeOut;
    int time = 0;
    while(!receiveBuffer.containsKey(seqNumber)) {
      //Wait
      while(getStatus().equals(Status.PAUSED)) {
        Utils.sleep(100);
      }
      Utils.sleep(10);
      time += 10;

      //Log last ack received (every LOG_TIME seconds)
      log(time);

      if(maxTimeOut > 0 && time > maxTimeOut) {
        throw new TimeoutException("Protocol.receivePacket: Exceeded timeOut of " + maxTimeOut + "ms.");
      }
    }

    return receiveBuffer.remove(seqNumber);
  }

  /**
   * Wait for specific ack.
   * @return true if given ack arrives within time.
   */
  public boolean waitForAck(int ackNumber, int maxTimeOut) {
    maxTimeOut = (maxTimeOut == -1) ? getTimeOut() : maxTimeOut;
    int time = 0;

    while(true) {
      if(getLastAck() >= ackNumber) {
        return true;
      }

      //Wait
      while(getStatus().equals(Status.PAUSED)) {
        Utils.sleep(100);
      }
      Utils.sleep(10);
      time += 10;

      //Log last ack received (every LOG_TIME seconds)
      log(time);

      if(maxTimeOut > 0 && time > maxTimeOut) {
        return false;
      }

    }
  }

  /** Log last ack received (every LOG_TIME seconds) **/
  private void log(int time) {
    if((time % (LOG_TIME * 1000)) == 0) {
      print("Got acks for " + getLastAck() + " out of " + getExpectedNumberOfPackets() + " packets");
    }

  }

  /********** Other ***********/
  /**
   * Create new empty packet for sending.
   * @return UDP packet with correct source/dest port and requestId.
   */
  public UDPPacket createEmptyPacket(){
    return new UDPPacket(sender.getSourcePort(),sender.getDestPort(),0,0,requestId,0);
  }

  public int getSeqNumber() {
    return seqNumber;
  }

  public abstract int getLastAck();

  Status getStatus() {
    return status;
  }

  public abstract String getInfo();

  public void setStatus(Status status) {
    this.status = status;
  }

  public int getTimeOut() {
    return timeOut;
  }

  public int getExpectedNumberOfPackets() {
    return expectedNumberOfPackets;
  }

  public void setExpectedNumberOfPackets(int expectedNumberOfPackets) {
    this.expectedNumberOfPackets = expectedNumberOfPackets;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }


  public void printDebug(String msg) {
    handler.printDebug(msg);
  }

  public void print(String msg) {
    handler.print(msg);
  }

  public synchronized boolean pause() {
    setStatus(Status.PAUSED);
    return true;
  }

  public synchronized boolean unPause() {
    setStatus(Status.RUNNING);
    return true;
  }


}

