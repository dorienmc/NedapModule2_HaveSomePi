package com.nedap.university.clientAndServer;

import com.nedap.university.Utils;
import com.nedap.university.clientAndServer.commands.*;
import com.nedap.university.clientAndServer.commands.client.ConnectCommandClient;

/**
 * Client that handles user input on clientside and responds to this.
 * Created by dorien.meijercluwen on 09/04/2017.
 */
public class Client extends Handler {
  public static final String FILEPATH = "./files";

  public Client() {
    super(9292,9293);
    setStatus(Status.RUNNING);
    super.setCommandFactory(new CommandFactoryClient(this));
  }

  @Override
  public void run() {
    handleTerminalInput();
  }

  public void handleTerminalInput() {
    while(getChannel() == null) {
      //Find host and create Reliable Udp Channel
      ConnectCommandClient connector = new ConnectCommandClient(this, new Byte((byte)0));
      connector.execute();

      if (getChannel() == null) {
        print("Could not connect to host " + connector.getHostName() + ".");
        super.removeChannel();
      }
    }

    if(isRunning()) {
      //List commands
      (new HelpCommand(this)).execute();
    }

    while(isRunning()) {
      //Wait for new user command
      String command = Utils.readString("Please enter a command (type help for a menu) ");

      setStatus(Status.PAUSED);
      super.handleCommand(command);

      //Sleep until command is done asking stuff
      while(getStatus().equals(Status.PAUSED)) {
        Utils.sleep(100);
      }

    }
  }

  /**
   * Handle it when sender or receiver breaks down because it cannot reach the socket.
   **/
  public void handleSocketException (String errorMessage) {
    print(errorMessage);
    //TODO what now? Restart connection?
  }


  public void shutdown() { //TODO add more?
    super.removeChannel();
    setStatus(Status.STOPPED);
    System.exit(0);
  }

  synchronized public boolean isRunning() {
    return !getStatus().equals(Status.STOPPED);
  }
}
