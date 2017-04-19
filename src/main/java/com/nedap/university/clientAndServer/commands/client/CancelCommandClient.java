package com.nedap.university.clientAndServer.commands.client;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Client;
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
public class CancelCommandClient extends Command {

  public CancelCommandClient(Handler handler, Byte requestId) {
    super(Keyword.CANCEL, "Abort given command", handler, requestId);
  }

  @Override
  public void execute() {
    handler.print(handler.listRunningCommands());
    int id = Utils.readInt("Choose a command to cancel (enter id number)? ");

    //Find corresponding command and cancel it.
    Command cancelCommand = handler.removeRunningCommand(new Byte((byte)id));
    if(cancelCommand == null) {
      handler.print("Could not find command with id " + id);
      shutdown();
    }

    //Ask server to also cancel the command.
    byte[] data = {(byte) id};
    protocol.sendRequest(data, Flag.CANCEL, true );

    //Shutdown (waits for server ack during shutting down)
    shutdown();
  }

}
