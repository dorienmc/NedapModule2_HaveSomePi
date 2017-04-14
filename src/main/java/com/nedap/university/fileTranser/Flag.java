package com.nedap.university.fileTranser;

import com.nedap.university.clientAndServer.commands.Keyword;

/**
 * Possible flags
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public enum Flag {
  CONNECT(128), LIST_FILES(64),DOWNLOAD(32), UPLOAD(16), PAUSE(8),RESUME(4), CANCEL(2),NOT_LAST(1);
  byte value;

  Flag(int value) {
    this.value = (byte) value;
  }

  public static int setFlag(Flag flag, int controlFlags){
    if(!isSet(flag, controlFlags)) {
      controlFlags += flag.value;
    }
    return controlFlags;
  }

  public static boolean isSet(Flag flag, int controlFlags){
    return (controlFlags & flag.value) != 0;
  }

  public byte getValue() {
    return this.value;
  }

  /*
  * Returns corresponding Flag if the byte corresponds exactly to that flag value.
  */
  public static Flag fromByte(byte value) {
    for(Flag f: Flag.values()) {
      if(f.value == value) {
        return f;
      }
    }
    return null;
  }

  /* Convert to keyword, return null if not possible. */
  public Keyword toKeyword() {
    //HELP, EXIT, CONNECT, DOWNLOAD, UPLOAD, PAUSE, RESUME, CANCEL, LS;
    switch (this) {
      case CONNECT:     return Keyword.CONNECT;
      case LIST_FILES:  return Keyword.LS;
      case DOWNLOAD:    return Keyword.DOWNLOAD;
      case UPLOAD:      return Keyword.UPLOAD;
      case PAUSE:       return Keyword.PAUSE;
      case RESUME:      return Keyword.RESUME;
      case CANCEL:       return Keyword.CANCEL;
      default:          return null;
    }
  }

}