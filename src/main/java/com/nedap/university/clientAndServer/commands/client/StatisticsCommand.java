package com.nedap.university.clientAndServer.commands.client;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class StatisticsCommand extends Command {

  public StatisticsCommand(Handler handler, Byte requestId) {
    super(Keyword.STATS, "Give statistics", handler, requestId);
  }

  @Override
  public void execute() {
    handler.print(handler.getStatistics().toString());
    shutdown(false);
  }

}
