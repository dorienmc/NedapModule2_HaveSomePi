package com.nedap.university.fileTranser;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dorien.meijercluwen on 11/04/2017.
 */
public class FlagTest {
  Flag flag;
  int flags;

  @Before
  public void setup() throws Exception {
    flag = Flag.CONNECT;
  }

  @Test
  public void setFlag() throws Exception {
    for(int i = 0; i < Flag.values().length; i++) {
      flags = 0;
      flags = Flag.setFlag(flag,flags);
      assertEquals(flag.value,flags);
    }
  }

  @Test
  public void isSet() throws Exception {
    for(int i = 0; i < Flag.values().length; i++) {
      for(int j = 0; j < Flag.values().length; j++) {
        if(j < i) {
          assertEquals(String.format("Expected flag %s to be set",Flag.values()[j]), true,Flag.isSet(Flag.values()[j],flags));
        } else {
          assertEquals(String.format("Expected flag %s NOT to be set",Flag.values()[j]),false,Flag.isSet(Flag.values()[j],flags));
        }

      }
      flags = Flag.setFlag(Flag.values()[i],flags);
    }
  }

  @Test
  public void getValue() throws Exception {
    for(int i = 0; i < Flag.values().length; i++) {
      assertEquals((byte) Math.pow(2,7-i), Flag.values()[i].getValue());
    }
  }

  @Test
  public void fromByte() throws Exception {
    for(int i = 0; i < Flag.values().length; i++) {
      Flag currentFlag = Flag.values()[i];
      assertEquals(String.format("Expected %s to correspond to %s",Byte.toString(currentFlag.getValue()),
          currentFlag), currentFlag, Flag.fromByte(currentFlag.getValue()));
      byte multipleFlags = currentFlag.getValue();
      multipleFlags--;
      assertNotEquals(String.format("Expected %s NOT to correspond to %s",Byte.toString(multipleFlags),
          currentFlag), currentFlag, Flag.fromByte(multipleFlags));
    }
  }

}