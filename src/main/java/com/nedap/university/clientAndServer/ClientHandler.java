package com.nedap.university.clientAndServer;

import com.nedap.university.clientAndServer.commands.*;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Handler for each client on a server.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class ClientHandler extends Handler {
  Server server;

  public ClientHandler(DatagramPacket connectPacket, int inPort, int outPort, Server server) throws SocketException {
    super(inPort, outPort);
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
    (new ConnectCommandServer(connectPacket)).execute(this);

  }

  @Override
  public void run() {
    //Listen for new commands from this client
    UDPPacket requestPacket;
    while (true) {
      try {
        requestPacket = getChannel().getNewRequest();
      } catch (Exception e) {
        System.out.println(e.getMessage());
        shutdown();
        return;
      }

      if(requestPacket != null) {
        //Determine request
        Flag flag = Flag.fromByte((byte) requestPacket.getFlags());
        Keyword requestType = flag.toKeyword();

        //Act on it
        if (requestType != null) {
          //Ignore connect requests
          if(!requestType.equals(Keyword.CONNECT)) {
            handleCommand(requestType); //TODO Handle command in new thread? Otherwise we cannot cancel stuff.
          }
        }
      }

      System.out.println("Wait for request");
    }
  }

  @Override
  public String toString() {
    return "Client at " + super.getAddress() + ":" + super.getInPort() + " and :" + super.getOutPort();
  }

  public void shutdown() {
    getChannel().shutdown();
    server.removeClientHandler(this);

    //TODO anything else?
  }
}
