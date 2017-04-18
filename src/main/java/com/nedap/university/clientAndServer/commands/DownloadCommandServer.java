package com.nedap.university.clientAndServer.commands;

import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.uploadFile;
import static com.nedap.university.fileTranser.UDPPacket.MAX_PAYLOAD;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.ARQProtocol.StopAndWaitProtocol;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
      firstPacket = protocol.receivePacket(0);
    } catch (TimeoutException e) {
      handler.print("Error in " + getKeyword() + " request " + e.getMessage());
      shutdown();
    }

    //Retrieve file (if possible)
    String filename = new String(firstPacket.getData());

    if(Utils.isANumber(filename)) {
      //Parse number to actual file
      filename = Utils.getFile(Server.FILEPATH, Integer.parseInt(filename));
    }

    File file = new File(Server.FILEPATH + "/" + filename);
    if(!file.exists()) {
      handler.print("Could not find " + Server.FILEPATH + "/" + filename);
      shutdown();
    }

    //Create file meta data and send this info to client
    FileMetaData metaData = new FileMetaData(file, MAX_PAYLOAD);
    UDPPacket downloadInfo = protocol.createEmptyPacket();
    downloadInfo.setData(metaData.getData());
    protocol.addPacketToSendBuffer(downloadInfo);

    //Tell protocol it can already start sending
    if(!protocol.isAlive()) {
      protocol.start();
    }

    //Split file in packets and send them
    byte[] digest = new byte[0];
    try {
      digest = uploadFile(file, protocol);
    } catch (IOException e) {
      handler.print("Cannot download " + metaData.getFileName() + " " + e.getMessage());
      shutdown();
    }

    //Send md5 of the uploaded file
    System.out.println("Created message digest " + Utils.binaryArrToHexString(digest));
    protocol.sendData(digest,false);

    //Wait for ack of client, aka the EOR packet
    try {
      UDPPacket md5Ack = protocol.receivePacket(metaData.getNumberOfPackets() + 1,
          metaData.getNumberOfPackets() * protocol.getTimeOut());

      //Check if this is also the EOR packet
      if(!md5Ack.isFlagSet(Flag.LAST)) {
        handler.print("Expected EOR packet");
      }

    } catch (TimeoutException e) {
      handler.print("Did not receive md5 ack packet from server in given time, " + e.getMessage());
    }

    shutdown();
  }

}
