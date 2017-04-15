package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.Sender;

/**
 * Created by dorien.meijercluwen on 14/04/2017.
 */
public class ProtocolFactory {
  public enum Name {
    DEFAULT, NAIVE;
  }

  /* Create protocol with given name, or a NAIVE protocol when name is DEFAULT. */
  public static Protocol createProtocol(ProtocolFactory.Name name, Sender sender, Receiver receiver, byte requestId) {
    switch (name) {
      case NAIVE:   return new NaiveProtocol(sender, receiver, requestId);
      case DEFAULT: return new NaiveProtocol(sender, receiver, requestId);
      default:      return null;
    }
  }
}
