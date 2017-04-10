package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.commands.Command.Keyword;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class DownloadCommand extends Command{

  public DownloadCommand() {
    super(Keyword.DOWNLOAD, "Download specific file");
  }

  @Override
  public void execute(Object parameters) {
    if(parameters instanceof String) {
      //TODO Download file with given name.
      //Create Udp channel and give it the file, udp should fragment it.
    }
  }

}
