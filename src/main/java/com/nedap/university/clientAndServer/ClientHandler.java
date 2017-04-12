package com.nedap.university.clientAndServer;

import com.nedap.university.clientAndServer.commands.*;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.UDPPacket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Handler for each client on a server.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class ClientHandler extends Handler {
  InetAddress address;
  int inPort;
  int outPort;
  Server server;

  public ClientHandler(DatagramPacket connectPacket, int inPort, int outPort, Server server) throws SocketException {
    super();
    this.server = server;

    //Add commands
    addCommand(new ExitCommand());
    addCommand(new ListFilesCommandServer());
    addCommand(new DownloadCommand());
    addCommand(new UploadCommand());
    addCommand(new PauseCommand());
    addCommand(new ResumeCommand());
    //where does abort belong?

    //TODO add more commands?

    //Setup Reliable UDP channel and return acknowledgement of connection to client
    (new ConnectCommandServer(connectPacket, inPort, outPort)).execute(this);



  }

  @Override
  public void run() {
    //Listen for new commands from this client
    while (true) {
      try {
        UDPPacket requestPacket = getChannel().getNewRequest();
      } catch (Exception e) {
        System.out.println(e.getMessage());
        shutdown();
        return;
      }

      //Determine request
      Keyword requestType = null;//TODO Convert flag to keyword

      //Act on it
      if(requestType != null) {
        handleCommand(requestType); //TODO Handle command in new thread? Otherwise we cannot cancel stuff.
      }
    }
  }

  public int getInPort() {
    return inPort;
  }

  @Override
  public String toString() {
    return "Client at " + address + ":" + inPort + "and :" + outPort;
  }

  public void shutdown() {
    server.removeClientHandler(this);
    //TODO anything else?
  }
}
