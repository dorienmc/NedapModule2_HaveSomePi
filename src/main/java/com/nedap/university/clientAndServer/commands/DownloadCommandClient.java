package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.clientAndServer.commands.client.ListFilesCommandClient;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.ARQProtocol.StopAndWaitProtocol;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.w3c.dom.ls.LSException;

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
    String filename = Utils.readString("Which file do you want to download (enter name!)? ");

    //Retrieve file metadata from server
    FileMetaData metaData = retrieveFileMetaData(filename);

    //Download requested file
    if(metaData != null) {
      downloadFile(metaData);
    }

    shutdown();
  }

  private FileMetaData retrieveFileMetaData(String filename) {
    //Create request packet
    protocol.sendRequest(filename.getBytes(), Flag.DOWNLOAD, true);

    //Read file meta data
    UDPPacket firstPacket = null;
    try {
      firstPacket = protocol.receivePacket(StopAndWaitProtocol.TIMEOUT);
    } catch (IOException|TimeoutException e) {
      handler.print("Error in " + getKeyword() + " request " + e.getMessage());
      return null;
    }

    FileMetaData metaData = new FileMetaData(firstPacket.getData());
    System.out.println("Start download of " + metaData);

    return metaData;
  }

  //TODO Retrieve parts of file from buffer (in order!)
  private void downloadFile(FileMetaData metaData) {
    //Acknowledge to server that we are ready to receive the file
    protocol.sendAck();

    //Save received packets in temporary file.
    try (FileOutputStream fileStream = new FileOutputStream(
        Client.FILEPATH + "/tmp_" + metaData.getFileName())) {
      for (int packetId = 0; packetId < metaData.getNumberOfPackets(); packetId++) {
        //TODO or use while(packet.isFlagSet(Flag.LAST) ?
        //Receive next packet //TODO require specific packet (specific seq.number? specific offset!)
        UDPPacket packet = protocol.receivePacket(0);
        protocol.sendAck();

        System.out.println("Received packet " + packet.getOffset());

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
    File file = new File(Client.FILEPATH + "/tmp_" + metaData.getFileName());
    file.renameTo(new File(Client.FILEPATH + "/" + metaData.getFileName()));
  }
}
