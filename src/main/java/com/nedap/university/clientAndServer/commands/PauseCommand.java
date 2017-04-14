package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class PauseCommand extends Command{

  public PauseCommand(Handler handler, Byte requestId) {
    super(Keyword.PAUSE, "Pause specific download/upload", handler, requestId);
  }

  @Override
  public void execute() {
    //TODO
  }

}
