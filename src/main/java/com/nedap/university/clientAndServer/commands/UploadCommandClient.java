package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.Utils;
import java.io.IOException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommandClient extends Command{

  public UploadCommandClient(Handler handler, Byte requestId) {
    super(Keyword.UPLOAD, "Upload specific file", handler, requestId);
  }

  @Override
  public void execute() {
    String filename = Utils.readString("Which file do you want to upload? ");

    try {
      handler.getChannel().uploadFile(filename, getRequestId().byteValue());
    } catch (IOException e) {
      System.out.println("Could not upload " + filename + ", " + e.getMessage());
    }
  }

}
