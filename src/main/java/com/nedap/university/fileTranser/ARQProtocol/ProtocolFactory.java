package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.Sender;

/**
 * Created by dorien.meijercluwen on 14/04/2017.
 */
public class ProtocolFactory {
  public enum Name {
    DEFAULT, NAIVE, STOP_AND_WAIT, SLIDING_WINDOW;
  }

  /* Create protocol with given name, or a STOP_AND_WAIT protocol when name is DEFAULT. */
  public static Protocol createProtocol(ProtocolFactory.Name name, Sender sender, Receiver receiver, byte requestId, Handler handler) {
    switch (name) {
      case DEFAULT:
      case STOP_AND_WAIT:   return new StopAndWaitProtocol(sender,receiver,requestId, handler);
      case SLIDING_WINDOW:  return new SlidingWindowProtocol(sender,receiver,requestId, handler);
      case NAIVE:           return new NaiveProtocol(sender, receiver, requestId, handler);
      default:              return null;
    }
  }
}
