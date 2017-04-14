package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommandServer extends Command{

  public UploadCommandServer(Handler handler, Byte requestId) {
    super(Keyword.UPLOAD, "Upload specific file", handler, requestId);
  }

  @Override
  public void execute() {
    //Read file meta data
    //TODO from where?


    //TODO figure out how to call protocol.receiveFile
    //Wait until complete file is received
    //Merge packets (or do this in RUDP channel?)
    //Save file
    //TODO get file/data that should be send
  }

}
