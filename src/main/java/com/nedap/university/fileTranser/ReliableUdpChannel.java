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
      int otherSidePortIn, int otherSidePortOut, boolean isClient) throws SocketException {
    this.receiver = new Receiver(socketIn,destAddress,socketIn.getLocalPort(),otherSidePortOut); //receive from socketIn
    this.sender = new Sender(socketOut,destAddress,socketOut.getLocalPort(), otherSidePortIn); //send over socketOut
    this.protocol = new NaiveProtocol(sender,receiver);
    sender.start();
    receiver.start();
    System.out.println(String.format("Set ReliableUdp Channel to %s, sender: %d -> %d, receiver: %d <- %d",
        destAddress,socketOut.getLocalPort(),otherSidePortIn,socketIn.getLocalPort(),otherSidePortOut));

    if(isClient) {
      sendAckToOtherSide(socketOut.getLocalPort(), otherSidePortIn);
    }

  }

  private void sendAckToOtherSide(int sourcePort, int destPort) {
    //Send ack to server (send 2 to be sure)
    System.out.println("Send ack to server");
    UDPPacket connectionAck = new UDPPacket(sourcePort, destPort, 0,0);
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

  /* Send given data, with given flag */
  public byte[] sendAndReceive(byte[] data, Flag flag) throws IOException{
    protocol.send(data, flag.getValue());
    return protocol.receive();
  }

  /* Send given data */
  public byte[] sendAndReceive(byte[] data) throws IOException{
    protocol.send(data, 0);
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
    sender.shutdown();
    receiver.shutdown();

    //Shutdown sender and receiver.
  }
  //TODO what else?
}