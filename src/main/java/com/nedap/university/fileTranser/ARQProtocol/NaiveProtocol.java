package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.Sender;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Very naive protocol that just send data and does send acks.
 * Doesnt change the sequence number either. Also sends all data at once.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class NaiveProtocol extends Protocol{
  public static final int MAX_RESEND = 10;
  private int resendCount;

  public NaiveProtocol(Sender sender, Receiver receiver) {
    super(sender, receiver);
  }

  /* Send given data (in parts) and wait for response */
  public void send(byte[] data, int flags) throws IOException {
    resendCount = 0;

    //Send data in 1 go
    UDPPacket packet = new UDPPacket(sender.getSourcePort(), sender.getDestPort(), 0, 0);
    packet.setData(data);
    packet.setFlags(flags);

    while(resendCount < MAX_RESEND) {
      sender.addPacketToBuffer(packet);
      sender.unBlockSender();

      //Wait for response
      try {
        UDPPacket response = super.receivePacket(7000);

        //Save data to 'dataReceived'
        super.addReceivedData(response.getData());
        break;

      } catch (TimeoutException e) {
        System.out.println("TIME OUT " + resendCount);
      }

      resendCount++;
    }

    if(resendCount >= MAX_RESEND) {
      throw new IOException("Resend limit of " + MAX_RESEND + " exceeded.");
    }
  }
}
