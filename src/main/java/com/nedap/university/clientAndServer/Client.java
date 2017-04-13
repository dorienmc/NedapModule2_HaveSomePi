package com.nedap.university.clientAndServer;

import com.nedap.university.clientAndServer.commands.ConnectCommandClient;
import com.nedap.university.clientAndServer.commands.DownloadCommand;
import com.nedap.university.clientAndServer.commands.ExitCommand;
import com.nedap.university.clientAndServer.commands.HelpCommand;
import com.nedap.university.clientAndServer.commands.ListFilesCommandClient;
import com.nedap.university.clientAndServer.commands.PauseCommand;
import com.nedap.university.clientAndServer.commands.ResumeCommand;
import com.nedap.university.clientAndServer.commands.UploadCommand;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    addCommand(new DownloadCommand());
    addCommand(new UploadCommand());
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

    while(running) {
      //List commands
      (new HelpCommand()).execute(this);

      //Wait for new user command
      String command = readString("Please enter a command ");

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

  /**
   * Writes a prompt to standard out and tries to read an String value from
   * standard in. This is repeated until an String value is entered.
   * @param prompt the question to prompt the user
   * @return the first String value which is entered by the user
   */
  public static String readString(String prompt) {
    String answer;
    @SuppressWarnings("resource")
    Scanner line = new Scanner(System.in);

    do {
      System.out.print(prompt);
      try (Scanner scannerLine = new Scanner(line.nextLine());) {
        answer = scannerLine.hasNext() ? scannerLine.nextLine() : null;
      }
    } while (answer == null);
    return answer;
  }
}
