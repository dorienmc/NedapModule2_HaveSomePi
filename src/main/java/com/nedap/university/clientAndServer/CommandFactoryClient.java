package com.nedap.university.clientAndServer;


import com.nedap.university.clientAndServer.commands.*;
import com.nedap.university.clientAndServer.commands.client.*;

/**
 * Created by dorien.meijercluwen on 14/04/2017.
 */
public class CommandFactoryClient extends CommandFactory {
  public final static Byte ZERO = new Byte((byte)0);

  public CommandFactoryClient(Handler handler) {
    super(handler);

    addCommand(new ExitCommand(handler,ZERO));
    addCommand(new HelpCommand(handler));
    addCommand(new ConnectCommandClient(handler,ZERO));
    addCommand(new ListRunningCommandsCommand(handler, ZERO));
    addCommand(new ListFilesCommandClient(handler,ZERO));
    addCommand(new DownloadCommandClient(handler,ZERO));
    addCommand(new UploadCommandClient(handler,ZERO));
    addCommand(new PauseCommand(handler,ZERO));
    addCommand(new ResumeCommand(handler,ZERO));
    addCommand(new CancelCommand(handler,ZERO));
  }

  /**
   * Create new Client command.
   * @param keyword
   * @return Corresponding command or null.
   */
  @Override
  public Command createCommand(Keyword keyword, Byte requestId) {
    switch (keyword) {
      case EXIT:    return new ExitCommand(handler, requestId);
      case HELP:    return new HelpCommand(handler);
      case CONNECT: return new ConnectCommandClient(handler,requestId);
      case LS:      return new ListFilesCommandClient(handler,requestId);
      case DOWNLOAD:return new DownloadCommandClient(handler,requestId);
      case UPLOAD:  return new UploadCommandClient(handler,requestId);
      case PAUSE:   return new PauseCommand(handler,requestId);
      case RESUME:  return new ResumeCommand(handler,requestId);
      case CANCEL:  return new CancelCommand(handler,requestId);
      default:      return null;
    }
  }


}
