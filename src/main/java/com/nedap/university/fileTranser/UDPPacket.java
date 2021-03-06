package com.nedap.university.fileTranser;

import com.nedap.university.Utils;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
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
  public static final int MAX_PAYLOAD = 2900;
  public static final int MAX_PACKET_SIZE = MAX_PAYLOAD + MyUDPHeader.HeaderField.getTotalLength();
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
    ByteBuffer buffer = ByteBuffer.allocate(packetData.length).put(packetData);
    buffer.rewind();

    byte[] headerFields = new byte[header.getHeaderSize()];
    buffer.get(headerFields);
    header = new MyUDPHeader(headerFields);

    //Only retrieve actual data (datagram might be appended with zeros)
    data = new byte[header.getField(HeaderField.LENGTH) - header.getHeaderSize()];
    if(data.length > 0) {
      buffer.get(data);
    }
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
    header.setField(HeaderField.LENGTH,data.length + header.getHeaderSize());
    updateChecksum();
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

  public long getChecksum() {return header.getCheckSum();}

  public int getOffset() { return header.getField(HeaderField.OFFSET);}

  public void setHeaderSetting(HeaderField field, int value) {
    header.setField(field, value);
    updateChecksum();
  }

  public byte[] getPkt() {
    ByteBuffer buffer = ByteBuffer.allocate(getLength());
    buffer.put(header.getHeader());
    if(data.length > 0) {
      buffer.put(data);
    }

    return buffer.array();
  }


  /****** Checksum methods and validity checking *****/
  /* Warning updates the checksum field */
  public void updateChecksum() {
    long newChecksum = calculateChecksum();
    header.setCheckSum(newChecksum);
  }

  public long calculateChecksum() {
    Checksum checksum = new CRC32();
    long oldChecksum = header.getCheckSum();

    header.setCheckSum(0);
    checksum.update(getPkt(),0,getLength());
    long checksumValue = checksum.getValue();

    header.setCheckSum(oldChecksum);
    return checksumValue;
  }

  private boolean checkChecksum() {
    long calculated = calculateChecksum();
    long expected = getChecksum();
    return calculated == expected;
  }

  /* Check if packet is valid, eg. checksum is correct and packet is meant for this host) */
  public boolean isValid(int expectedSourcePort, int expectedDestPort) {
    if(getSourcePort() != expectedSourcePort) {
      //System.out.println(String.format("Source port: expected %d but got %d", expectedSourcePort, getSourcePort()));
      return false;
    }

    if(getDestPort() != expectedDestPort) {
      //System.out.println(String.format("Dest port: expected %d but got %d", expectedDestPort, getDestPort()));
      return false;
    }

    if(checkChecksum()) {
      return true;
    } else {
      //System.out.println(String.format("Checksum: expected %d but got %d", getChecksum(), calculateChecksum()));
      return false;
    }
  }

  @Override
  public String toString() {
    String result = "Packet representation in Hex.\n";
    result += "Header: \n" + header;//Utils.binaryArrToHexString(header.getHeader());
    result += "Data: " + Utils.binaryArrToHexString(data);
    return result;
  }
}
