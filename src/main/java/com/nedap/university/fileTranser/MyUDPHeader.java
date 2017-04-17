package com.nedap.university.fileTranser;

import java.nio.ByteBuffer;

/**
 * Created by dorien.meijercluwen on 11/04/2017.
 */
public class MyUDPHeader {
  private ByteBuffer buffer;

  public enum HeaderField {
    SOURCE_PORT(0,4), //Port of sender
    DEST_PORT(4,4),   //Port of receiver
    LENGTH(8,2),      //Length of UDP packet, eg. datagrampacket.getData()
    CHECKSUM(10,8),    //Checksum for UDP packet
    SEQ_NUMBER(18,2), //Sequence number
    ACK_NUMBER(20,2), //Expected sequence number of next response
    FLAGS(22,1),      //Option flags (see fileTransfer.Flag)
    REQUEST_ID(23,1),   //Request id, used for demuxing.
    OFFSET(24,2);     //Offset in fragmented files (in bytes) //TODO count in bigger chunks? Otherwise this field is to small

    private int length;
    private int startIndex;

    HeaderField(int startIndex, int lengthInBytes) {
      this.length = lengthInBytes;
      this.startIndex = startIndex;
    }

    public int getStartIndex() {
      return startIndex;
    }

    public int getLength() {
      return length;
    }

    public static int getTotalLength() {
      int totLength = 0;
      for(HeaderField field: HeaderField.values()) {
        totLength += field.getLength();
      }
      return totLength;
    }
  }

  public MyUDPHeader() {
    buffer = ByteBuffer.allocate(getHeaderSize());
    setField(HeaderField.SOURCE_PORT,0);
    setField(HeaderField.DEST_PORT,0);
    setField(HeaderField.LENGTH,getHeaderSize()); //Update this when setting data
    setField(HeaderField.CHECKSUM,0); //Calculate checksum over header and data in packet
    setField(HeaderField.SEQ_NUMBER, 0);
    setField(HeaderField.ACK_NUMBER,0);
    setField(HeaderField.FLAGS,0); //Set flags in packet
    setField(HeaderField.REQUEST_ID, 0);
    setField(HeaderField.OFFSET, 0);
  }

  public MyUDPHeader(int sourcePort, int destPort, int seqNumber, int ackNumber, int id,
      int offset) {
    this();
    setField(HeaderField.SOURCE_PORT,sourcePort);
    setField(HeaderField.DEST_PORT,destPort);
    setField(HeaderField.SEQ_NUMBER, seqNumber);
    setField(HeaderField.ACK_NUMBER,ackNumber);
    setField(HeaderField.REQUEST_ID, id);
    setField(HeaderField.OFFSET, offset);
  }

  public MyUDPHeader(byte[] headerFields) throws ArrayIndexOutOfBoundsException {
    if (headerFields.length != getHeaderSize()) {
      throw new ArrayIndexOutOfBoundsException(
          String.format("Expected %d bytes but got %d", HeaderField.values().length, headerFields.length));
    }

    buffer = ByteBuffer.allocate(getHeaderSize()).put(headerFields);
  }

  public int getHeaderSize() {
    return HeaderField.getTotalLength();
  }

  /* Only for int/short/byte values! */
  public void setField(HeaderField field, int value) {
    switch (field.getLength()) {
      case 1: {
        buffer.put(field.startIndex, (byte) value);
        break;
      }
      case 2: {
        buffer.putShort(field.startIndex, (short) value);
        break;
      }
      case 4: {
        buffer.putInt(field.startIndex,value);
        break;
      }
    }
  }

  /* Returns value if given header field, returns -1 if not found. */
  public int getField(HeaderField field) {
    switch (field.getLength()) {
      case 1: {
        return buffer.get(field.startIndex);
      }
      case 2: {
        return buffer.getShort(field.startIndex);
      }
      case 4: {
        return buffer.getInt(field.startIndex);
      }
    }
    return -1;
  }

  public void setCheckSum(long value) {
    buffer.putLong(HeaderField.CHECKSUM.startIndex, value);
  }

  public long getCheckSum() {
    return buffer.getLong(HeaderField.CHECKSUM.startIndex);
  }

  public byte[] getHeader() {
    return buffer.array();
  }

  @Override
  public String toString() {
    String result = "";
    for(HeaderField field: HeaderField.values()) {
      if(field.equals(HeaderField.CHECKSUM)) {
        result += String.format("%s: %d\n",field.toString(), getCheckSum());
      } else {
        result += String.format("%s: %d\n",field.toString(), getField(field));
      }
    }
    return result;
  }

}
