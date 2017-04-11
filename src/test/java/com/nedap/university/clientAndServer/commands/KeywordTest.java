package com.nedap.university.clientAndServer.commands;

import static org.junit.Assert.*;

import java.security.Key;
import org.junit.Test;

/**
 * Created by dorien.meijercluwen on 11/04/2017.
 */
public class KeywordTest {

  @Test
  public void fromString() throws Exception {
    //Wrong keyword
    assertNull("fromString() should return a null object when the input is not a keyword", Keyword.fromString("Not a keyword"));

    //Keyword but only in lower letters
    assertEquals(Keyword.DOWNLOAD, Keyword.fromString("download"));

    //Keyword but only in upper case letters
    assertEquals(Keyword.RESUME, Keyword.fromString("RESUME"));

    //Keyword in both upper and lower case letters
    assertEquals(Keyword.UPLOAD, Keyword.fromString("UpLoAd"));


  }

}