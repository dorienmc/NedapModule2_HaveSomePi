package com.nedap.university.clientAndServer.commands;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.Flag;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ResumeCommandClient extends Command{

  public ResumeCommandClient(Handler handler, Byte requestId) {
    super(Keyword.RESUME, "Resume given download/upload", handler, requestId);
  }

  @Override
  public void execute() {
    handler.print(handler.listRunningCommands());
    int id = Utils.readInt("Choose a command to resume (enter id number)? ");

    //TODO also resume non-running commands

    //Find corresponding command
    Command command = handler.getRunningCommand((byte) id);
    if(command == null) {
      handler.print("Could not find a command with id " + id);
      shutdown();
    }

    //Ask server to resume the corresponding command
    byte[] data = {(byte) id}; //TODO add more info
    protocol.sendRequest(data, Flag.RESUME, false);

    //Wait for ack
    try {
      protocol.receivePacket(0);
    } catch (TimeoutException e) {
      handler.print("Did not receive an ack from the server");
    }

    //Now resume the command on this side.
    command.unPause();

    //Send eof request packet
    protocol.sendEndOfRequestPacket();

    shutdown();
  }

}
