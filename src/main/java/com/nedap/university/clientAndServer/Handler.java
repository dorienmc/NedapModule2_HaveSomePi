package com.nedap.university.clientAndServer;

import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for peer handling, used by client and clienthandler.
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public abstract class Handler extends Thread {
  ReliableUdpChannel channel;
  Map<Keyword,Command> commands;
  int inPort;
  int outPort;
  InetAddress address;

  public Handler(int inPort, int outPort) {
    this.inPort = inPort;
    this.outPort = outPort;
    commands = new HashMap<>();
  }

  public void setInPort(int inPort) {
    this.inPort = inPort;
  }

  public void setOutPort(int outPort) {
    this.outPort = outPort;
  }

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

  public void addCommand(Command command) {commands.put(command.getKeyword(),command);}

  public void handleCommand(String input) {
    Keyword keyword = Keyword.fromString(input);
    handleCommand(keyword);
  }

  public void handleCommand(Keyword keyword) {
    if(keyword != null && commands.containsKey(keyword)) {
      commands.get(keyword).execute(this);
    } else {
      print(String.format("WARNING keyword %s is unknown", keyword));
    }
  }

  public List<Command> getCommands() {
    return new ArrayList<>(commands.values());
  }

  public abstract void run();

  public abstract void shutdown();

  public void print(String msg) {
    System.out.println(msg);
  }



}
