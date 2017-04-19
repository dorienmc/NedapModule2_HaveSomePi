package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.UDPPacket;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class PauseCommandServer extends Command{

  public PauseCommandServer(Handler handler, Byte requestId) {
    super(Keyword.PAUSE, "Pause specific command", handler, requestId);
  }

  @Override
  public void execute() {
    //Read pause data
    UDPPacket request = null;
    try {
      request = protocol.receivePacket(0);
    } catch (TimeoutException e) {
      handler.print("Error in " + getKeyword() + " request " + e.getMessage());
    }
    byte id = request.getData()[0];

    //Find specific request and pause it
    Command command = handler.getRunningCommand(id);
    if(command == null) {
      handler.print("Could not find a command with id " + id);
      shutdown();
    }

    //Pause if possible
    if(!command.pause()) {
      shutdown();
    }

    //Ack the packet
    protocol.sendAck();

    //Wait for EOR packet
    UDPPacket endOfRequest = null;
    try {
      endOfRequest = protocol.receivePacket(0);
    } catch (TimeoutException e) {
      handler.print("Error in " + getKeyword() + " request " + e.getMessage());
    }

    shutdown();
  }

}
