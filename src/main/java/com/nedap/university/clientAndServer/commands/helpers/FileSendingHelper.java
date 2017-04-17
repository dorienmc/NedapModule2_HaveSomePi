package com.nedap.university.clientAndServer.commands.helpers;

import static com.nedap.university.fileTranser.UDPPacket.MAX_PAYLOAD;

import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.fileTranser.ARQProtocol.*;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 17/04/2017.
 */
public class FileSendingHelper {

  /*
  MessageDigest md = MessageDigest.getInstance("MD5");
try (InputStream is = Files.newInputStream(Paths.get("file.txt"));
     DigestInputStream dis = new DigestInputStream(is, md))
{
  /* Read decorated stream (dis) to EOF as normal...
}
  byte[] digest = md.digest();
   */

  public static void downloadFile(FileMetaData metaData, Protocol protocol, String filepath) {
    //Acknowledge to other side that we are ready to receive the file
    protocol.sendAck();

    //Save received packets in temporary file.
    try (FileOutputStream fileStream = new FileOutputStream(
        filepath + "/tmp_" + metaData.getFileName())) {
      for (int packetId = 0; packetId < metaData.getNumberOfPackets(); packetId++) {
        UDPPacket packet = protocol.receivePacket(0);
        protocol.sendAck();

        System.out.println(
            "Received packet " + packet.getOffset() + " data length " + packet.getData().length);

        //Check if not last
        if (!packet.isFlagSet(Flag.LAST)) {
          fileStream.write(packet.getData());
        } else {
          //TODO (check checksum?)
        }
      }
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
      return;
    }

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
    //Split file in packets
    try (FileInputStream fileStream = new FileInputStream(file)) {
      for (int packetId = 0; packetId < nPackets; packetId++) {
        UDPPacket packet = protocol.createEmptyPacket();
        packet.setHeaderSetting(HeaderField.OFFSET,packetId); //Count per MAX_PAYLOAD

        byte[] data = new byte[MAX_PAYLOAD];
        int bytesRead = fileStream.read(data);
        System.out.println(bytesRead);
        if(bytesRead > 0) {
          ByteBuffer dataTrimmed = ByteBuffer.allocate(bytesRead).put(data, 0, bytesRead);
          packet.setData(dataTrimmed.array());
          System.out.println("Packet length " + packet.getLength() + " data length " + packet.getData().length);

          //Put each packet in the sender buffer
          protocol.addPacketToSendBuffer(packet);
        }
      }

      //Send EOR packet
      protocol.sendEndOfRequestPacket();
    } catch (IOException e) {
      throw new IOException(e.getMessage());
    }
  }
}
