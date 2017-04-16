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
import java.util.concurrent.TimeoutException;

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
    //Retrieve request packet
    try {
      protocol.receivePacket(0);
    } catch (IOException|TimeoutException e) {
      return;
    }

    //List all files
    String allFiles = Utils.listFiles(Server.FILEPATH, "Files");

    //Send this to the client
    protocol.sendData(allFiles.getBytes(), false);

    shutdown();
  }
}
