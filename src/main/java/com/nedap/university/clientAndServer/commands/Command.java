package com.nedap.university.clientAndServer.commands;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.ARQProtocol.*;
import com.nedap.university.fileTranser.UDPPacket;

/**
 * Command that client or server has to perform.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public abstract class Command extends Thread {
  Keyword keyword;
  String description;
  protected Handler handler;
  protected Protocol protocol;
  Byte requestId;

  public Command(Keyword keyword, String description, Handler handler, Byte requestId){
    this.keyword = keyword;
    this.description =description;
    this.handler = handler;
    this.requestId = requestId;
    registerToChannel(ProtocolFactory.Name.NAIVE);
  }

  /********** Methods to (de)register to a Reliable UDP channel ********/
  /**
   *  Registers to the Reliable UDP channel, if it is set.
   *  And instantiates a protocol with the given protocolName.
   **/
  public void registerToChannel(ProtocolFactory.Name protocolName) {
    if(handler.getChannel() != null) {
      handler.getChannel().register(this, protocolName);
    }
  }

  /* Deregister from channel, for when you are done. */
  public void deregisterFromChannel() {
    if(handler.getChannel() != null) {
      handler.getChannel().deregister(this);
    }
  }

  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  /******** Methods called by Sender and Receiver (of the Reliable UDP Channel) to which this Command is registered. **********/
  /**
   * Return next packet that should be send. Or null if there are none currently.
   * Internally update the seq., add it to unacked, create timer etc.
   **/
  public UDPPacket getNextPacket() {
    if(protocol != null) {
      return protocol.getNextPacket();
    } else {
      return null;
    }
  }

  /**
   * Add new packet to the request. Protocol can decide to drop it or not.
   */
  public void addPacketToReceiveBuffer(UDPPacket packet) {
    //Let protocol decide if packet is expected (eg. sequence number could not confirm to sliding window)
    if(protocol.isExpected(packet)) {
      protocol.addPacketToBuffer(packet);
    } else {
      //Drop packet.
      //TODO list ignored packets in statistics.
    }
  }



  /********** Command methods ********/
  public void run(){
    execute();
  }

  public abstract void execute();

  public Keyword getKeyword() {
    return keyword;
  }

  public Byte getRequestId() {
    return requestId;
  }

  @Override
  public String toString(){ return String.format("  %-10s %s", requestId.intValue(), this.getClass().getName());}

  public String getAsMenuItem()  {
    return String.format("  %-10s %s", keyword, description);
  }
}
