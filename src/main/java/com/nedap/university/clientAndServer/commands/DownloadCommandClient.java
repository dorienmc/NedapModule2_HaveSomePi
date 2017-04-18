package com.nedap.university.clientAndServer.commands;

import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.*;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.client.ListFilesCommandClient;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class DownloadCommandClient extends Command{

  public DownloadCommandClient(Handler handler, Byte requestId) {
    super(Keyword.DOWNLOAD, "Download specific file", handler, requestId);
  }

  @Override
  public void execute() {
    //List files
    handler.print("Files available for download:");
    (new ListFilesCommandClient(handler,handler.getFreeRequestId())).execute();

    //Ask user for input
    String filename = Utils.readString("Which file do you want to download (enter name or id)? ");

    //Create request packet
    protocol.sendRequest(filename.getBytes(), Flag.DOWNLOAD, true);

    //Retrieve file metadata from server
    FileMetaData metaData = retrieveFileMetaData(this);

    //Download requested file and calculate message digest
    byte[] digest = downloadFile(metaData, protocol, handler.getFilePath());

    //Wait for packet with expected message digest
    try {
      UDPPacket md5Packet = protocol.receivePacket(metaData.getNumberOfPackets() + 1,
          metaData.getNumberOfPackets() * protocol.getTimeOut());

      //Check message digest
      System.out.println(String.format("Expected digest %s, got %s",
          Utils.binaryArrToHexString(md5Packet.getData()),Utils.binaryArrToHexString(digest)));

      //Send EOR packet if message digest was correct
      if(isDigestCorrect(md5Packet.getData(), digest)) {
        handler.print("File md5 correct");
        protocol.sendEndOfRequestPacket();
      } else {
        handler.print("File corrupted, message digest incorrect");
        removeCorruptedFile(handler.getFilePath() + "/tmp_" + metaData.getFileName());
        shutdown();
      }
    } catch (TimeoutException e) {
      handler.print("Did not receive md5 packet from server in given time, " + e.getMessage());
      removeCorruptedFile(handler.getFilePath() + "/tmp_" + metaData.getFileName());
      shutdown();
    }

    //Rename file
    File file = new File(handler.getFilePath() + "/tmp_" + metaData.getFileName());
    file.renameTo(new File(handler.getFilePath() + "/" + metaData.getFileName()));

    shutdown();
  }
}