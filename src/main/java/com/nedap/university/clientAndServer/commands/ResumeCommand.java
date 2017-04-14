package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ResumeCommand extends Command{

  public ResumeCommand(Handler handler, Byte requestId) {
    super(Keyword.RESUME, "Resume given download/upload", handler, requestId);
  }

  @Override
  public void execute() {
    //TODO
  }

}
