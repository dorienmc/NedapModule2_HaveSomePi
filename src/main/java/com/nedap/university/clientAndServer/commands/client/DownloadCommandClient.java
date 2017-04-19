package com.nedap.university.clientAndServer.commands.client;

import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.*;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.clientAndServer.commands.client.ListFilesCommandClient;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.ARQProtocol.ProtocolFactory.Name;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class DownloadCommandClient extends Command {

  public DownloadCommandClient(Handler handler, Byte requestId) {
    super(Keyword.DOWNLOAD, "Download specific file", handler, requestId, Name.SLIDING_WINDOW);
  }

  @Override
  public void execute() {
    //List files
    handler.print("Files available for download:");
    (new ListFilesCommandClient(handler,handler.getFreeRequestId())).execute();

    //Ask user for input
    String filename = Utils.readString("Which file do you want to download (enter name or id)? ");

    //Start download timer
    long startTime = System.currentTimeMillis();

    //Create request packet
    protocol.sendRequest(filename.getBytes(), Flag.DOWNLOAD, true);

    //Retrieve file metadata from server
    FileMetaData metaData = retrieveFileMetaData(this);

    //Unpause handler
    handler.unPause();

    //Download requested file and calculate message digest
    byte[] digest = downloadFile(metaData, protocol, handler.getFilePath(), handler);

    //Wait for packet with expected message digest
    try {
      UDPPacket md5Packet = protocol.receivePacket(metaData.getNumberOfPackets() + 1,
          metaData.getNumberOfPackets() * protocol.getTimeOut());

      //Check message digest
      handler.printDebug(String.format("Expected digest %s, got %s",
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

    //Report Statistics
    long endTime = System.currentTimeMillis();
    String downloadLog = String.format("Downloaded %s (%s) using %d packets in %d ms, speed: %5.2f (KB/s)",
        metaData.getFileName(), Utils.getFileSize(metaData.getFileLength()),
        metaData.getNumberOfPackets() + 2,(endTime - startTime),
        Utils.getSpeed(metaData.getNumberOfPackets() + 2, endTime - startTime));
    handler.print(downloadLog);
    handler.getStatistics().addSpeedLog(downloadLog);

    //Rename file (delete old if one exists).
    File file = new File(handler.getFilePath() + "/tmp_" + metaData.getFileName());
    File oldFile = new File(handler.getFilePath() + "/" + metaData.getFileName());
    if(oldFile.exists()) {
      oldFile.delete();
    }
    file.renameTo(new File(handler.getFilePath() + "/" + metaData.getFileName()));

    shutdown();
  }
}