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
  public void execute(Object parameters) {
    if(parameters instanceof Client) {
      ((Client) parameters).shutdown();
    } else if(parameters instanceof Server) {
      ((Server) parameters).shutdown();
    } else if(parameters instanceof ClientHandler) {
      ((ClientHandler) parameters).shutdown();
    }
  }
}
