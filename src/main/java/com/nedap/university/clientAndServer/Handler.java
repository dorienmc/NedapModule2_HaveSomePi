package com.nedap.university.clientAndServer;

import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for peer handling, used by client and clienthandler.
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public abstract class Handler extends Thread {
  ReliableUdpChannel channel;
  CommandFactory commandFactory;
  int inPort;
  int outPort;
  InetAddress address;
  Map<Byte,Command> runningCommands;

  public Handler(int inPort, int outPort) {
    this.inPort = inPort;
    this.outPort = outPort;
  }

  /********** Setters and getters *********/
  public void setCommandFactory(CommandFactory commandFactory) {
    this.commandFactory = commandFactory;
  }

  //TODO remove this?
//  public void setInPort(int inPort) {
//    this.inPort = inPort;
//  }
//
//  public void setOutPort(int outPort) {
//    this.outPort = outPort;
//  }

  public int getInPort() {
    return inPort;
  }

  public int getOutPort() {
    return outPort;
  }

  public InetAddress getAddress() {
    return address;
  }

  public void setAddress(InetAddress address) {
    this.address = address;
  }


  /********** Reliable UDP channel methods *********/
  /**
   * Create a reliable udp channel to 'destAddress' which receives message over port 9292
   * and sends them over 9293 (see ReliableUdpChannel DEFAULT_PORT_IN and DEFAULT_PORT_OUT.
   * @throws SocketException if one of the sockets could not be created.
   */
  public void setChannel(int myPortIn, int myPortOut, InetAddress destAddress,
      int serverPortIn, int serverPortOut, boolean isClient) throws SocketException {
    this.channel = new ReliableUdpChannel(myPortIn, myPortOut, destAddress, serverPortIn, serverPortOut, isClient);
  }

  public void removeChannel() {
    if(channel != null) {
      channel.shutdown();
    }
    channel = null;
  }

  public ReliableUdpChannel getChannel() {
    return channel;
  }

  /********** Command methods *********/
  public void handleCommand(String input) {
    Keyword keyword = Keyword.fromString(input);
    handleCommand(keyword);
  }

  public void handleCommand(Keyword keyword) {
    if(keyword != null && commandFactory.hasCommand(keyword)) {
      startNewCommand(keyword);
    } else {
      print(String.format("WARNING keyword %s is unknown", keyword));
    }
  }

  /* Create new command using the given keyword */
  private void startNewCommand(Keyword keyword) {
    Byte requestId = getFreeRequestId();
    startNewCommand(keyword, requestId);
  }

  /* Create new command using the given keyword and requestId */
  public void startNewCommand(Keyword keyword, Byte requestId) {
    Command command = commandFactory.createCommand(keyword, requestId);
    command.start();
    runningCommands.put(requestId, command);
  }



  /* Get requestId that is not in use */
  public Byte getFreeRequestId() {
    for(byte i = 0; i < 256; i++) {
      if(!runningCommands.containsKey(new Byte(i))) {
        return new Byte(i);
      }
    }
    return null;
  }

  public List<Command> getCommands() {
    return commandFactory.getCommands();
  }

  public String listRunningCommands() {
    String result = "--------------\nRunning commands: ";

    //for(Map.Entry<Byte, Command> entry: runningCommands.entrySet()) {
    for(Command command: runningCommands.values()) {
      result += command.requestToString();
      result += "\n";
    }

    result += "--------------";
    return result;
  }

  public Command getRunningCommand(byte requestId) {
    return runningCommands.get(new Byte(requestId));
  }




  public void print(String msg) {
    System.out.println(msg);
  }

  public abstract void run();

  public abstract void shutdown();
}
