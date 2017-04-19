package com.nedap.university.clientAndServer.commands.client;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import java.util.List;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class HelpCommand extends Command {
  public HelpCommand(Handler handler) {
    super(Keyword.HELP,"Show this menu", handler, new Byte((byte)0));
  }

  @Override
  public void execute() {
    List<Command> commands = handler.getCommands();
    Command exit = null;

    handler.print("--------------");
    handler.print("Commands: ");

    for(Command command : commands) {
      if(command.getKeyword().equals(Keyword.EXIT) ) {
        exit = command;
      } else if (!command.getKeyword().equals(Keyword.HELP)) {
        handler.print(command.getAsMenuItem());
      }
    }

    handler.print(this.getAsMenuItem());
    if(exit != null) {handler.print(exit.getAsMenuItem());}

    handler.print("--------------");
    //handler.unPause();
    shutdown(false);
  }

}
