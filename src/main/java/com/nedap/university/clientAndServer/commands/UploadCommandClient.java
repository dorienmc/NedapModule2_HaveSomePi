package com.nedap.university.clientAndServer.commands;

import static com.nedap.university.fileTranser.ARQProtocol.Protocol.MAX_BUFFER;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommandClient extends Command{

  public UploadCommandClient(Handler handler, Byte requestId) {
    super(Keyword.UPLOAD, "Upload specific file", handler, requestId);
  }

  @Override
  public void execute() {
    System.out.println("test");
    handler.print(Utils.listFiles(Client.FILEPATH,"Files available for upload"));
    String filename = Utils.readString("Which file do you want to upload (enter name or id)? ");

    if(Utils.isANumber(filename)) {
      //Parse number to actual file
      filename = Utils.getFile(Client.FILEPATH, Integer.parseInt(filename));
    }

    try {
      uploadFile(filename);
    } catch (IOException e) {
      System.out.println("Could not upload " + filename + ", " + e.getMessage());
    }

    while(protocol.busy()) {
      Utils.sleep(10);
    }

    deregisterFromChannel();
  }

  /* Send data in given file */
  //TODO create checksum/md5 during reading?
  public void uploadFile(String filename) throws IOException {
    File file = new File(Client.FILEPATH + "/" + filename);
    if(!file.exists()) {
      throw new FileNotFoundException("Could not find " + Client.FILEPATH + "/" + filename);
    }

    //Create file meta data
    FileMetaData metaData = new FileMetaData(file, MAX_BUFFER);

    handler.print(String.format("Start upload of %s, needs %d packets",filename, metaData.getNumberOfPackets()));
    System.out.println("Send metadata " + metaData);
    handler.unPause();

    //Put uploadrequest (with file metadata) in the sender buffer
    UDPPacket uploadRequest = protocol.createEmptyPacket();
    uploadRequest.setFlags(Flag.UPLOAD.getValue());
    uploadRequest.setData(metaData.getData());
    protocol.addPacketToSendBuffer(uploadRequest);

    //Tell protocol it can already start sending
    protocol.send();

    //Split file in packets
    try (FileInputStream fileStream = new FileInputStream(file)) {

      for (int packetId = 0; packetId < metaData.getNumberOfPackets(); packetId++) {
        UDPPacket packet = protocol.createEmptyPacket();
        packet.setFlags(Flag.NOT_LAST.getValue());
        packet.setHeaderSetting(HeaderField.OFFSET,packetId); //Count per MAX_BUFFER

        byte[] data = new byte[MAX_BUFFER];
        if(fileStream.read(data) == -1) {
          //NO more bytes to read, send packet with Flag.NOT_LAST = false
          packet.setFlags(0);
          //TODO add packet checksum/md5?
        } else {
          packet.setData(data);

          //Put each packet in the sender buffer
          protocol.addPacketToSendBuffer(packet);
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

}
