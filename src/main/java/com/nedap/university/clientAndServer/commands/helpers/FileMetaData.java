package com.nedap.university.clientAndServer.commands.helpers;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Created by dorien.meijercluwen on 15/04/2017.
 */
public class FileMetaData {
  private static final int MIN_SIZE = 4 + 8;
  String fileName;
  long fileLength;
  int numberOfPackets;

  public FileMetaData(File file, int maxPacketSize) {
    fileName = file.getName();
    fileLength = file.length();
    numberOfPackets = (int)(Math.ceil(file.length() / (double) maxPacketSize));
  }

  public FileMetaData(byte[] data) throws IndexOutOfBoundsException {
    if (data.length < MIN_SIZE) {
      throw new IndexOutOfBoundsException("Input to small, need at least " + MIN_SIZE +" bytes.");
    }
    ByteBuffer buffer = ByteBuffer.allocate(data.length).put(data);
    buffer.rewind();
    byte[] fileNameInBytes = new byte[buffer.capacity() - MIN_SIZE];
    buffer.get(fileNameInBytes);
    fileName = fileNameInBytes.toString();
    fileLength = buffer.getLong();
    numberOfPackets = buffer.getInt();
  }

  public String getFileName() {
    return fileName;
  }

  public long getFileLength() {
    return fileLength;
  }


  public int getNumberOfPackets() {
    return numberOfPackets;
  }

  public byte[] getData() {
    ByteBuffer buffer = ByteBuffer.allocate(fileName.getBytes().length + MIN_SIZE);
    buffer.put(fileName.getBytes());
    buffer.putLong(fileLength);
    buffer.putInt(numberOfPackets);
    return buffer.array();
  }
}
