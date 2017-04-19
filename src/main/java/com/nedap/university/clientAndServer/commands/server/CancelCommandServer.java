package com.nedap.university.clientAndServer.commands.server;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ARQProtocol.StopAndWaitProtocol;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class CancelCommandServer extends Command {

  public CancelCommandServer(Handler handler, Byte requestId) {
    super(Keyword.CANCEL, "Abort given command", handler, requestId);
  }

  @Override
  public void execute() {
    //Read cancel data
    UDPPacket request = null;
    try {
      request = protocol.receivePacket(0);
    } catch (TimeoutException e) {
      handler.print("Error in " + getKeyword() + " request " + e.getMessage());
    }
    byte id = request.getData()[0];

    //If the id is not -1 (cancel complete connection), cancel the corresponding request.
    //If its -1 do not stop the clienthandler otherwise we cannot receive the EOR packet,
    handler.print("Canceled " + handler.removeRunningCommand(id));
    //TODO log where the command was? So we can restart it?

    //Ack the packet
    protocol.sendAck();

    //Wait for EOR packet
    UDPPacket endOfRequest = null;
    try {
      endOfRequest = protocol.receivePacket(0);
    } catch (TimeoutException e) {
      handler.print("Error in " + getKeyword() + " request " + e.getMessage());
    }

    if(endOfRequest.isFlagSet(Flag.LAST)) {
      //If id == -1 the complete ClientHandler should be stopped
      if (id == ((byte) -1)) {
        handler.shutdown();
        shutdown();
      }
    } else {
      handler.print("Error expected EOR packet.");
    }

    shutdown();
  }

}
