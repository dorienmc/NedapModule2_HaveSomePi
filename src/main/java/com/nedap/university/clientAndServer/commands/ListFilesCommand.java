package com.nedap.university.clientAndServer.commands;

/**
 * Created by dorien.meijercluwen on 10/04/2017.
 */
public class ListFilesCommand extends Command{

  public ListFilesCommand() {
    super(Keyword.LS, "List files");
  }

  @Override
  public void execute(Object parameters) {
    //TODO different for both sides?
    //Eg on client -> send request to list files
    //On pi -> list all files
  }

}
