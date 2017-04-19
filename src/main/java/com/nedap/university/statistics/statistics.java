package com.nedap.university.statistics;

import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to track Statistics.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class Statistics {
  int numberOfUnparseableConnectionPackets;   //Number of packets arrived at the server which did not adhere to the CONNECT format
  int numberOfUnparseableReceivedPackets;     //Number of packets received via the Receiver, which could not be parsed to an UDPPacket()
  int numberOfInvalidPackets;                 //Number of packets with incorrect sourcePort, destinationPort and/or checksum.
  int numberOfUnrecognizedRequestPackets;     //Number of packets of which the flags do not correspond to a Keyword (request) and the request id not to a current request.
  int numberOfPacketsOutsideReceiveWindow;    //Number of packets which lie outside of the receiver window of the protocol (eg. we already have them or we are not ready to receive them yet).
  int numberOfRetransmission;                 //Number of packets that are retransmitted due to timeout.
  ArrayList<String> speedLogs;                //Logs of download/upload speed of downloaded/uploaded files.

  public Statistics() {
    this.numberOfUnparseableConnectionPackets = 0;
    this.numberOfUnparseableReceivedPackets = 0;
    this.numberOfInvalidPackets = 0;
    this.numberOfUnrecognizedRequestPackets = 0;
    this.numberOfPacketsOutsideReceiveWindow = 0;
    this.numberOfRetransmission = 0;
    this.speedLogs = new ArrayList<>();
  }

  public synchronized void logUnparseableConnectionPacket() {
    numberOfUnparseableConnectionPackets++;
  }

  public synchronized void logUnparseableReceivedPacket() {
    numberOfUnparseableReceivedPackets++;
  }

  public synchronized void logInvalidPacket() {
    numberOfInvalidPackets++;
  }

  public synchronized void logUnrecognizedRequest() {
    numberOfUnrecognizedRequestPackets++;
  }

  public synchronized void logPacketOutsideReceiverWindow() {
    numberOfPacketsOutsideReceiveWindow++;
  }

  public synchronized void logRetransmission() {
    numberOfRetransmission++;
  }

  public synchronized void addSpeedLog(String info) {
    speedLogs.add(info);
  }



  public synchronized int getUnparseAblePackets() {
    return numberOfUnparseableConnectionPackets + numberOfUnparseableReceivedPackets;
  }

  public synchronized int getInvalidPackets() {
    return numberOfInvalidPackets + numberOfUnrecognizedRequestPackets;
  }

  private synchronized int getNumberOfUnparseableConnectionPackets() {
    return numberOfUnparseableConnectionPackets;
  }

  private synchronized int getNumberOfUnparseableReceivedPackets() {
    return numberOfUnparseableReceivedPackets;
  }

  private synchronized int getNumberOfInvalidPackets() {
    return numberOfInvalidPackets;
  }

  private synchronized int getNumberOfUnrecognizedRequestPackets() {
    return numberOfUnrecognizedRequestPackets;
  }

  private synchronized int getNumberOfPacketsOutsideReceiveWindow() {
    return numberOfPacketsOutsideReceiveWindow;
  }

  private synchronized int getNumberOfRetransmission() {
    return numberOfRetransmission;
  }

  private synchronized ArrayList<String> getSpeedLogs() {
    return speedLogs;
  }

  @Override
  public String toString() {
    String result = "--------------\nStatistics: \n";

    result += String.format("%-10s %d\n", "Unparseable packets", getUnparseAblePackets());
    result += String.format("%-10s %d\n", "\tDuring connecting", getNumberOfUnparseableConnectionPackets());
    result += String.format("%-10s %d\n", "\tWhile receiving", getNumberOfUnparseableReceivedPackets());
    result += String.format("%-10s %d\n", "Invalid packets", getInvalidPackets());
    result += String.format("%-10s %d\n", "\tUnrecognized requests", getNumberOfUnrecognizedRequestPackets());
    result += String.format("%-10s %d\n", "\tInvalid source/dest port or checksum", getNumberOfInvalidPackets());
    result += String.format("%-10s %d\n", "Dropped packets", getNumberOfPacketsOutsideReceiveWindow());
    result += String.format("%-10s %d\n", "Retransmission", getNumberOfRetransmission());

    result += String.format("%-10s\n", "Speed logs:");
    for(String speedLog: getSpeedLogs()) {
      result += "\t" + speedLog + "\n";
    }

    result += "--------------";
    return result;
  }
}