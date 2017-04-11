package com.nedap.university.fileTranser;

/**
 * Possible flags
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public enum Flag {
  CONNECT(128), LIST_FILES(64),DOWNLOAD(32), UPLOAD(16), PAUSE(8),RESUME(4),ABORT(2),NOT_LAST(1);
  byte value;

  Flag(int value) {
    this.value = (byte) value;
  }

  public static byte setFlag(Flag flag, byte controlFlags){
    if(!isSet(flag, controlFlags)) {
      controlFlags += flag.value;
    }
    return controlFlags;
  }

  public static boolean isSet(Flag flag, byte controlFlags){
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

}