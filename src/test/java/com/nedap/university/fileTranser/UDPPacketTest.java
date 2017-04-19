package com.nedap.university.fileTranser;

import static com.nedap.university.fileTranser.UDPPacket.MAX_PACKET_SIZE;
import static com.nedap.university.fileTranser.UDPPacket.MAX_PAYLOAD;
import static org.junit.Assert.*;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dorien.meijercluwen on 11/04/2017.
 */
public class UDPPacketTest {
  UDPPacket packet;
  UDPPacket packetFromDataGram;
  int sourcePort = 9297;
  int destPort = 5353;
  long checksum = 0;
  int seqNumber = 13;
  int ackNumber = 255;
  int flags = Flag.LIST_FILES.getValue();
  int id = 21;
  int offset = 5;
  byte[] udpData = {1,2,3,4};
  int length = HeaderField.getTotalLength() + udpData.length;
  byte[] data;
  MyUDPHeader header;

  @Before
  public void setUp() throws Exception {
    //Create normal packet
    packet = new UDPPacket(sourcePort,destPort,seqNumber,ackNumber,id,offset);
    packet.setFlags(flags);
    packet.setData(udpData);
    checksum = packet.getChecksum();

    //Set header and data (header + udpdata) variables
    header = new MyUDPHeader(sourcePort,destPort,seqNumber,ackNumber,id,offset);
    header.setField(HeaderField.LENGTH, length);
    header.setField(HeaderField.FLAGS, flags);
    header.setCheckSum(checksum);
    data = new byte[length];
    System.arraycopy(header.getHeader(),0,data,0,header.getHeaderSize());
    System.arraycopy(udpData,0,data,header.getHeaderSize(),udpData.length);

    //Create packets
    DatagramPacket datagram = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
    datagram.setData(data);
    packetFromDataGram = new UDPPacket(datagram);
  }

  @Test
  public void flags() throws Exception {
    assertEquals(flags, packet.getFlags());
    assertEquals(true, packet.isFlagSet(Flag.LIST_FILES));
    assertEquals(false, packet.isFlagSet(Flag.CONNECT));

    packetFromDataGram.setFlags(flags);
    assertEquals(flags, packetFromDataGram.getFlags());
    assertEquals(true, packetFromDataGram.isFlagSet(Flag.LIST_FILES));
    assertEquals(false, packetFromDataGram.isFlagSet(Flag.CONNECT));
  }

  @Test
  public void setAndGetData() throws Exception {
    MyUDPHeader header = new MyUDPHeader();
    assertArrayEquals(udpData, packet.getData());
    assertArrayEquals(udpData, packetFromDataGram.getData());

    //Change data
    byte[] data = {1,2,3,4,5,6,7};
    packet.setData(data);
    packetFromDataGram.setData(data);

    //Packet now has the given data
    assertArrayEquals("Packet has given data", data, packet.getData());
    assertEquals(header.getHeaderSize() + data.length, packet.getLength());

    assertArrayEquals("Packet from datagram has given data", data, packetFromDataGram.getData());
    assertEquals(header.getHeaderSize() + data.length, packetFromDataGram.getLength());
  }

  @Test
  public void getSourcePort() throws Exception {
    assertEquals(sourcePort, packet.getSourcePort());
    assertEquals(sourcePort, packetFromDataGram.getSourcePort());
  }

  @Test
  public void getLength() throws Exception {
    MyUDPHeader header = new MyUDPHeader();
    assertEquals(length, packet.getLength());
    assertEquals(length, packetFromDataGram.getLength());
  }

  @Test
  public void getDestPort() throws Exception {
    assertEquals(destPort, packet.getDestPort());
    assertEquals(destPort, packetFromDataGram.getDestPort());
  }

  @Test
  public void getSequenceNumber() throws Exception {
    assertEquals(seqNumber, packet.getSequenceNumber());
    assertEquals(seqNumber, packetFromDataGram.getSequenceNumber());
  }

