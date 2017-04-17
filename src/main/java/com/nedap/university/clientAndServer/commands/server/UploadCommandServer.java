package com.nedap.university.clientAndServer.commands.server;

import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.downloadFile;
import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.retrieveFileMetaData;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.ARQProtocol.StopAndWaitProtocol;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommandServer extends Command {

  public UploadCommandServer(Handler handler, Byte requestId) {
    super(Keyword.UPLOAD, "Upload specific file", handler, requestId);
  }

  @Override
  public void execute() {
    //Retrieve file meta data
    FileMetaData metaData = retrieveFileMetaData(this);

    downloadFile(metaData, protocol, handler.getFilePath());

    shutdown();
  }
}
