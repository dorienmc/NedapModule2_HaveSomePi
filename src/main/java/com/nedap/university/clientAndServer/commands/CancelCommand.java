package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class CancelCommand extends Command{

  public CancelCommand(Handler handler, Byte requestId) {
    super(Keyword.CANCEL, "Abort given command", handler, requestId);
  }

  @Override
  public void execute() {
    //TODO
  }

}
