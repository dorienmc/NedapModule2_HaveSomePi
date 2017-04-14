package com.nedap.university.fileTranser;

import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

/**
 * UDP packet with a datagram and some extra info.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class UDPPacket {
  MyUDPHeader header;   //Header settings, in the MyUDPHeader format
  byte[] data;          //Payload data

  /*
   * Create UDPPacket
   */
  public UDPPacket(int sourcePort, int destPort, int seqNumber, int ackNumber, int id, int offset) {
    data = new byte[0];
    header = new MyUDPHeader(sourcePort,destPort,seqNumber,ackNumber,id,offset);
    updateChecksum();
  }

  /*
   * Create UDPPacket
   */
  public UDPPacket(int sourcePort, int destPort, int seqNumber, int ackNumber) {
    this(sourcePort,destPort,seqNumber,ackNumber,1,0);
  }

  /*
   * Create UDPPacket from DatagramPacket.
   */
  public UDPPacket(DatagramPacket packet) throws ArrayIndexOutOfBoundsException, NegativeArraySizeException {
    header = new MyUDPHeader();
    byte[] packetData = packet.getData();
    if(packetData.length < header.getHeaderSize()) {
      throw new ArrayIndexOutOfBoundsException(
          "Expected at least " + header.getHeaderSize() + "bytes of data.");
    }

    //Split datagram packet into UDP header fields and data
    byte[] headerFields = new byte[header.getHeaderSize()];
    System.arraycopy(packetData,0,headerFields,0,headerFields.length);
    header = new MyUDPHeader(headerFields);


    data = new byte[packet.getLength() - headerFields.length];
    System.arraycopy(packetData,headerFields.length,data,0,data.length);

//    //TODO check if checksum is correct?
//    //TODO check if ack and seq are correct?
  }


  /**
   * Convert UDPPacket to Datagram.
   */
  public DatagramPacket toDatagram(InetAddress destAddress) {
    DatagramPacket packet = new DatagramPacket(this.getPkt(),0,getLength(),
        destAddress, getDestPort());
    return packet;
  }


  /****** Flag methods *****/
  public void setFlags(int flags) {
    header.setField(HeaderField.FLAGS,flags);
    updateChecksum();
  }

  public boolean isFlagSet(Flag flag) {
    return Flag.isSet(flag, header.getField(HeaderField.FLAGS));
  }

  public int getFlags() {
    return header.getField(HeaderField.FLAGS);
  }


  /****** Getters and Setters *****/
  public void setData(byte[] data) {
    this.data = data;
    setLength(data.length + header.getHeaderSize());
    updateChecksum();
  }

  private void setLength(int length) {
    header.setField(HeaderField.LENGTH,length);
  }

  public byte[] getData() {
    return data;
  }

  public int getSourcePort() {
    return header.getField(HeaderField.SOURCE_PORT);
  }

  public int getDestPort() {
    return header.getField(HeaderField.DEST_PORT);
  }

  public int getLength() {
    return header.getField(HeaderField.LENGTH);
  }

  public int getSequenceNumber() { return header.getField(HeaderField.SEQ_NUMBER);}

  public int getAckNumber() { return header.getField(HeaderField.ACK_NUMBER);}

  public int getRequestId() { return header.getField(HeaderField.REQUEST_ID);}

  public void setHeaderSetting(HeaderField field, int value) {
    header.setField(field, value);
    updateChecksum();
  }

  private byte[] getPkt() {
    ByteBuffer buffer = ByteBuffer.allocate(getLength());
    buffer.put(header.getHeader());
    if(data.length > 0) {
      buffer.put(data);
    }

    return buffer.array();
  }


  /****** Checksum methods and validity checking *****/
  /* Warning updates the checksum field */
  public int updateChecksum() {
    Checksum checksum = new CRC32();

    header.setField(HeaderField.CHECKSUM,0);
    checksum.update(getPkt(),0,getLength());

    int checksumValue = (short)checksum.getValue();
    header.setField(HeaderField.CHECKSUM,checksumValue);

    return checksumValue;
  }

  private boolean checkChecksum() {
    Checksum checksum = new CRC32();

    checksum.update(getPkt(),0,getLength());
    return checksum.getValue() == 0;
  }

  /* Check if packet is valid, eg. checksum is correct and packet is meant for this host) */
  public boolean isValid(int expectedSourcePort, int expectedDestPort) {
    if(getSourcePort() != expectedSourcePort) {
      System.out.println(String.format("Source port: expected %d but got %d", expectedSourcePort, getSourcePort()));
      return false;
    }

    if(getDestPort() != expectedDestPort) {
      System.out.println(String.format("Dest port: expected %d but got %d", expectedDestPort, getDestPort()));
      return false;
    }

    return checkChecksum();
  }

  @Override
  public String toString() {
    String result = "Packet representation in Hex.\n";
    result += "Header: " + HexBin.encode(header.getHeader());
    result += "\nData: " + HexBin.encode(data);
    return result;
  }
}
