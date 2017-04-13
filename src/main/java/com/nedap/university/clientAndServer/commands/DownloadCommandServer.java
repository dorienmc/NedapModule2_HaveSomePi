package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class DownloadCommandServer extends Command{

  public DownloadCommandServer() {
    super(Keyword.DOWNLOAD, "Download specific file");
  }

  @Override
  public void execute(Handler handler) {
    //TODO Download file with given name.
    //Create Udp channel and give it the file, udp should fragment it.
  }

}
