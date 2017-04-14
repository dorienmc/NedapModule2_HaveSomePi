package com.nedap.university.clientAndServer;

import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dorien.meijercluwen on 14/04/2017.
 */
public abstract class CommandFactory {
  Map<Keyword,Command> commands;
  protected Handler handler;

  public CommandFactory(Handler handler) {
    commands = new HashMap<>();
    this.handler = handler;
  }

  protected void addCommand(Command command) {commands.put(command.getKeyword(),command);}

  public boolean hasCommand(Keyword keyword) {
    return commands.containsKey(keyword);
  }

  public List<Command> getCommands() {
    return new ArrayList<>(commands.values());
  }

  public abstract Command createCommand(Keyword keyword, Byte requestId);

}
