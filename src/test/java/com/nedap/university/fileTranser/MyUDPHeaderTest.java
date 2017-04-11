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
  int destPort = 5353;
  int length = 16;
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
    int[] expectedValues = {sourcePort,destPort,length,checksum, seqNumber,ackNumber,flags,id, offset};
    assertEquals(HeaderField.values().length, expectedValues.length);

    for(int i = 0; i < expectedValues.length; i++) {
      assertEquals(String.format("Value of %s should be %s",HeaderField.values()[i],expectedValues[i]),
          expectedValues[i],header.getField(HeaderField.values()[i]));
    }
  }

  @Test
  public void getHeader() throws Exception {
    byte[] expected = {36,81,20,(byte) 233,0,16,0,0,0,13,0,(byte) 255,0,1,0,0};
    assertArrayEquals(expected, header.getHeader());
  }

}