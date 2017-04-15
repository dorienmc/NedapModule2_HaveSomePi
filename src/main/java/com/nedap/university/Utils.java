package com.nedap.university;

import java.util.Scanner;

/**
 * Methods used by several classes.
 * Created by dorien.meijercluwen on 13/04/2017.
 */
public class Utils {
  static final private char [] lookUpHexAlphabet = new char[16];
  static {
    for(int i = 0; i<10; i++ ) {
      lookUpHexAlphabet[i] = (char)('0'+i);
    }
    for(int i = 10; i<=15; i++ ) {
      lookUpHexAlphabet[i] = (char)('A'+i -10);
    }
  }

  /**
   * Writes a prompt to standard out and tries to read an String value from
   * standard in. This is repeated until an String value is entered.
   * @param prompt the question to prompt the user
   * @return the first String value which is entered by the user
   */
  public static String readString(String prompt) {
    String answer;
    @SuppressWarnings("resource")
    Scanner line = new Scanner(System.in);

    do {
      System.out.print(prompt);
      try (Scanner scannerLine = new Scanner(line.nextLine());) {
        answer = scannerLine.hasNext() ? scannerLine.nextLine() : null;
      }
    } while (answer == null);
    return answer;
  }

  /* Wait for 'waitTime' ms. */
  public static void sleep(int waitTime) {
    //Wait
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /* Pad string with zeros to given length */
  public static String padString(String txt, int length) {
    return String.format("%" + length + "s", txt).replace(' ', '0');
  }

  /* Convert decimal 32bit integer to hexadecimal string, with the given length.
  * Pads the string with zeros at the left side. */
  public static String HexToString(int number, int length){
    return padString(Integer.toHexString(number),length);
  }

  /* Convert decimal 64bit integer to hexadecimal string, with the given length.
  * Pads the string with zeros at the left side. */
  public static String HexToString(long number, int length){
    return padString(Long.toHexString(number),length);
  }

  /**
   * Encode a byte array to hex string
   *
   * @param binaryData array of byte to encode
   * @return return encoded string
   */
  static public String binaryArrToHexString(byte[] binaryData) {
    if (binaryData == null)
      return null;
    int lengthData   = binaryData.length;
    int lengthEncode = lengthData * 2;
    char[] encodedData = new char[lengthEncode];
    int temp;
    for (int i = 0; i < lengthData; i++) {
      temp = binaryData[i];
      if (temp < 0)
        temp += 256;
      encodedData[i*2] = lookUpHexAlphabet[temp >> 4];
      encodedData[i*2+1] = lookUpHexAlphabet[temp & 0xf];
    }
    return new String(encodedData);
  }
}
