package com.nedap.university.fileTranser;


import static com.nedap.university.fileTranser.ARQProtocol.Protocol.MAX_BUFFER;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.fileTranser.ARQProtocol.NaiveProtocol;
import com.nedap.university.fileTranser.ARQProtocol.Protocol;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
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

    if(isClient) { //TODO should be done by protocol?
      sendAckToOtherSide(sourceOutPort, destInPort);
    }
  }

  /*
   * Send ack to server
   */
  private void sendAckToOtherSide(int sourceOutPort, int destInPort) {
    System.out.println("Send ack to server");
    sendAck(Flag.CONNECT.getValue());
  }

  /* Change protocol, is currently Stop-and-wait by default */
  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  /* Send ack with flags (set to 0 if not needed) */
  public void sendAck(int flags) {
    try {
      sendRequest(new byte[0], flags);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /********** Send request ***********/

  /* Send Request (type defined by flags) with extra parameters in data */
  public byte[] sendRequest(byte[] data, int flags) throws IOException{
    UDPPacket packet = sender.createEmptyPacket();
    packet.setData(data);
    packet.setFlags(flags);
    sender.addPacketToBuffer(packet);

    protocol.send();
    return protocol.receive();
  }

  /* Send Request (type defined by Flag) with extra parameters in data */
  public byte[] sendRequest(byte[] data, Flag flag) throws IOException{
    return sendRequest(data, flag.getValue());
  }

  /* Send Request (type defined by Keyword) with extra parameters in data */
  public byte[] sendRequest(Keyword keyword) throws IOException{
    Flag flag = keyword.toFlag();
    if(flag != null) {
      return sendRequest(new byte[0], flag);
    } else {
      return null;
    }
  }

  /********** Send data/file **********/
  /* Send data (not from file) */
  public byte[] sendData(byte[] data) throws IOException {
    UDPPacket packet = sender.createEmptyPacket();
    packet.setData(data);
    sender.addPacketToBuffer(packet);

    protocol.send();
    return protocol.receive();
  }

  /* Send data in given file */
  public byte[] uploadFile(String filename, byte requestId) throws IOException {
    File file = new File("./files/filename");
    if(!file.exists()) {
      throw new FileNotFoundException("Could not find ./files/" + filename);
    }

    //Put uploadrequest (with file metadata) in the sender buffer
    UDPPacket uploadRequest = sender.createEmptyPacket();
    uploadRequest.setFlags(Flag.UPLOAD.getValue());
    uploadRequest.setHeaderSetting(HeaderField.REQUEST_ID,requestId);
    byte[] fileName = file.getName().getBytes();
    ByteBuffer buffer = ByteBuffer.allocate(fileName.length + 8);
    buffer.put(fileName);
    buffer.putLong(file.length());
    uploadRequest.setData(buffer.array());
    sender.addPacketToBuffer(uploadRequest);

    //Split file in packets
    try (FileInputStream fileStream = new FileInputStream(file)) {
      //Determine the number of packets that are needed
      double nPackets = Math.ceil(file.length() / (double) MAX_BUFFER);
      System.out.println("Splitting file in " + nPackets);

      for (int packetId = 0; packetId < nPackets; packetId++) {
        UDPPacket packet = sender.createEmptyPacket();
        packet.setFlags(Flag.NOT_LAST.getValue());
        packet.setHeaderSetting(HeaderField.REQUEST_ID,requestId);
        packet.setHeaderSetting(HeaderField.OFFSET,packetId); //Count per MAX_BUFFER

        byte[] data = new byte[MAX_BUFFER];
        if(fileStream.read(data) == -1) {
          //NO more bytes to read, send packet with Flag.NOT_LAST = false
          packet.setFlags(0);
        } else {
          packet.setData(data);

          //Put each packet in the sender buffer
          sender.addPacketToBuffer(packet);
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    //Tell protocol to start sending and wait until its done.
    protocol.send();

    return protocol.receive(); //TODO is there something to return?
  }



  /* Request data from given file */
  public byte[] downloadFile(String filename) {
    //TODO
    System.out.println("TODO implement ReliableUdpChannel.send(Flag flag, String filename)");
    return protocol.receive();
  }


  /********* DEMUX received packets **********/
  /**
   * Send received packet to the correct process (Command)
   * Or create a new Command.
   * @param handler Handler for which packets are received.
   * @throws TimeoutException if no packets have arrived in the last 300000ms (5min)
   * @throws IOException if it could not reach the input socket
   */
  public void handleReceivedPackets(Handler handler) throws IOException, TimeoutException {
    while(true) {
      //Wait for next packet
      UDPPacket packet = protocol.receivePacket(300000);

      //Get requestId
      byte requestId = (byte) packet.getRequestId();
      handler.print("Received packet for request " + requestId + " with sequence number" + packet
          .getSequenceNumber());

      //Determine to which process the packet belongs
      Command command = handler.getRunningCommand(requestId);

      if (command == null) {
        //Get requestType from flag
        Keyword requestType = Keyword.fromFlags(packet.getFlags());

        //Create a new one
        if (requestType != null) {
          handler.startNewCommand(requestType, requestId);
        } else {
          handler.print("Could not parse request type from flags: " + packet.getFlags());
          continue;
        }
      } else {
        //Let protocol decide if packet is expected (eg. sequence number could not confirm to sliding window)
        //TODO how to determine sequence number for different protocols.
//        if(!protocol.isExpected(packet)) {
//          //Drop packet.
//          //TODO list ignored packets in statistics.
//          continue;
//        }
      }

      //Add packet to corresponding command.
      command = handler.getRunningCommand(requestId);
      //TODO how? It has no receive buffer currently.

//    if(requestPacket != null) {
//      //Determine request
//      Flag flag = Flag.fromByte((byte) requestPacket.getFlags());
//      Keyword requestType = flag.toKeyword();
//
//      //Act on it
//      if (requestType != null) {
//        //Ignore connect requests
//        if(!requestType.equals(Keyword.CONNECT)) {
//          handleCommand(requestType); //TODO Handle command in new thread? Otherwise we cannot cancel stuff.
//        }
//      }
//    }
    }
  }




  /**
   *  Wait for new request
   *  @throws TimeoutException if it takes more than 300000ms (5min)
   *  @throws IOException if it could not reach the socket
   */
  public UDPPacket getNewRequest() throws IOException,TimeoutException {
    return null;
  }



  //Private methods
  public void shutdown() {
    sender.shutdown();
    receiver.shutdown();
    //TODO log still running commands/processes
  }
}
