package com.nedap.university.clientAndServer.commands.server;

import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.uploadFile;
import static com.nedap.university.fileTranser.UDPPacket.MAX_PAYLOAD;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.ARQProtocol.ProtocolFactory.Name;
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
public class DownloadCommandServer extends Command {

  public DownloadCommandServer(Handler handler, Byte requestId) {
    super(Keyword.DOWNLOAD, "Download specific file", handler, requestId, Name.SLIDING_WINDOW);
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
      try {
        filename = Utils.getFile(Server.FILEPATH, Integer.parseInt(filename));
      } catch (ArrayIndexOutOfBoundsException e) {
        handler.print("File with id " + Integer.parseInt(filename) + " not found");
        shutdown();
      }
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
      digest = uploadFile(file, metaData, protocol, handler);
    } catch (IOException e) {
      handler.print("Cannot download " + metaData.getFileName() + " " + e.getMessage());
      shutdown();
    }

    //Send md5 of the uploaded file
    handler.printDebug("Created message digest " + Utils.binaryArrToHexString(digest));
    protocol.sendData(digest,false);

    //Wait for EOR packet from client, eg until protocol is stopping
    int time = 0;
    while(protocol.busy()) {
      Utils.sleep(100);

      time += 100;
      if(time > metaData.getNumberOfPackets() * protocol.getTimeOut()) {
        handler.print("Did not receive md5 ack packet from client in given time (" +
            metaData.getNumberOfPackets() * protocol.getTimeOut()
            +  "ms)");
        shutdown();
      }
    }

    handler.print("Download of " + metaData.getFileName() + " to client was successful.");
    shutdown();
  }

}
