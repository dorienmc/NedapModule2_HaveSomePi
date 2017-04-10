package com.nedap.university.clientAndServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Handler for each client on a server.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class ClientHandler extends Thread {
  InetAddress address;
  int port;

  DatagramSocket socket;

  public ClientHandler(DatagramPacket packet) {
    this.address = packet.getAddress();
    this.port = packet.getPort();
    try {
      socket = new DatagramSocket(port,address);
    } catch (SocketException e) {
      e.printStackTrace(); //TODO
    }
  }

  @Override
  public String toString() {
    return address + ":" + port;
  }

  public void shutdown() {
    //TODO
  }

  //TODO Methods for listening to packets and act on them.
}
