package com.nedap.university.clientAndServer.commands.helpers;

import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.net.DatagramPacket;

/**
 * Created by dorien.meijercluwen on 18/04/2017.
 */
public class ConnectionHelper {


  public static boolean isConnectionPacket(DatagramPacket packet, Handler handler) {
    UDPPacket udpPacket;
    try {
      udpPacket = new UDPPacket(packet);
    } catch (ArrayIndexOutOfBoundsException|NegativeArraySizeException e) {
      handler.print("Received packet is not a connection packet, " + e.getMessage());
      return false;
    }

    //Has connection flag set
    if(!udpPacket.isFlagSet(Flag.CONNECT)) {
      return false;
    }

    //Is asking for me
    try {
      MDNSdata mdnSdata = new MDNSdata(udpPacket.getData());
      if(mdnSdata.getHostname() != null && mdnSdata.getHostname().equals(handler.getHostName())) {
        return true;
      }
    } catch (IndexOutOfBoundsException e) {
      handler.print(e.getMessage());
      return false;
    }

    return false;
  }
}
