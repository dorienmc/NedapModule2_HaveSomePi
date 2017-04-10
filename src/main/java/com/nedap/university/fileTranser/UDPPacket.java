package com.nedap.university.fileTranser;

/**
 * UDP packet with a datagram and some extra info.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class UDPPacket {
  int sourcePort; //Senders port                                2bytes
  int destPort;   //Receivers port                              2bytes
  int length;     //Length in bytes of UDP header + UDP data    2bytes
  int checksum;   //User for error checking                     2bytes
  int seqNumber;  //Used to ensure delivery guarantee           2bytes
  int ackNumber;  //Used to ensure correct order                2bytes
  int flags;      //Used to describe request type               1byte
  int id;         //File id in case of fragmentation            1byte
  int offset;     //Offset in bytes of fragment                 2byte

  public UDPPacket(int sourcePort, int destPort, int seqNumber, int ackNumber) {
    //TODO
  }

  public UDPPacket(int sourcePort, int destPort, int seqNumber, int ackNumber, int id, int offset) {
    //TODO
  }

  public void setLength(int length) {
    this.length = length;
  }

  public void setChecksum(int checksum) {
    this.checksum = checksum;
  }

  public void setFlags(int flags) {
    this.flags = flags;
  }

  public byte[] getData() {
    //TODO
    return new byte[0];
  }
  
}
