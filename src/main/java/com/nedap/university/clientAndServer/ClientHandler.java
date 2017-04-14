package com.nedap.university.clientAndServer;

import com.nedap.university.clientAndServer.commands.server.ConnectCommandServer;
import java.net.DatagramPacket;
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
    super.setCommandFactory(new CommandFactoryServer(this));

    //Setup Reliable UDP channel and return acknowledgement of connection to client
    (new ConnectCommandServer(connectPacket,this, new Byte((byte)0))).execute();
  }

  @Override
  public void run() {
    //handleSocketInput();
  }

  /**
   * Handle it when sender or receiver breaks down because it cannot reach the socket.
   **/
  public void handleSocketException (String errorMessage) {
    print(errorMessage);
    shutdown();
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
