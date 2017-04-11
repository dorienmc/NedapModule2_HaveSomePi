package com.nedap.university.fileTranser;

import java.nio.ByteBuffer;

/**
 * Created by dorien.meijercluwen on 11/04/2017.
 */
public class MyUDPHeader {
  private ByteBuffer buffer;

  public enum HeaderField {
    SOURCE_PORT(0,2), DEST_PORT(2,2), LENGTH(4,2), CHECKSUM(6,2), SEQ_NUMBER(8,2), ACK_NUMBER(10,2),
    FLAGS(12,1), FILE_ID(13,1), OFFSET(14,2);

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
    setField(HeaderField.FILE_ID, 0);
    setField(HeaderField.OFFSET, 0);
  }

  public MyUDPHeader(int sourcePort, int destPort, int seqNumber, int ackNumber, int id,
      int offset) {
    this();
    setField(HeaderField.SOURCE_PORT,sourcePort);
    setField(HeaderField.DEST_PORT,destPort);
    setField(HeaderField.SEQ_NUMBER, seqNumber);
    setField(HeaderField.ACK_NUMBER,ackNumber);
    setField(HeaderField.FILE_ID, id);
    setField(HeaderField.OFFSET, offset);
  }

  public int getHeaderSize() {
    return HeaderField.getTotalLength();
  }

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
    }
    return -1;
  }

  public byte[] getHeader() {
    return buffer.array();
  }
}
