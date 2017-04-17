package com.nedap.university.clientAndServer.commands.helpers;

import static com.nedap.university.fileTranser.UDPPacket.MAX_PAYLOAD;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.fileTranser.ARQProtocol.*;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 17/04/2017.
 */
public class FileSendingHelper {

  public static void downloadFile(FileMetaData metaData, Protocol protocol, String filepath) {
    //Acknowledge to other side that we are ready to receive the file
    protocol.sendAck();

    //Create message digest for file 'proofing'
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return;
    }

    //Save received packets in temporary file.
    UDPPacket packet = protocol.createEmptyPacket();
    String temporaryFileName = filepath + "/tmp_" + metaData.getFileName();
    try (OutputStream fileStream = Files.newOutputStream(Paths.get(temporaryFileName));
        DigestOutputStream digestOutStream = new DigestOutputStream(fileStream, md))
    {
      //Receive first packet
      packet = protocol.receivePacket(0);
      protocol.sendAck();
      System.out.println("Received packet " + packet.getOffset() + " data length " + packet.getData().length);

      //Receive packets until the last packets has arrived.
      while(!packet.isFlagSet(Flag.LAST)) {
        digestOutStream.write(packet.getData());

        packet = protocol.receivePacket(0);
        protocol.sendAck();
        System.out.println("Received packet " + packet.getOffset() + " data length " + packet.getData().length);
      }
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
      return;
    }

    //Check message digest
    byte[] digest = md.digest();
    System.out.println(String.format("Expected digest %s, got %s",
        Utils.binaryArrToHexString(packet.getData()),Utils.binaryArrToHexString(digest)));

    //Rename file
    File file = new File(filepath + "/tmp_" + metaData.getFileName());
    file.renameTo(new File(filepath + "/" + metaData.getFileName()));
  }

  public static FileMetaData retrieveFileMetaData(Command command) {
    UDPPacket firstPacket = null;
    try {
      firstPacket = command.getProtocol().receivePacket(StopAndWaitProtocol.TIMEOUT);
    } catch (IOException|TimeoutException e) {
      command.getHandler().print("Error in " + command.getKeyword() + " request " + e.getMessage());
      command.shutdown();
    }

    FileMetaData metaData = new FileMetaData(firstPacket.getData());
    System.out.println("Start download of " + metaData);

    return metaData;
  }

  public static void uploadFile(File file, int nPackets, Protocol protocol) throws IOException {
    //Create message digest for file 'proofing'
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return;
    }

    //Split file in packets
    try (InputStream fileStream = Files.newInputStream(Paths.get(file.getPath()));
        DigestInputStream digestInputStream = new DigestInputStream(fileStream,md))
    {
      int packetId = 0;

      //Create first packet
      UDPPacket packet = protocol.createEmptyPacket();
      packet.setHeaderSetting(HeaderField.OFFSET,packetId); //Count per MAX_PAYLOAD
      byte[] data = new byte[MAX_PAYLOAD];
      int bytesRead = digestInputStream.read(data);

      //Send packets
      while(bytesRead > 0) {
        ByteBuffer dataTrimmed = ByteBuffer.allocate(bytesRead).put(data, 0, bytesRead);
        packet.setData(dataTrimmed.array());
        System.out.println("Packet length " + packet.getLength() + " data length " + packet.getData().length);

        //Put each packet in the sender buffer
        protocol.addPacketToSendBuffer(packet);

        //Create next packet
        packetId++;
        packet = protocol.createEmptyPacket();
        packet.setHeaderSetting(HeaderField.OFFSET,packetId); //Count per MAX_PAYLOAD
        data = new byte[MAX_PAYLOAD];
        bytesRead = digestInputStream.read(data);
      }
    } catch (IOException e) {
      throw new IOException(e.getMessage());
    }

    //Send EOR packet, with packet md5
    byte[] digest = md.digest();
    System.out.println("Created message digest " + Utils.binaryArrToHexString(digest));
    protocol.sendEndOfRequestPacket(digest);
  }
}
