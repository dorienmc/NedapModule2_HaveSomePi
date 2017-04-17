package com.nedap.university.clientAndServer.commands;

import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.downloadFile;
import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.retrieveFileMetaData;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.client.ListFilesCommandClient;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.ARQProtocol.StopAndWaitProtocol;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
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
    String filename = Utils.readString("Which file do you want to download (enter name!)? ");

    //Create request packet
    protocol.sendRequest(filename.getBytes(), Flag.DOWNLOAD, true);

    //Retrieve file metadata from server
    FileMetaData metaData = retrieveFileMetaData(this);

    //Download requested file
    downloadFile(metaData, protocol, handler.getFilePath());

    shutdown();
  }
}
