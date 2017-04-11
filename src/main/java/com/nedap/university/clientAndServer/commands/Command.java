package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Command that client or server has to perform.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public abstract class Command {
  Keyword keyword;
  String description;
  Object parameters;

  public Command(Keyword keyword, String description){
    this.keyword = keyword;
    this.description =description;
  }

  public abstract void execute(Object parameters);

  public Keyword getKeyword() {
    return keyword;
  }

  @Override
  public String toString() {
    return String.format("  %-10s %s", keyword, description);
  }
}
