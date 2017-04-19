package com.nedap.university.clientAndServer;

import com.nedap.university.Main;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
  FileOutputStream logFile;

  public enum Status {
    PAUSED, RUNNING, STOPPED;
  }
  private Status status;

  public Handler(int inPort, int outPort, String logPath) {
    this.inPort = inPort;
    this.outPort = outPort;
    this.runningCommands = new HashMap<>();
    this.status = Status.RUNNING;
    try {
      this.logFile = new FileOutputStream(String.format("%s/%s.txt",logPath,
          new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date())));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /********** Setters and getters *********/
  public abstract String getFilePath();

  public abstract String getHostName();

  public void setCommandFactory(CommandFactory commandFactory) {
    this.commandFactory = commandFactory;
  }

  public int getInPort() {
    return inPort;
  }

  public int getOutPort() {
    return outPort;
  }

  public void setInPort(int inPort) {
    this.inPort = inPort;
  }

  public void setOutPort(int outPort) {
    this.outPort = outPort;
  }

  public InetAddress getAddress() {
    return address;
  }

  public void setAddress(InetAddress address) {
    this.address = address;
  }

  public synchronized Status getStatus() {
    return status;
  }

  public synchronized void setStatus(Status status) {
    this.status = status;
  }

  //Unpause handler (currenly only usefull for client)
  public synchronized void unPause() {
    if(getStatus().equals(Status.PAUSED)) {
      setStatus(Status.RUNNING);
    }
  }

  /********** Reliable UDP channel methods *********/
  /**
   * Create a reliable udp channel to 'destAddress' which receives message over port 9292
   * and sends them over 9293 (see ReliableUdpChannel DEFAULT_PORT_IN and DEFAULT_PORT_OUT.
   * @throws SocketException if one of the sockets could not be created.
   */
  public void setChannel(int myPortIn, int myPortOut, InetAddress destAddress,
      int serverPortIn, int serverPortOut) throws SocketException {
    this.channel = new ReliableUdpChannel(myPortIn, myPortOut, destAddress, serverPortIn,
        serverPortOut, this);
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

  /**
  * Handle it when sender or receiver breaks down because it cannot reach the socket.
  **/
  public abstract void handleSocketException (String errorMessage);

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
      setStatus(Status.RUNNING);
    }
  }

  /* Create new command using the given keyword */
  private void startNewCommand(Keyword keyword) {
    Byte requestId = getFreeRequestId();
    startNewCommand(keyword, requestId);
  }

  /* Create new command using the given keyword and requestId */
  public Command startNewCommand(Keyword keyword, Byte requestId) {
    Command command = commandFactory.createCommand(keyword, requestId);
    command.start();
    runningCommands.put(requestId, command);
    printDebug("Started " + command);
    return command;
  }



  /* Get requestId that is not in use */
  public Byte getFreeRequestId() {
    for(byte i = 1; i < 256; i++) {
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
    String result = "--------------\nRunning commands: \n";

    for(Command command: runningCommands.values()) {
      result += command;
      result += "\n";
    }

    result += "--------------";
    return result;
  }

  public Command removeRunningCommand(Byte id) {
    return runningCommands.remove(id);
  }

  public void print(String msg) {
    System.out.println(msg);
    logToLogFile(msg);
  }

  public void printDebug(String msg) {
    if(Main.DEBUG) {
      System.out.println(msg);
    }
    logToLogFile(msg);
  }

  public void logToLogFile(String msg) {
    msg += "\n";
    if(logFile != null) {
      try {
        logFile.write(msg.getBytes());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public abstract void run();

  public abstract void shutdown();
}
