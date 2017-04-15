package com.nedap.university.clientAndServer.commands.helpers;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Class to parse/create data for an mDNS packet.
 * Created by dorien.meijercluwen on 13/04/2017.
 */
public class MDNSdata {
  int inPort;
  int outPort;
  String hostname;

  /* Create MDNSdata for mDNS packet. */
  public MDNSdata(int inPort, int outPort, String hostname) {
    this.inPort = inPort;
    this.outPort = outPort;
    this.hostname = hostname;
  }

  /* Create MDNSdata from byte array. */
  public MDNSdata(byte[] data) throws IndexOutOfBoundsException {
    try {
      ByteBuffer buffer = ByteBuffer.allocate(data.length).put(data);
      buffer.rewind();
      inPort = buffer.getInt();
      outPort = buffer.getInt();
      byte[] hostNameInBytes = new byte[data.length - 4*2];
      buffer.get(hostNameInBytes);
      hostname = new String(hostNameInBytes);
    } catch (BufferUnderflowException e) {
      throw new IndexOutOfBoundsException("Could not parse data to mDNS data.");
    }
  }

  public int getInPort() {
    return inPort;
  }

  public int getOutPort() {
    return outPort;
  }

  public String getHostname() {
    return hostname;
  }

  public byte[] getData() {
    byte[] hostNameInBytes = hostname.getBytes();
    ByteBuffer buffer = ByteBuffer.allocate(2 * 4 + hostNameInBytes.length);
    buffer.putInt(inPort);
    buffer.putInt(outPort);
    buffer.put(hostNameInBytes);
    return buffer.array();
  }

  @Override
  public String toString() {
    return String.format("Host %s, ports %d, %d", hostname, inPort, outPort);
  }
}
