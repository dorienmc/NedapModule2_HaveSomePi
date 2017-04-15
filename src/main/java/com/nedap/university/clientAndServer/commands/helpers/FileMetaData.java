package com.nedap.university.clientAndServer.commands.helpers;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Created by dorien.meijercluwen on 15/04/2017.
 */
public class FileMetaData {
  private static final int MIN_SIZE = 4 + 8;
  private long fileLength;
  private int numberOfPackets;
  private String fileName;

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
    fileLength = buffer.getLong();
    numberOfPackets = buffer.getInt();
    byte[] fileNameInBytes = new byte[buffer.capacity() - MIN_SIZE];
    buffer.get(fileNameInBytes);
    fileName = new String(fileNameInBytes);
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
    byte[] fileNameInBytes = fileName.getBytes();
    ByteBuffer buffer = ByteBuffer.allocate(fileNameInBytes.length + MIN_SIZE);
    buffer.putLong(fileLength);
    buffer.putInt(numberOfPackets);
    buffer.put(fileNameInBytes);
    return buffer.array();
  }

  @Override
  public String toString() {
    return String.format("%s, length: %d, #packets:%d", fileName, fileLength, numberOfPackets);
  }
}
