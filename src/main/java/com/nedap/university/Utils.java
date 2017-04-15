package com.nedap.university;

import java.util.Scanner;

/**
 * Methods used by several classes.
 * Created by dorien.meijercluwen on 13/04/2017.
 */
public class Utils {

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
}
