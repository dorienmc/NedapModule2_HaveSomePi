package com.nedap.university.fileTranser;


import com.nedap.university.fileTranser.ARQProtocol.Protocol;
import java.util.ArrayList;
import java.util.List;

/**
 * Sequence of UDP packets that are send to perform a command (see clientAndServer.commands)
 * and follow the Reliable Udp File Transfer Protocol.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class ReliableUdpChannel {
  Protocol protocol;
  List<UDPPacket> unAckedPackets;

  int sourcePort; //Senders port
  int destPort;   //Receivers port
  int currentSeqNumber; //Sequence number of next packet that is to be send.
  int currentAckNumber; //Sequence number of next expected packet from receiver.

  public ReliableUdpChannel(int sourcePort, int destPort){
    this.sourcePort = sourcePort;
    this.destPort = destPort;
    this.unAckedPackets = new ArrayList<>();
  }

  public ReliableUdpChannel(int sourcePort, int destPort, Protocol protocol){
    this(sourcePort,destPort);
    this.protocol = protocol;
  }

  //TODO some execute methods.
  //TODO Set data that should be send
  //TODO Tell what data should be received
}
