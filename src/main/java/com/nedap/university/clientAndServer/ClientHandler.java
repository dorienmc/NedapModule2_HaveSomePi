package com.nedap.university.clientAndServer;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.*;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

/**
 * Handler for each client on a server.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class ClientHandler extends Handler {
  Server server;

  public ClientHandler(DatagramPacket connectPacket, int inPort, int outPort, Server server) throws SocketException {
    super(inPort, outPort);
    this.server = server;
    super.setCommandFactory(new CommandFactoryServer(this));

    //Setup Reliable UDP channel and return acknowledgement of connection to client
    (new ConnectCommandServer(connectPacket,this, new Byte((byte)0))).execute();
  }

  @Override
  public void run() {
    handleSocketInput();
  }

  private void handleSocketInput() {
    //Wait until channel has started up
    while(getChannel() == null) {
      Utils.sleep(10);
    }

    //Let channel demux the incoming packets
    try {
      getChannel().handleReceivedPackets(this);
    } catch (IOException|TimeoutException e) {
      print("Could not receive over socket " + e.getMessage());
      shutdown();
    }
  }

  @Override
  public String toString() {
    return "Client at " + super.getAddress() + ":" + super.getInPort() + " and :" + super.getOutPort();
  }

  public void shutdown() {
    if(getChannel() != null) {
      getChannel().shutdown();
    }
    server.removeClientHandler(this);

    //TODO anything else?
  }
}
