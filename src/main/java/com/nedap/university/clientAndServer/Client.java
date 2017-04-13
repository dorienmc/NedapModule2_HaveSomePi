package com.nedap.university.clientAndServer;

import com.nedap.university.clientAndServer.commands.*;
import java.util.Scanner;

/**
 * Client that handles user input on clientside and responds to this.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class Client extends Handler {
  volatile boolean running;

  public Client() {
    super(9292,9293);
    running = true;

    //Add commands
    addCommand(new ExitCommand());
    addCommand(new HelpCommand());
    addCommand(new ConnectCommandClient());
    addCommand(new ListFilesCommandClient());
    addCommand(new DownloadCommandClient());
    addCommand(new UploadCommandClient());
    addCommand(new PauseCommand());
    addCommand(new ResumeCommand());
  }

  @Override
  public void run() {
    while(getChannel() == null) {
      //Find host and create Reliable Udp Channel
      ConnectCommandClient connector = new ConnectCommandClient();
      connector.execute(this);

      if (getChannel() == null) {
        print("Could not connect to host " + connector.getHostName() + ".");
        super.removeChannel();
      }
    }

    if(running) {
      //List commands
      (new HelpCommand()).execute(this);
    }

    while(running) {
      //Wait for new user command
      String command = Utils.readString("Please enter a command (type help for a menu) ");

      super.handleCommand(command);
    }


  }

  public void shutdown() { //TODO add more?
    super.removeChannel();
    running = false;
    System.exit(0);
  }

  synchronized public boolean isRunning() {
    return running;
  }
}
