package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import java.io.IOException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ListFilesCommandClient extends Command{

  public ListFilesCommandClient() {
    super(Keyword.LS, "List files");
  }

  @Override
  public void execute(Handler handler) {
    if(!(handler instanceof Client)) {
      return;
    }

    ReliableUdpChannel channel = handler.getChannel();

    if(channel != null) {
      //Send request and wait for response.
      String response = null;
      try {
        response = new String(channel.sendAndReceive(Keyword.LS));
      } catch (IOException e) {
        handler.print(e.getMessage());
      }

      //Print files
      handler.print(response);
    }

  }

}
