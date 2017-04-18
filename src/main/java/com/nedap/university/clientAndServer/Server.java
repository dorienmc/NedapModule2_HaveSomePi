package com.nedap.university.clientAndServer;

import static com.nedap.university.clientAndServer.commands.helpers.ConnectionHelper.isConnectionPacket;

import com.nedap.university.Utils;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.clientAndServer.commands.helpers.MDNSdata;
import com.nedap.university.fileTranser.UDPPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * Server that receives client commands via UDP.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class Server extends Handler {
  volatile boolean running = true;
  private MulticastSocket socket;
  public static final int MULTI_DNS_PORT = 5353;
  public static final int FIRST_RUDP_PORT = 8000;
  public static final String HOSTNAME = "8";
  public static final String FILEPATH = "/home/pi/files";

  private Map<Integer, ClientHandler> clients = new HashMap<>(); //map clientHandlers to ports
  private InetAddress broadcastAddress;
  private static InetAddress myAddress;

  public Server() {
    super(MULTI_DNS_PORT,-1);
  }

  public void run() {
    //Create socket to listen to broadcast messages from clients.
    try {
      socket = new MulticastSocket(MULTI_DNS_PORT);
      broadcastAddress = InetAddress.getByName("192.168.40.255");
      myAddress = InetAddress.getByName("192.168.40.8");
    } catch (IOException e) {
      shutdown();
      print(String.format("Could not create socket on %s:%d", broadcastAddress,MULTI_DNS_PORT));
      return;
    }

    //Listen for clients
    print("Waiting for clients...");
    while(true) {
      DatagramPacket packet = new DatagramPacket(new byte[UDPPacket.MAX_PAYLOAD],
          UDPPacket.MAX_PAYLOAD);

      try {
        socket.receive(packet);
      } catch (IOException e) {
        print("Lost connection to broadcast socket");
        shutdown();
        //Drop packet
      }

      //Check is packet is a connecting packet and destined for this Server
      //Otherwise drop it.
      if(isConnectionPacket(packet, this)) {
        print("Found client " + packet.getAddress() + ":" + packet.getPort());
        addClientHandler(packet);
      }
    }
  }

  public static InetAddress getMyAddress() {
    return myAddress;
  }


  /**
   * Create ClientHandler for client which sends the given connect request.
   * But do check if the connection request is valid.
   */
  private void addClientHandler(DatagramPacket packet) {
    int inPort = Utils.getFreePort(FIRST_RUDP_PORT);
    int outPort = inPort + 1;

    //Reserve spot for client (so port is not free for others)
    ClientHandler client = null;
    clients.put(new Integer(inPort), client);


    try {
      client = new ClientHandler(packet, inPort, outPort, this);
    } catch (SocketException e) {
      client.shutdown();
      print(String.format("Could not create client for %s:%s %s",packet.getAddress(), packet.getPort(), e.getMessage()));
      return;
    }

    //Update client in 'clients'
    clients.put(new Integer(inPort), client);
    client.start();
    print("[Connected " + client + "]");


    //TODO print warning if packet is incorrect?
  }

  /**
   * Remove a ClientHandler from the collection of ClientHandlers.
   * @param client ClientHandler that will be removed
   */
  public void removeClientHandler(ClientHandler client) {
    if(clients.remove(new Integer(client.getInPort())) != null) {
      print("Remove ClientHandler on " + client);
    }
  }

  @Override
  public String getFilePath() {
    return FILEPATH;
  }

  @Override
  public String getHostName() {
    return HOSTNAME;
  }

  @Override
  public void handleSocketException(String errorMessage) {
    print("Server socket error: " + errorMessage);
  }

  /**
   * Print message to System.out //TODO to logfile?
   * @param message
   */
  public void print(String message){
    System.out.println(message);
  }

  public void shutdown() {
    print("Server is shutting down.");
    for(ClientHandler client : clients.values()) {
      client.shutdown();
    }

    //close socket
    socket.close();
    running = false;

    Thread.currentThread().interrupt();
  }

  synchronized public boolean isRunning() {
    return running;
  }


}
