package com.nedap.university.fileTranser;

import static org.junit.Assert.*;

import java.net.DatagramPacket;
import java.net.InetAddress;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dorien.meijercluwen on 11/04/2017.
 */
public class UDPPacketTest {
  UDPPacket packet;
  int sourcePort = 9297;
  int destPort = 5353;
  int length = 18;
  int checksum = 0;
  int seqNumber = 13;
  int ackNumber = 255;
  int flags = Flag.LIST_FILES.getValue();
  int id = 21;
  int offset = 5;

  @Before
  public void setUp() throws Exception {
    packet = new UDPPacket(sourcePort,destPort,seqNumber,ackNumber,id,offset);
  }

  @Test
  public void testSetup() throws Exception {
    checksum = packet.updateChecksum();
    String textBeforeHeader = "Packet representation in Hex.\n" + "Header: ";
    String textAfterHeader = "\nData: ";
    String expected = "";
    expected += HexToString(sourcePort,4);
    expected += HexToString(destPort,4);
    expected += HexToString(length,4);
    expected += HexToString(checksum,8);
    expected += HexToString(seqNumber,4);
    expected += HexToString(ackNumber,4);
    expected += "00"; //expected += Integer.toHexString(flags); //Not set by constructor
    expected += HexToString(id,2);
    expected += HexToString(offset,4);
    String packetToString = packet.toString();
    String packetHeaderPart = packetToString.substring(textBeforeHeader.length(), packetToString.length() - textAfterHeader.length());

    assertEquals(true, expected.equals(packetHeaderPart.toLowerCase()));
  }

  @Test
  public void flags() throws Exception {
    assertEquals(0, packet.getFlags());

    packet.setFlags(flags);
    assertEquals(flags, packet.getFlags());
    assertEquals(true, packet.isFlagSet(Flag.LIST_FILES));
    assertEquals(false, packet.isFlagSet(Flag.CONNECT));
  }

  @Test
  public void setAndGetData() throws Exception {
    MyUDPHeader header = new MyUDPHeader();
    assertArrayEquals("Packet starts without data", new byte[0], packet.getData());

    //Add data
    byte[] data = {1,2,3,4,5,6,7}; //TODO check checksum
    packet.setData(data);

    //Packet now has the given data
    assertArrayEquals("Packet has given data", data, packet.getData());
    assertEquals(length + data.length, packet.getLength());
  }

  @Test
  public void getSourcePort() throws Exception {
    assertEquals(sourcePort, packet.getSourcePort());
  }

  @Test
  public void getLength() throws Exception {
    MyUDPHeader header = new MyUDPHeader();
    assertEquals("Packet starts with only header",header.getHeaderSize(), packet.getLength());
  }


  @Test
  public void fromDatagram() throws Exception {
    DatagramPacket datagram = new DatagramPacket(new byte[100],100);
    byte[] data = {36,81,20,(byte) 233,0,(byte)length,0,0,0,0,0,13,0,(byte) 255,
        Flag.LIST_FILES.getValue(),0,0,0,1,2,3,4}; //last 4 bytes are UDP data
    byte[] udpData = {1,2,3,4};
    datagram.setData(data);

    packet = new UDPPacket(datagram);
    assertEquals(4, packet.getData().length);
    assertEquals(length, packet.getLength());
    assertEquals(flags, packet.getFlags());
    assertEquals(sourcePort, packet.getSourcePort());
    assertArrayEquals(udpData, packet.getData());
  }

  @Test
  public void toDatagram() throws Exception {
    DatagramPacket expectedDatagram = new DatagramPacket(new byte[100],100);
    byte[] udpData = {1,2,3,4};
    byte[] data = {36,81,20,(byte) 233,0,(byte)(length + udpData.length),0,0,0,0,0,13,0,(byte) 255,
        Flag.LIST_FILES.getValue(),0,0,0,1,2,3,4}; //last 4 bytes are UDP data
    expectedDatagram.setData(data);

    packet = new UDPPacket(expectedDatagram);
    assertArrayEquals(expectedDatagram.getData(), packet.toDatagram(InetAddress.getByName("192.168.40.8")).getData());
  }

  /* Pad string with zeros to given length */
  static String padString(String txt, int length) {
    return String.format("%" + length + "s", txt).replace(' ', '0');
  }

  /* Convert decimal 32bit integer to hexadecimal string, with the given length.
  * Pads the string with zeros at the left side. */
  static String HexToString(int number, int length){
    return padString(Integer.toHexString(number),length);
  }
}