package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class AbortCommand extends Command{

  public AbortCommand() {
    super(Keyword.ABORT, "Abort given command");
  }

  @Override
  public void execute(Handler handler) {
    //TODO
  }

}
