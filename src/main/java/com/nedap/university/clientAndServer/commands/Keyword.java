package com.nedap.university.clientAndServer.commands;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public enum Keyword {
  HELP, EXIT, CONNECT, DOWNLOAD, UPLOAD, PAUSE, RESUME, ABORT, LS;

  public static Keyword fromString(String text) {
    for (Keyword k : Keyword.values()) {
      if (k.toString().equalsIgnoreCase(text)) {
        return k;
      }
    }
    return null;
  }
}
