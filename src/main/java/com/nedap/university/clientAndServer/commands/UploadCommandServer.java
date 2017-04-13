package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommandServer extends Command{

  public UploadCommandServer() {
    super(Keyword.UPLOAD, "Upload specific file");
  }

  @Override
  public void execute(Handler handler) {
    //Read file meta data
    //Wait until complete file is received
    //Merge packets (or do this in RUDP channel?)
    //Save file
    //TODO get file/data that should be send
  }

}
