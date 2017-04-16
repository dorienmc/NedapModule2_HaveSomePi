package com.nedap.university.clientAndServer.commands;

import static com.nedap.university.fileTranser.ARQProtocol.Protocol.MAX_BUFFER;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.ARQProtocol.StopAndWaitProtocol;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class DownloadCommandServer extends Command{

  public DownloadCommandServer(Handler handler, Byte requestId) {
    super(Keyword.DOWNLOAD, "Download specific file", handler, requestId);
  }

  @Override
  public void execute() {
    //Retrieve file download request
    UDPPacket firstPacket = null;
    try {
      firstPacket = protocol.receivePacket(StopAndWaitProtocol.TIMEOUT);
    } catch (IOException |TimeoutException e) {
      handler.print("Error in " + getKeyword() + " request " + e.getMessage());
      shutdown();
    }

    //Retrieve file (if possible)
    String filename = new String(firstPacket.getData());
    File file = new File(Server.FILEPATH + "/" + filename);
    if(!file.exists()) {
      handler.print("Could not find " + Server.FILEPATH + "/" + filename);
      shutdown();
    }

    //Create file meta data and send this info to client
    FileMetaData metaData = new FileMetaData(file, MAX_BUFFER);
    UDPPacket downloadInfo = protocol.createEmptyPacket();
    downloadInfo.setData(metaData.getData());
    protocol.addPacketToSendBuffer(downloadInfo);

    //Tell protocol it can already start sending
    if(!protocol.isAlive()) {
      protocol.start();
    }

    //Split file in packets and send them
    try (FileInputStream fileStream = new FileInputStream(file)) {

      for (int packetId = 0; packetId < metaData.getNumberOfPackets(); packetId++) {
        UDPPacket packet = protocol.createEmptyPacket();
        packet.setHeaderSetting(HeaderField.OFFSET,packetId); //Count per MAX_BUFFER

        byte[] data = new byte[MAX_BUFFER];
        fileStream.read(data);
        packet.setData(data);

        //Put each packet in the sender buffer
        protocol.addPacketToSendBuffer(packet);

      }

      //Send EOR packet
      protocol.sendEndOfRequestPacket();

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }


  }

}
