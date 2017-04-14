package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;

/**
 * Command that client or server has to perform.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public abstract class Command extends Thread {
  Keyword keyword;
  String description;
  Handler handler;
  Byte requestId;

  public Command(Keyword keyword, String description, Handler handler, Byte requestId){
    this.keyword = keyword;
    this.description =description;
    this.handler = handler;
    this.requestId = requestId;
  }

  public void run(){
    execute();
  }

  public abstract void execute();

  public Keyword getKeyword() {
    return keyword;
  }

  public Byte getRequestId() {
    return requestId;
  }

  @Override
  public String toString() {
    return String.format("  %-10s %s", keyword, description);
  }

  public String requestToString() { return String.format("  %-10s %s", requestId.intValue(), this.getClass().getName());}
}
