package com.nedap.university.clientAndServer.commands.client;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ListFilesCommandClient extends Command {

  public ListFilesCommandClient(Handler handler, Byte requestId) {
    super(Keyword.LS, "List files",handler, requestId);
  }

  @Override
  public void execute() {
    if(!(handler instanceof Client)) {
      return;
    }

    //Send request
    try {
      protocol.sendRequest(Keyword.LS);
    } catch (IOException e) {
      handler.print(e.getMessage());
      deregisterFromChannel();
    }

    //Wait for response
    UDPPacket response = null; //TODO let protocol determine timeout?
    try {
      response = protocol.receivePacket(0);
    } catch (IOException|TimeoutException e) {
      handler.print(e.getMessage());
      return;
    }

    //Print files
    handler.print(new String(response.getData()));

    shutdown();
  }

}
