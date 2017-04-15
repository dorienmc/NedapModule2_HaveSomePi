package com.nedap.university.clientAndServer.commands.server;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.ClientHandler;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import java.io.File;
import java.io.IOException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ListFilesCommandServer extends Command {

  public ListFilesCommandServer(Handler handler, Byte requestId) {
    super(Keyword.LS, "List files", handler, requestId);
  }

  @Override
  public void execute() {
    if(!(handler instanceof ClientHandler)) {
      return;
    }

    //List all files
    String allFiles = Utils.listFiles(Server.FILEPATH, "Files");

    //Send this to the client
    try {
      protocol.sendData(allFiles.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }

    shutdown();
  }
}
