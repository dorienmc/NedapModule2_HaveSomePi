package com.nedap.university.clientAndServer.commands;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommand extends Command{

  public UploadCommand() {
    super(Keyword.UPLOAD, "Upload specific file");
  }

  @Override
  public void execute(Object parameters) {
    if(parameters instanceof String) {//TODO; String with file name or actual file?
      //TODO upload file with given name.
    }
  }

}
