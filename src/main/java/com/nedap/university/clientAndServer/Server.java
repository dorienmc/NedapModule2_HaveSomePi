package com.nedap.university.clientAndServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

/**
 * Server that receives client commands via UDP.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class Server extends Thread {
  private DatagramSocket socket;
  public static final int MULTI_DNS_PORT = 5353;
  public static final int MAX_BUFFER = 1500;
  private List<ClientHandler> clients;

  public void run() {
    //Create socket to listen to broadcast messages from clients.
    try {
      socket = new DatagramSocket(MULTI_DNS_PORT);
    } catch (SocketException e) {
      print("Could not create socket on " + MULTI_DNS_PORT + " " + e.getMessage());
      return;
    }

    //Listen for clients
    print("Waiting for clients...");
    while(true) {
      DatagramPacket packet = new DatagramPacket(new byte[1500],1500);

      try {
        socket.receive(packet);
      } catch (IOException e) {
        e.printStackTrace();
        //Drop packet
      }

      print("Found client " + packet.getAddress() + ":" + packet.getPort());
      addClientHandler(packet);
    }
  }

  /**
   * Create ClientHandler for client which sends the given connect request.
   * But do check if the connection request is valid.
   */
  private void addClientHandler(DatagramPacket packet) {
    //TODO
    //print("[Client " + client + " connected.]");
  }

  /**
   * Remove a ClientHandler from the collection of ClientHanlders.
   * @param client ClientHandler that will be removed
   */
  public void removeClientHandler(ClientHandler client) {
    if(clients.remove(client)) {
      print("Remove ClientHandler on " + client);
    }
  }

  /**
   * Print message to System.out //TODO to logfile?
   * @param message
   */
  public void print(String message){
    System.out.println(message);
  }

  public void shutdown() {
    //TODO
  }

}
