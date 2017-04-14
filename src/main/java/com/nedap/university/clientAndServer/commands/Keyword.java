package com.nedap.university.clientAndServer.commands;

import com.nedap.university.fileTranser.Flag;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public enum Keyword {
  HELP, EXIT, CONNECT, DOWNLOAD, UPLOAD, PAUSE, RESUME, CANCEL, LS;

  public static Keyword fromString(String text) {
    for (Keyword k : Keyword.values()) {
      if (k.toString().equalsIgnoreCase(text)) {
        return k;
      }
    }
    return null;
  }

  /* Parse keyword from flags (int) value, returns null if not possible.
  * Note: only 1 flag can be set!
  */
  public static Keyword fromFlags(int flags) {
    Keyword keyword = null;
    Flag flag = Flag.fromByte((byte) flags);
    if(flag != null) {
      keyword = flag.toKeyword();
    }
    return keyword;
  }

  /* Convert keyword to flag, return null if not possible */
  public Flag toFlag() {
    //CONNECT(128), LIST_FILES(64),DOWNLOAD(32), UPLOAD(16), PAUSE(8),RESUME(4),CANCEL(2),NOT_LAST(1);
    switch (this) {
      case CONNECT:   return Flag.CONNECT;
      case DOWNLOAD:  return Flag.DOWNLOAD;
      case UPLOAD:    return Flag.UPLOAD;
      case PAUSE:     return Flag.PAUSE;
      case RESUME:    return Flag.RESUME;
      case CANCEL:    return Flag.CANCEL;
      case LS:        return Flag.LIST_FILES;
      default:        return null;
    }

  }
}
