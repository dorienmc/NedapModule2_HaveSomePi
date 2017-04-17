package com.nedap.university.clientAndServer.commands.client;

import static com.nedap.university.clientAndServer.commands.helpers.FileSendingHelper.uploadFile;
import static com.nedap.university.fileTranser.UDPPacket.MAX_PAYLOAD;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Handler;
import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.Command;
import com.nedap.university.clientAndServer.commands.Keyword;
import com.nedap.university.clientAndServer.commands.helpers.FileMetaData;
import com.nedap.university.fileTranser.Flag;
import com.nedap.university.fileTranser.MyUDPHeader.HeaderField;
import com.nedap.university.fileTranser.UDPPacket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class UploadCommandClient extends Command {

  public UploadCommandClient(Handler handler, Byte requestId) {
    super(Keyword.UPLOAD, "Upload specific file", handler, requestId);
  }

  @Override
  public void execute() {
    handler.print(Utils.listFiles(Client.FILEPATH,"Files available for upload"));
    String filename = Utils.readString("Which file do you want to upload (enter name or id)? ");

    if(Utils.isANumber(filename)) {
      //Parse number to actual file
      filename = Utils.getFile(Client.FILEPATH, Integer.parseInt(filename));
    }

    //Retrieve file, if possible
    File file = new File(Client.FILEPATH + "/" + filename);
    if(!file.exists()) {
      handler.print("File not found: " + filename);
      shutdown();
    }

    //Create file meta data
    FileMetaData metaData = new FileMetaData(file, MAX_PAYLOAD);
    handler.print(String.format("Start upload of %s, needs %d packets",filename, metaData.getNumberOfPackets()));
    System.out.println("Send metadata " + metaData);
    handler.unPause();

    //Put uploadrequest (with file metadata) in the sender buffer
    UDPPacket uploadRequest = protocol.createEmptyPacket();
    uploadRequest.setFlags(Flag.UPLOAD.getValue());
    uploadRequest.setData(metaData.getData());
    protocol.addPacketToSendBuffer(uploadRequest);

    //Tell protocol it can already start sending
    if(!protocol.isAlive()) {
      protocol.start();
    }

    //Split file in packets
    try {
      uploadFile(file, metaData.getNumberOfPackets(), protocol);
    } catch (IOException e) {
      handler.print("Could not upload " + filename + ", " + e.getMessage());
    }

    shutdown();
  }


}
