package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import com.nedap.university.fileTranser.Sender;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Abstract class for an ARQ protocol
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public abstract class Protocol {
  public static final int MAX_BUFFER = 1500;
  byte[] dataToSend;
  byte[] dataReceived;
  Sender sender;
  Receiver receiver;
  public static final int TIMEOUT = 7000;

  int seqNumber; //Sequence number of next packet that is to be send.
  int ackNumber; //Sequence number of next expected packet from receiver.

  public Protocol(Sender sender, Receiver receiver) {
    this.sender = sender;
    this.receiver = receiver;
    this.dataToSend = new byte[0];
    this.dataReceived = new byte[0];
  }

  //TODO split file into multiple UDP packets

  /* Send given data (in parts) and wait for acks if requested */
  public abstract void send(byte[] data, int flags) throws IOException;

  /* Send packet over socket */
  protected void sendPacket(UDPPacket packet) {
    sender.addPacketToBuffer(packet);
  }

  /* Wait for next packet over socket */
  protected UDPPacket receivePacket() throws IOException,TimeoutException {
    UDPPacket response = receiver.retrievePacket();
    int time = 0;

    while(response == null) {
      //Wait
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      time += 10;
      if(time > TIMEOUT) {
        throw new TimeoutException("Exceeded time out, resend");
      }

      response = receiver.retrievePacket();
    }
    return response;
  }

  /* Add data to received data */
  protected void addReceivedData(byte[] data) {
    int oldLength = dataReceived.length;
    int dataLength = data.length;
    dataReceived = Arrays.copyOf(dataReceived, oldLength + dataLength);
    System.arraycopy(data,0,dataReceived,oldLength,dataLength);
  }

  /* Return received data */
  public byte[] receive() {
    return dataReceived;
  }
}
