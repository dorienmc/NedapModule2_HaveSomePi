package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.Sender;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;

/**
 * Abstract class for an ARQ protocol
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public abstract class Protocol {
  public static final int MAX_BUFFER = 1500;
  Sender sender;
  Receiver receiver;
  byte requestId;
  ConcurrentLinkedDeque<UDPPacket> sendBuffer; //Packets that still need to be send
  ConcurrentLinkedDeque<UDPPacket> receiveBuffer; //Packets need to be processed
  int seqNumber; //Sequence number of next packet that is to be send.
  int ackNumber; //Sequence number of next expected packet from receiver.
  Status status;

  public enum Status {
    PAUSED, RUNNING;
  }

  public Protocol(Sender sender, Receiver receiver, byte requestId) {
    this.sender = sender;
    this.receiver = receiver;
    this.requestId = requestId;
    this.ackNumber = 1;
    this.seqNumber = 1;
    this.sendBuffer = new ConcurrentLinkedDeque<>();
    this.receiveBuffer = new ConcurrentLinkedDeque<>();
    this.status = Status.PAUSED;
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
   * Send data in sender buffer according to protocol
   **/
  public abstract void send() throws IOException;

  /**
   *  Tell if received packet is expected, if not it should be dropped .
   **/
  public abstract boolean isExpected(UDPPacket packet);


  /********** Add/Remove from Buffers **********/
  public void addPacketToSendBuffer(UDPPacket packet) {
    //TODO create timeout timer? Set sequence number and ack number?
    packet.setHeaderSetting(HeaderField.SEQ_NUMBER, seqNumber);
    packet.setHeaderSetting(HeaderField.ACK_NUMBER, ackNumber);
    seqNumber++;
    ackNumber++;
    sendBuffer.add(packet);
  }

  public void addPacketToReceiverBuffer(UDPPacket packet) {
    receiveBuffer.add(packet);
  }

  /********** Send request ***********/
  /**
   * Tell protocol to send given request.
   */
  public void sendRequest(Keyword keyword) throws IOException {
    Flag flag = keyword.toFlag();
    if(flag != null) {
      sendRequest(new byte[0], flag);
    }
  }

  /**
   * Tell protocol to send given request.
   */
  public void sendRequest(byte[] data, Flag flag) throws IOException {
    sendRequest(data, flag.getValue());
  }

  /**
   * Tell protocol to send given request.
   */
  public void sendRequest(byte[] data, int flags) throws IOException {
    UDPPacket packet = createEmptyPacket();
    packet.setData(data);
    packet.setFlags(flags);
    addPacketToSendBuffer(packet);

    send();
  }

  /********** Send data **********/
  /* Send data (not from file), small enough to fit in 1 packet */
  public void sendData(byte[] data) throws IOException {
    UDPPacket packet = createEmptyPacket();
    packet.setData(data);
    addPacketToSendBuffer(packet);

    send();
  }

  /********** Receive data **********/
  /* Wait for next packet over socket, set maxTimeOut to 0 for infinite timeout */ //TODO wait for specific packet
  public UDPPacket receivePacket(int maxTimeOut) throws IOException,TimeoutException {
    int time = 0;
    while(receiveBuffer.size() == 0) {
      //Wait
      Utils.sleep(10);
      time += 10;

      if(maxTimeOut > 0 && time > maxTimeOut) {
        throw new TimeoutException("Protocol.receivePacket: Exceeded timeOut of " + maxTimeOut + "ms.");
      }
    }

    UDPPacket response = receiveBuffer.pollFirst();
    return response;
  }

  /********** Other ***********/
  /**
   * Create new empty packet for sending.
   * @return UDP packet with correct source/dest port and requestId.
   */
  public UDPPacket createEmptyPacket(){
    return new UDPPacket(sender.getSourcePort(),sender.getDestPort(),0,0,requestId,0);
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }




//
//
//
//  /* Add data to received data */
//  protected void addReceivedData(byte[] data) {
//    int oldLength = dataReceived.length;
//    int dataLength = data.length;
//    dataReceived = Arrays.copyOf(dataReceived, oldLength + dataLength);
//    System.arraycopy(data,0,dataReceived,oldLength,dataLength);
//  }
//
//  /* Return received data */
//  public byte[] receive() {
//    byte[] response = dataReceived.clone();
//    dataReceived = new byte[0];
//    return response;
//  }


}

