package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Utils;
import com.nedap.university.fileTranser.Flag;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommandClient extends Command{

  public UploadCommandClient() {
    super(Keyword.UPLOAD, "Upload specific file");
  }

  @Override
  public void execute(Handler handler) {
    String filename = Utils.readString("Which file do you want to upload? ");

    try {
      handler.getChannel().uploadFile(filename);
    } catch (IOException e) {
      System.out.println("Could not upload " + filename + ", " + e.getMessage());
    }
  }

}
