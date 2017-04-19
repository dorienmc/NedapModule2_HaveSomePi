package com.nedap.university.clientAndServer.commands.helpers;

import static com.nedap.university.fileTranser.UDPPacket.MAX_PAYLOAD;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.fileTranser.ARQProtocol.*;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
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

  /**
   * Downloads the given file from the other side, using the given protocol.
   * @param metaData Metadata about the file that is to be received.
   * @param protocol Protocol used for sending/receiving
   * @param filepath File path to the folder the file should be saved to.
   * @param handler Handler that wants to download, used for logging
   * @return md5 of the downloaded file, or an empty array if the md5 protocol was not found or
   * the file was downloaded only partially.
   */
  public static byte[] downloadFile(FileMetaData metaData, Protocol protocol, String filepath, Handler handler) {
    //Acknowledge to other side that we are ready to receive the file
    protocol.sendAck();

    //Create message digest for file 'proofing'
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      handler.print(e.getMessage());
      return new byte[0];
    }

    //Save received packets in temporary file.
    UDPPacket packet = protocol.createEmptyPacket();
    String temporaryFileName = filepath + "/tmp_" + metaData.getFileName();
    try (OutputStream fileStream = Files.newOutputStream(Paths.get(temporaryFileName));
        DigestOutputStream digestOutStream = new DigestOutputStream(fileStream, md))
    {
      for(int packetId = 0; packetId < metaData.getNumberOfPackets(); packetId++) {
        packet = protocol.receivePacket(packetId + 1, 0);

        if(packet.isFlagSet(Flag.LAST)) {
          throw new IOException(String.format("Warning, expected %d data packets, but got only %d before receiving an EOR packet.",
              metaData.getNumberOfPackets(), packetId));
        }

        protocol.sendAck();
        handler.printDebug("Received packet " + packet.getOffset() + " data length " + packet.getData().length);
        digestOutStream.write(packet.getData());
      }

    } catch (IOException | TimeoutException e) {
      handler.print(e.getMessage());
      return new byte[0];
    }

    return md.digest();
  }

  public static FileMetaData retrieveFileMetaData(Command command) {
    UDPPacket firstPacket = null;
    try {
      firstPacket = command.getProtocol().receivePacket(0);
    } catch (TimeoutException e) {
      command.getHandler().print("Error in " + command.getKeyword() + " request " + e.getMessage());
      command.shutdown();
    }

    FileMetaData metaData = new FileMetaData(firstPacket.getData());
    command.getHandler().print("Start download of " + metaData);

    return metaData;
  }

  /**
   * Uploads given file to the other side, using the given protocol.
   * @param file
   * @param protocol
   * @param handler Handler that wants to download, used for logging
   * @return md5 of the uploaded file
   * @throws IOException When md5 algorithm is not found or reading of file does not work.
   */
  public static byte[] uploadFile(File file, FileMetaData metaData, Protocol protocol, Handler handler) throws IOException {
    //Create message digest for file 'proofing'
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      handler.print(e.getMessage());
      return new byte[0];
    }

    //Split file in packets
    try (InputStream fileStream = Files.newInputStream(Paths.get(file.getPath()));
        DigestInputStream digestInputStream = new DigestInputStream(fileStream,md))
    {
      for(int packetId = 0; packetId < metaData.getNumberOfPackets(); packetId++) {
        //Create packet
        UDPPacket packet = protocol.createEmptyPacket();
        packet.setHeaderSetting(HeaderField.OFFSET,packetId); //Count per MAX_PAYLOAD
        byte[] data = new byte[MAX_PAYLOAD];

        //Read bytes from file and put in packet.
        int bytesRead = digestInputStream.read(data);
        if(bytesRead > 0) {
          ByteBuffer dataTrimmed = ByteBuffer.allocate(bytesRead).put(data, 0, bytesRead);
          packet.setData(dataTrimmed.array());

          //Put each packet in the sender buffer
          protocol.addPacketToSendBuffer(packet);
        }
      }
    } catch (IOException e) {
      throw new IOException(e.getMessage());
    }

    //Return md5
    return md.digest();
  }

  public static void removeCorruptedFile(String fileName) {
    File file = new File(fileName);
    if(file.exists()) {
      file.delete();
    }
  }

  public static boolean isDigestCorrect(byte[] expected, byte[] calculated) {
    return Utils.binaryArrToHexString(expected).equals(Utils.binaryArrToHexString(calculated));
  }
}
