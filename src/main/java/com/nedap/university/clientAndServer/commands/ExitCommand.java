package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.*;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ExitCommand extends Command {

  public ExitCommand() {
    super(Keyword.EXIT,"Exit");
  }

  @Override
  public void execute(Handler handler) {
    handler.shutdown();
  }
}
