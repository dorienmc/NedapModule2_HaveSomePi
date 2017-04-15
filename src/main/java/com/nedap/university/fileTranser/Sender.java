package com.nedap.university.fileTranser;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dorien.meijercluwen on 12/04/2017.
 */
public class Sender extends Thread {
  Handler handler;
  DatagramSocket socket;
  int destPort;
  int sourcePort;
  InetAddress address;
  private ConcurrentHashMap<Byte, Command> requests; //Requests/commands which follow a specific protocol
  boolean stop;

  /*
  * Create new sender, starts blocked
   */
  public Sender(InetAddress address, int sourcePort, int destPort, Handler handler) throws SocketException {
    this.socket = new DatagramSocket(sourcePort);
    this.requests = new ConcurrentHashMap<>();
    this.stop = false;
    this.address = address;
    this.destPort = destPort;
    this.sourcePort = sourcePort;
    this.handler = handler;
  }

  public int getDestPort() {
    return destPort;
  }

  public int getSourcePort() {
    return sourcePort;
  }

  @Override
  public void run() {

    while(!stop) {
      if(requests.size() == 0) {
        //Wait for new requests.
        Utils.sleep(10);
        continue;
      }

      //If there are requests, ask them for a next packet to send. (Round Robin)
      for(Command request: requests.values()) {
        UDPPacket packet = request.getNextPacket();
        if(packet != null) {
          try {
            System.out.println("Send new packet for " + request);
            sendPacket(packet);
          } catch (IOException e) {
            handler.handleSocketException(String.format("WARNING could not send packet from request %d with seq. number %d",
                request.getRequestId(), packet.getSequenceNumber()));
          }
        }
      }

    }
  }

  private void sendPacket(UDPPacket packet) throws IOException {
    //System.out.println("Try to send packet to " + address + ":" + packet.getDestPort());
    //Try to send
    socket.send(packet.toDatagram(address));
  }

  public void shutdown() {
    stop = true;
    socket.close();
  }


  public void register(Command request) {
    requests.put(request.getRequestId(), request);
  }


  public void deregister(Command request) {
    requests.remove(request.getRequestId());
  }

  /**
   * Log running commands.
   */
  public void log() {
    //TODO
    System.out.println("TODO log running commands in Sender");
  }

}
