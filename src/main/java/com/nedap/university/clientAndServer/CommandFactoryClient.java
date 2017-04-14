package com.nedap.university.clientAndServer;


import com.nedap.university.clientAndServer.commands.*;

/**
 * Created by dorien.meijercluwen on 14/new Byte((byte) 0)4/2new Byte((byte) 0)17.
 */
public class CommandFactoryClient extends CommandFactory {

  public CommandFactoryClient(Handler handler) {
    super(handler);

    addCommand(new ExitCommand(handler,new Byte((byte) 0)));
    addCommand(new HelpCommand(handler));
    addCommand(new ConnectCommandClient(handler,new Byte((byte) 0)));
    addCommand(new ListFilesCommandClient(handler,new Byte((byte) 0)));
    addCommand(new DownloadCommandClient(handler,new Byte((byte) 0)));
    addCommand(new UploadCommandClient(handler,new Byte((byte) 0)));
    addCommand(new PauseCommand(handler,new Byte((byte) 0)));
    addCommand(new ResumeCommand(handler,new Byte((byte) 0)));
    addCommand(new CancelCommand(handler,new Byte((byte) 0)));
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
