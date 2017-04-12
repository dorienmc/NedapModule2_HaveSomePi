package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ResumeCommand extends Command{

  public ResumeCommand() {
    super(Keyword.RESUME, "Resume given download/upload");
  }

  @Override
  public void execute(Handler handler) {
    //TODO
  }

}
