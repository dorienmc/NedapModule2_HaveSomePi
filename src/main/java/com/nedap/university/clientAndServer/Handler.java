package com.nedap.university.clientAndServer;

import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ReliableUdpChannel;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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

  public Handler() {
    commands = new HashMap<>();
  }

  /**
   * Create a reliable udp channel to 'destAddress' which receives message over port 9292
   * and sends them over 9293 (see ReliableUdpChannel DEFAULT_PORT_IN and DEFAULT_PORT_OUT.
   * @throws SocketException if one of the sockets could not be created.
   */
  public void setChannel(DatagramSocket socketIn, DatagramSocket socketOut, InetAddress destAddress,
      int serverPortIn, int serverPortOut, boolean isClient) throws SocketException {
    this.channel = new ReliableUdpChannel(socketIn, socketOut, destAddress, serverPortIn, serverPortOut, true);
  }

  /**
   * Create a reliable udp channel from the given in/out sockets.
   */
  public void setChannel(DatagramSocket socketIn, DatagramSocket socketOut, InetAddress destAddress,
      int clientPortIn, int clientPortOut) {
    this.channel = new ReliableUdpChannel(socketIn,socketOut,destAddress,clientPortIn,clientPortOut);
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
    return (List) commands.values();
  }

  public abstract void run();

  public abstract void shutdown();

  public void print(String msg) {
    System.out.println(msg);
  }



}
