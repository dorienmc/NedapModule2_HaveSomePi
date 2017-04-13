package com.nedap.university.fileTranser;


import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ARQProtocol.NaiveProtocol;
import com.nedap.university.fileTranser.ARQProtocol.Protocol;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

/**
 * Sequence of UDP packets that are send to perform a command (see clientAndServer.commands)
 * and follow the Reliable Udp File Transfer Protocol.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class ReliableUdpChannel {
  Protocol protocol;
  Sender sender;    //Class that sends UDP packets
  Receiver receiver;//Class that receives UDP packets

  /**
   * Create a reliable udp channel to 'destAddress'
   * @throws SocketException if one of the sockets could not be created.
   */
  public ReliableUdpChannel(int sourceInPort, int sourceOutPort, InetAddress destAddress,
      int destInPort, int destOutPort, boolean isClient) throws SocketException {
    this.receiver = new Receiver(destAddress,sourceInPort,destOutPort); //receive from socketIn
    this.sender = new Sender(destAddress,sourceOutPort,destInPort); //send over socketOut
    this.protocol = new NaiveProtocol(sender,receiver);
    sender.start();
    receiver.start();
    System.out.println(String.format("Setup ReliableUdp Channel to %s, sender: %d -> %d, receiver: %d <- %d",
        destAddress,sourceOutPort,destInPort,sourceInPort,destOutPort));

    if(isClient) {
      sendAckToOtherSide(sourceOutPort, destInPort);
    }
  }

  /*
   * Send ack to server
   */
  private void sendAckToOtherSide(int sourceOutPort, int destInPort) {
    System.out.println("Send ack to server");
    sendAck(Flag.CONNECT.getValue());
//    UDPPacket connectionAck = new UDPPacket(sourceOutPort, destInPort, 0,0);
//    connectionAck.setFlags(Flag.CONNECT.getValue());
//    sender.forceSend(connectionAck);
  }

  /* Change protocol, is currently Stop-and-wait by default */
  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  /* Send request without data */
  public byte[] sendAndReceive(Keyword keyword) throws IOException {
    Flag flag = keyword.toFlag();
    if(flag != null) {
      protocol.send(new byte[0], flag.getValue());
      return protocol.receive();
    } else {
      return null;
    }
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

  /* Send ack with flags (set to 0 if not needed) */
  public void sendAck(int flags) {
    protocol.sendAck(0);
  }

  /**
   *  Wait for new request
   *  @throws TimeoutException if it takes more than 300000ms (5min)
   *  @throws IOException if it could not reach the socket
   */
  public UDPPacket getNewRequest() throws IOException,TimeoutException {
    return protocol.receivePacket(300000);
  }

  //Private methods
  public void shutdown() {
    sender.shutdown();
    receiver.shutdown();
  }
}
