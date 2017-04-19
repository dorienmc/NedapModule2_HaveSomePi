package com.nedap.university.clientAndServer.commands;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.Flag;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class PauseCommandClient extends Command{

  public PauseCommandClient(Handler handler, Byte requestId) {
    super(Keyword.PAUSE, "Pause specific command", handler, requestId);
  }

  @Override
  public void execute() {
    handler.print(handler.listRunningCommands());
    int id = Utils.readInt("Choose a command to pause (enter id number)? ");

    //Find corresponding command and pause it.
    Command command = handler.getRunningCommand((byte) id);
    if(command == null) {
      handler.print("Could not find a command with id " + id);
      shutdown();
    }

    //Ask server to also pause the corresponding command
    byte[] data = {(byte) id};
    protocol.sendRequest(data, Flag.PAUSE, false);

    //Wait for ack
    try {
      protocol.receivePacket(0);
    } catch (TimeoutException e) {
      handler.print("Did not receive an ack from the server");
    }

    //Now pause command on this side, if possible.
    if(!command.pause()) {
      //Could not pause on this side, so unpause the other side.
      protocol.sendRequest(data, Flag.RESUME, true);
    } else {
      //Send eof request packet
      protocol.sendEndOfRequestPacket();
    }

    shutdown();
  }
}