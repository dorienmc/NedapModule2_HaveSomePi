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
    super.setCommandFactory(new CommandFactoryClient(this));
  }

  @Override
  public void run() {
    while(getChannel() == null) {
      //Find host and create Reliable Udp Channel
      ConnectCommandClient connector = new ConnectCommandClient(this, new Byte((byte)0));
      connector.execute();

      if (getChannel() == null) {
        print("Could not connect to host " + connector.getHostName() + ".");
        super.removeChannel();
      }
    }

    if(running) {
      //List commands
      (new HelpCommand(this)).execute();
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
