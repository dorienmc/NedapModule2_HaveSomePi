package com.nedap.university.fileTranser;


import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ARQProtocol.NaiveProtocol;
import com.nedap.university.fileTranser.ARQProtocol.Protocol;
import com.sun.javafx.binding.StringFormatter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Sequence of UDP packets that are send to perform a command (see clientAndServer.commands)
 * and follow the Reliable Udp File Transfer Protocol.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class ReliableUdpChannel { //TODO extend thread?
  public static final int DEFAULT_PORT_IN = 9292;
  public static final int DEFAULT_PORT_OUT = 9293;

  Protocol protocol;
  Sender sender;    //Class that sends UDP packets
  Receiver receiver;//Class that receives UDP packets

//  int portInOtherSide;         //Socket for receiving data on the other end //TODO still needed?
//  int portOutOtherSide;        //Socket for sending data on the other end

  /**
   * Create a reliable udp channel to 'destAddress' which receives message over port 9292
   * and sends them over 9293 (see ReliableUdpChannel DEFAULT_PORT_IN and DEFAULT_PORT_OUT.
   * Clientside.
   * @throws SocketException if one of the sockets could not be created.
   */
  public ReliableUdpChannel(DatagramSocket socketIn, DatagramSocket socketOut, InetAddress destAddress,
      int serverPortIn, int serverPortOut, boolean isClient) throws SocketException {
    this.receiver = new Receiver(socketIn,destAddress,DEFAULT_PORT_IN,serverPortOut); //receive from socketIn
    this.sender = new Sender(socketOut,destAddress,DEFAULT_PORT_OUT, serverPortIn); //send over socketOut
    this.protocol = new NaiveProtocol(sender,receiver);
    sender.start();
    receiver.start();
    System.out.println(String.format("Set ReliableUdp Channel to %s, sender: %d -> %d, receiver: %d <- %d",
        destAddress,DEFAULT_PORT_OUT,serverPortIn,DEFAULT_PORT_IN,serverPortOut));

    //Send ack to server (send 2 to be sure)
    System.out.println("Send ack to server");
    UDPPacket connectionAck = new UDPPacket(DEFAULT_PORT_OUT, serverPortIn, 0,0);
    connectionAck.setFlags(Flag.CONNECT.getValue());
    System.out.println(connectionAck.getSourcePort());
    sender.forceSend(connectionAck);

    //Wait
    try {
      Thread.sleep(7000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    sender.forceSend(connectionAck);

    //Wait
    try {
      Thread.sleep(7000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

  }

  /**
   * Create a reliable udp channel from the given in/out sockets. Server side.
   */
  public ReliableUdpChannel(DatagramSocket socketIn, DatagramSocket socketOut,InetAddress destAddress,
      int clientPortIn, int clientPortOut) {
    this.receiver = new Receiver(socketIn, destAddress, socketIn.getLocalPort(), clientPortOut);
    this.sender = new Sender(socketOut,destAddress, socketOut.getLocalPort(), clientPortIn);
    this.protocol = new NaiveProtocol(sender,receiver);
    sender.start();
    receiver.start();
    System.out.println(String.format("Set ReliableUdp Channel to %s, sender: %d -> %d, receiver: %d <- %d",
        destAddress,socketOut.getLocalPort(),clientPortIn,socketIn.getLocalPort(),clientPortOut));
  }

  /* Change protocol, is currently Stop-and-wait by default */
  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  public int getReceivePort() {
    return receiver.getPortIn();
  }

  public int getSendPort() {
    return sender.getSourcePort();
  }

  /* Send request without data */
  public byte[] sendAndReceive(Keyword requestType) {
    //TODO
    System.out.println("TODO implement ReliableUdpChannel.send(Flag flag)");
    return protocol.receive();
  }

  /* Send data in given file */
  public byte[] sendAndReceive(String filename) {
    //TODO
    System.out.println("TODO implement ReliableUdpChannel.send(String filename)");
    return protocol.receive();
  }

  /* Send given data */
  public byte[] sendAndReceive(byte[] data) throws IOException{
    protocol.send(data);
    return protocol.receive();
  }

  /* Send request with data */
  public byte[] sendAndReceive(Keyword requestType, String filename) {
    //TODO
    System.out.println("TODO implement ReliableUdpChannel.send(Flag flag, String filename)");
    return protocol.receive();
  }

  /* Wait for new request */
  public UDPPacket getNewRequest() throws Exception {
    System.out.println("TODO implement ReliableUdpChannel.getNewRequest()");
    throw new Exception("TODO implement ReliableUdpChannel.getNewRequest()");
    //return null;
  }

  //Private methods
  public void shutdown() {
    //Shutdown sender and receiver.
  }
  //TODO what else?
}
