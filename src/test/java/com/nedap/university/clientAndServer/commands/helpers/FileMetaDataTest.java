package com.nedap.university.clientAndServer.commands.helpers;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dorien.meijercluwen on 15/04/2017.
 */
public class FileMetaDataTest {
  File file;
  FileMetaData metaData;
  FileMetaData metaDataFromArr;
  String filename = "test.md";
  long length = 232086;
  int numberOfPackets = 155;
  byte[] data = {0,0,0,0,0,3,(byte)-118,(byte)-106,  0,0,0,(byte)155,
      (byte)116, (byte)101, (byte)115, (byte)116, (byte)46, (byte)109, (byte)100};

  @Before
  public void setUp() throws Exception {
    file = new File("./files/" + filename);
    metaData = new FileMetaData(file, 1500);
    metaDataFromArr = new FileMetaData(data);
  }

  @Test
  public void getFileName() throws Exception {
    assertEquals(true, filename.equals(metaData.getFileName()));
    assertEquals(true, filename.equals(metaDataFromArr.getFileName()));
  }

  @Test
  public void getFileLength() throws Exception {
    assertEquals(length, metaData.getFileLength());
    assertEquals(length, metaDataFromArr.getFileLength());
  }

  @Test
  public void getNumberOfPackets() throws Exception {
    assertEquals(numberOfPackets, metaData.getNumberOfPackets());
    assertEquals(numberOfPackets, metaDataFromArr.getNumberOfPackets());
  }

  @Test
  public void getData() throws Exception {
    assertArrayEquals(data, metaData.getData());
    assertArrayEquals(data, metaDataFromArr.getData());
  }

}