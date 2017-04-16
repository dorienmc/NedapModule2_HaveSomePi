package com.nedap.university.clientAndServer.commands.client;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ARQProtocol.Protocol;
import com.nedap.university.fileTranser.ARQProtocol.ProtocolFactory;
import com.nedap.university.fileTranser.UDPPacket;

/**
 * Command that client or server has to perform.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class ListRunningCommandsCommand extends Command {

  public ListRunningCommandsCommand(Handler handler, Byte requestId) {
    super(Keyword.LSRUNNING, "List Running Commands", handler, requestId);
  }

  @Override
  public void execute() {
    handler.print(handler.listRunningCommands());
    shutdown(false);
  }

  @Override
  public String getInfo() {
    return "Listing commands.";
  }
}