  @Test
  public void getAckNumber() throws Exception {
    assertEquals(ackNumber, packet.getAckNumber());
    assertEquals(ackNumber, packetFromDataGram.getAckNumber());
  }

  @Test
  public void getRequestId() throws Exception {
    assertEquals(id, packet.getRequestId());
    assertEquals(id, packetFromDataGram.getRequestId());
  }

  @Test
  public void getOffset() throws Exception {
    assertEquals(offset, packet.getOffset());
    assertEquals(offset, packetFromDataGram.getOffset());
  }

  @Test
  public void getPkt() throws Exception {
    packet.setData(udpData);
    assertArrayEquals(data, packet.getPkt());
    assertArrayEquals(data, packetFromDataGram.getPkt());
  }


  @Test
  public void isValid() throws Exception {
    assertEquals(true, packet.isValid(sourcePort,destPort));
    assertEquals(true, packetFromDataGram.isValid(sourcePort,destPort));

  }

  @Test
  public void fromDatagram() throws Exception {
    assertEquals(udpData.length, packetFromDataGram.getData().length);
    assertEquals(length, packetFromDataGram.getLength());
    assertEquals(flags, packetFromDataGram.getFlags());
    assertEquals(sourcePort, packetFromDataGram.getSourcePort());
    assertArrayEquals(udpData, packetFromDataGram.getData());

    //Adding a big packet appended with zeros, leaves only a packet with length 'length'
    byte[] bigData = new byte[MAX_PAYLOAD];
    Arrays.fill(bigData,(byte)0);
    System.arraycopy(data,0,bigData,0,data.length);
    DatagramPacket datagram = new DatagramPacket(new byte[MAX_PACKET_SIZE],
        MAX_PACKET_SIZE);
    datagram.setData(bigData);

    packet = new UDPPacket(datagram);
    assertEquals(length,packet.getLength());
    assertArrayEquals(udpData, packet.getData());

  }

  @Test
  public void toDatagram() throws Exception {
    DatagramPacket expectedDatagram = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
    expectedDatagram.setData(data);

    packet = new UDPPacket(expectedDatagram);
    assertArrayEquals(expectedDatagram.getData(), packet.toDatagram(InetAddress.getByName("192.168.40.8")).getData());
  }

  /* Test checksum */
  @Test
  public void checkSum() throws Exception {
    assertEquals(packet.getChecksum(), packet.calculateChecksum());
    byte[] udpData = {1,2,3,4};
    packet.setData(udpData);
    assertEquals(packet.getChecksum(), packet.calculateChecksum());
    packet.setFlags(Flag.CONNECT.getValue());
    assertEquals(packet.getChecksum(), packet.calculateChecksum());

    assertEquals(packet.getChecksum(), packet.calculateChecksum());
    packet.setData(udpData);
    assertEquals(packet.getChecksum(), packet.calculateChecksum());
    packet.setFlags(Flag.CONNECT.getValue());
    assertEquals(packet.getChecksum(), packet.calculateChecksum());

  }

  /* Test checksum with a file */
  @Test
  public void checkSumFile() throws Exception {
    File file = new File(Client.FILEPATH + "/" + "test_b.txt");
    FileMetaData metaData = new FileMetaData(file, MAX_PAYLOAD);

    try (FileInputStream fileStream = new FileInputStream(file)) {

      for (int packetId = 0; packetId < metaData.getNumberOfPackets(); packetId++) {
        packet = new UDPPacket(9002, 8000, packetId + 1, packetId + 1, 4, 0);
        packet.setHeaderSetting(HeaderField.OFFSET, packetId); //Count per MAX_PAYLOAD

        byte[] data = new byte[MAX_PAYLOAD];
        fileStream.read(data);
        packet.setData(data);

        assertEquals(packet.getChecksum(), packet.calculateChecksum());

        DatagramPacket datagram = packet.toDatagram(InetAddress.getByName("192.168.40.8"));
        packetFromDataGram = new UDPPacket(datagram);
        assertEquals(packetFromDataGram.getChecksum(), packetFromDataGram.calculateChecksum());


      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }


  }
}