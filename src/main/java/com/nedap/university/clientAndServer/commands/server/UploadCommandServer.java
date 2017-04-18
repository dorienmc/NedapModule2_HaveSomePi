package com.nedap.university.clientAndServer.commands.server;

import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.*;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.ARQProtocol.ProtocolFactory.Name;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommandServer extends Command {

  public UploadCommandServer(Handler handler, Byte requestId) {
    super(Keyword.UPLOAD, "Upload specific file", handler, requestId, Name.SLIDING_WINDOW);
  }

  @Override
  public void execute() {
    //Retrieve file meta data
    FileMetaData metaData = retrieveFileMetaData(this);

    //Download file and calculate message digest
    byte[] digest = downloadFile(metaData, protocol, handler.getFilePath());

    //Check if message digest was computed
    if(digest.length == 0) {
      handler.print("Warning file corrupted, could not calculate message digest.");
      removeCorruptedFile(handler.getFilePath() + "/tmp_" + metaData.getFileName());
      shutdown();
    }

    //Wait for packet with expected message digest
    try {
      UDPPacket md5Packet = protocol.receivePacket(metaData.getNumberOfPackets() + 1,
          metaData.getNumberOfPackets() * protocol.getTimeOut());

      //Check message digest
      System.out.println(String.format("Expected digest %s, got %s",
          Utils.binaryArrToHexString(md5Packet.getData()),Utils.binaryArrToHexString(digest)));

      //Send ack if message digest was correct
      if(isDigestCorrect(md5Packet.getData(), digest)) {
        handler.print("File md5 correct");
        protocol.sendAck();;
      } else {
        handler.print("File corrupted, message digest incorrect");
        removeCorruptedFile(handler.getFilePath() + "/tmp_" + metaData.getFileName());
        shutdown();
      }
    } catch (TimeoutException e) {
      handler.print("Did not receive md5 packet from client in given time, " + e.getMessage());
      removeCorruptedFile(handler.getFilePath() + "/tmp_" + metaData.getFileName());
      shutdown();
    }

    //Rename file
    File file = new File(handler.getFilePath() + "/tmp_" + metaData.getFileName());
    file.renameTo(new File(handler.getFilePath() + "/" + metaData.getFileName()));

    shutdown();
  }


}


