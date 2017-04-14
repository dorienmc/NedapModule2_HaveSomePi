package com.nedap.university.fileTranser.ARQProtocol;

import com.nedap.university.Utils;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.Receiver;
import com.nedap.university.fileTranser.Sender;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

  /* Send data in sender buffer, dont wait */
  public void send() throws IOException {
    resendCount = 0;

    //Send complete buffer
    sender.unBlockSender();

    while(sender.getBufferLength() > 0) {
      //Wait some more until buffer is empty.
      Utils.sleep(10);
    }

    sender.blockSender();


//    while(resendCount < MAX_RESEND) {
//      sender.addPacketToBuffer(packet);
//      sender.unBlockSender();
//
//      //Wait for response
//      try {
//        UDPPacket response = super.receivePacket(7000);
//
//        //Save data to 'dataReceived'
//        super.addReceivedData(response.getData());
//        break;
//
//      } catch (TimeoutException e) {
//        System.out.println("TIME OUT " + resendCount);
//      }
//
//      resendCount++;
//    }
//
//    if(resendCount >= MAX_RESEND) {
//      throw new IOException("Resend limit of " + MAX_RESEND + " exceeded.");
//    } else {
//
//    }
  }

  /* Wait until complete file is received */
  public void receiveFile(String filename, int fileId) {
    //Add received packets to received data (dont care about order)
    //Until a packet with NOT_LAST = false arrives.

    while(true) {
      try {
        UDPPacket packet = super.receivePacket(0);
        super.addReceivedData(packet.getData());
        if(!packet.isFlagSet(Flag.NOT_LAST)) {
          break;
        }
      } catch (IOException e) {
        e.printStackTrace();
      } catch (TimeoutException e) {
        e.printStackTrace();
      }

    }

    //Create file //TODO do this for each received packet as soon as it is received.
    //TODO move this somewhere else?
    File file = new File("./files/" + filename);

    try (FileOutputStream fileStream = new FileOutputStream(file)) {
      fileStream.write(dataReceived);
    } catch (FileNotFoundException e) {
      System.out.println(e.getMessage());
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }

  }

  /* Tell if received packet is expected, if not it should be dropped .
  * The naive protocol allows all packets.
  * */
  public boolean isExpected(UDPPacket packet) {
    return true;
  }
}
