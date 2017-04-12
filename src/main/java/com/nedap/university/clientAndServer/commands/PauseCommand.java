package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class PauseCommand extends Command{

  public PauseCommand() {
    super(Keyword.PAUSE, "Pause specific download/upload");
  }

  @Override
  public void execute(Handler handler) {
    //TODO
  }

}
