package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.Utils;
import com.nedap.university.fileTranser.Flag;
import java.io.IOException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class DownloadCommandClient extends Command{

  public DownloadCommandClient(Handler handler, Byte requestId) {
    super(Keyword.DOWNLOAD, "Download specific file", handler, requestId);
  }

  @Override
  public void execute() {
    String filename = Utils.readString("Which file do you want to download? ");

    //Create request packet
    try {
      handler.getChannel().sendRequest(filename.getBytes(), Flag.DOWNLOAD);
    } catch (IOException e) {
      handler.print("Could not download " + filename + " " + e.getMessage());
    }


  }

}
