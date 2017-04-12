package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommand extends Command{

  public UploadCommand() {
    super(Keyword.UPLOAD, "Upload specific file");
  }

  @Override
  public void execute(Handler handler) {
    //TODO get file/data that should be send
  }

}
