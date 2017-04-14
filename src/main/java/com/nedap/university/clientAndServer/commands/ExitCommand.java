package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.*;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ExitCommand extends Command {

  public ExitCommand(Handler handler, Byte requestId) {
    super(Keyword.EXIT,"Exit", handler, requestId);
  }

  @Override
  public void execute() {
    handler.shutdown();
  }
}
