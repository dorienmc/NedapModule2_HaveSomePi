package com.nedap.university.clientAndServer.commands;

import java.text.ParseException;
import java.util.List;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class HelpCommand extends Command {
  public HelpCommand() {
    super(Keyword.HELP,"Show this menu");
  }

  @Override
  public void execute(Object parameters){
    if(parameters instanceof List && ((List) parameters).get(0) instanceof Command) {
      List<Command> commands = (List) parameters;
      Command exit = null;

      System.out.println("--------------");
      System.out.println("Commands: ");

      for(Command command : commands) {
        if(command.getKeyword().equals(Keyword.EXIT) ) {
          exit = command;
        } else if (!command.getKeyword().equals(Keyword.HELP)) {
          System.out.println(command);
        }
      }

      System.out.println(this);
      if(exit != null) {System.out.println(exit);}

      System.out.println("--------------");

    }


  }

}
