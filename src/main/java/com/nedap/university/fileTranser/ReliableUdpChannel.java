package com.nedap.university.fileTranser;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.fileTranser.ARQProtocol.NaiveProtocol;
import com.nedap.university.fileTranser.ARQProtocol.ProtocolFactory;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Sequence of UDP packets that are send to perform a command (see clientAndServer.commands)
 * and follow the Reliable Udp File Transfer Protocol.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class ReliableUdpChannel {
  Sender sender;    //Class that sends UDP packets
  Receiver receiver;//Class that receives UDP packets

  /**
   * Create a reliable udp channel to 'destAddress'
   * @throws SocketException if one of the sockets could not be created.
   */
  public ReliableUdpChannel(int sourceInPort, int sourceOutPort, InetAddress destAddress,
      int destInPort, int destOutPort, Handler handler) throws SocketException {
    this.receiver = new Receiver(destAddress,sourceInPort,destOutPort, handler); //receive from socketIn
    this.sender = new Sender(destAddress,sourceOutPort,destInPort, handler); //send over socketOut
    sender.start();
    receiver.start();
    System.out.println(String.format("Setup ReliableUdp Channel to %s, sender: %d -> %d, receiver: %d <- %d",
        destAddress,sourceOutPort,destInPort,sourceInPort,destOutPort));
  }

  /**
   * Register to channel, so it protocol can use its sender and receiver.
   */
  public void register(Command command, ProtocolFactory.Name protocolName) {
    sender.register(command);
    receiver.register(command);
    command.setProtocol(ProtocolFactory.createProtocol(protocolName, sender,receiver, command.getRequestId()));
  }

  /**
   * Remove request from register.
   */
  public void deregister(Command command) {
    sender.deregister(command);
    receiver.deregister(command);
  }

  /**
   * Log running commands.
   */
  public void log() {
    receiver.log();
    sender.log();
  }

  //Private methods
  public void shutdown() {
    log();
    sender.shutdown();
    receiver.shutdown();
  }
}
