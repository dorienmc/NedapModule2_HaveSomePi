package com.nedap.university.fileTranser;

import static org.junit.Assert.*;

import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dorien.meijercluwen on 11/04/2017.
 */
public class MyUDPHeaderTest {
  MyUDPHeader header;
  int sourcePort = 9297;
  int destPort = 43140;
  int length = HeaderField.getTotalLength();
  int checksum = 0;
  int seqNumber = 13;
  int ackNumber = 255;
  int flags = 0;
  int id = 1;
  int offset = 0;


  @Before
  public void setUp() throws Exception {
    header = new MyUDPHeader(sourcePort,destPort,seqNumber,ackNumber,id,offset);
  }

  @Test
  public void getHeaderSize() throws Exception {
    assertEquals(length,header.getHeaderSize());
  }

  @Test
  public void setField() throws Exception {
    header.setField(HeaderField.FLAGS, Flag.DOWNLOAD.getValue());
    assertEquals(String.format("Value of %s should be %s",HeaderField.FLAGS,Flag.DOWNLOAD.getValue()),
        Flag.DOWNLOAD.getValue(),header.getField(HeaderField.FLAGS));
  }

  @Test
  public void getField() throws Exception {
    int[] expectedValues = {sourcePort,destPort,length,-1, seqNumber,ackNumber,flags,id, offset};
    assertEquals(HeaderField.values().length, expectedValues.length);

    for(int i = 0; i < expectedValues.length; i++) {
      assertEquals(String.format("Value of %s should be %s (Note: -1 means value is not parsed!)",HeaderField.values()[i],expectedValues[i]),
          expectedValues[i],header.getField(HeaderField.values()[i]));
    }

    assertEquals(checksum, header.getCheckSum());
  }

  @Test
  public void getHeader() throws Exception {
    byte[] expected = {0,0,36,81,
        0,0,(byte)168,(byte) 132,
        0,(byte)length,
        0,0,0,0,0,0,0,0,
        0,0,0,13,
        0,0,0,(byte) 255,
        0,
        1,
        0,0,0,0};
    assertArrayEquals(expected, header.getHeader());
  }

}