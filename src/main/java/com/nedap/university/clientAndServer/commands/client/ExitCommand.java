package com.nedap.university.clientAndServer.commands.client;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ExitCommand extends Command {

  public ExitCommand(Handler handler, Byte requestId) {
    super(Keyword.EXIT,"Exit", handler, requestId);
  }

  @Override
  public void execute() {
    //Inform server that we are shutting down. (Eg. a cancel request, but with id -1)
    byte[] data = {(byte) -1};
    protocol.sendRequest(data, Flag.CANCEL, true );

    //Wait for response
    try {
      UDPPacket response = protocol.receivePacket(0);
    } catch (IOException |TimeoutException e) {
      handler.print(e.getMessage());
      return;
    }

    handler.shutdown();
  }
}
