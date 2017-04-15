package com.nedap.university.clientAndServer.commands;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.clientAndServer.Server;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommandServer extends Command{

  public UploadCommandServer(Handler handler, Byte requestId) {
    super(Keyword.UPLOAD, "Upload specific file", handler, requestId);
  }

  @Override
  public void execute() {
    //Read file meta data
    UDPPacket firstPacket = null;
    try {
      firstPacket = protocol.receivePacket(0);
    } catch (IOException|TimeoutException e) {
      e.printStackTrace();
    }
    FileMetaData metaData = new FileMetaData(firstPacket.getData());

    downloadFile(metaData);

    while(protocol.busy()) {
      Utils.sleep(10);
    }

    deregisterFromChannel();

    //TODO figure out how to call protocol.receiveFile
    //Wait until complete file is received
    //Merge packets (or do this in RUDP channel?)
    //Save file
    //TODO get file/data that should be send
  }

  private void downloadFile(FileMetaData metaData) {
    //Save received packets in temporary file.
    try (FileOutputStream fileStream = new FileOutputStream(Server.FILEPATH + "/tmp_" + metaData.getFileName())) {
      for (int packetId = 0; packetId < metaData.getNumberOfPackets(); packetId++) {
        //Receive next packet //TODO require specific packet (specific seq.number? specific offset!)
        UDPPacket packet = protocol.receivePacket(0);

        System.out.println("Received packet " + packet.getOffset());

        //Check if not last
        if(packet.isFlagSet(Flag.NOT_LAST)) {
          fileStream.write(packet.getData());
        } else {
          //TODO (check checksum?)
        }
      }
    } catch (IOException|TimeoutException e) {
      e.printStackTrace();
      return;
    }

    //Rename file
    File file = new File(Server.FILEPATH + "/tmp_" + metaData.getFileName());
    file.renameTo(new File(Server.FILEPATH + "/" + metaData.getFileName()));
  }

}
