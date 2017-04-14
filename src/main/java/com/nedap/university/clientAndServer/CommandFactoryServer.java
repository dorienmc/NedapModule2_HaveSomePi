package com.nedap.university.clientAndServer;


import com.nedap.university.clientAndServer.commands.*;

/**
 * Created by dorien.meijercluwen on 14/04/2017.
 */
public class CommandFactoryServer extends CommandFactory {

  public CommandFactoryServer(Handler handler) {
    super(handler);

    addCommand(new ExitCommand(handler, new Byte((byte) 0)));
    addCommand(new ListFilesCommandServer(handler, new Byte((byte) 0)));
    addCommand(new DownloadCommandServer(handler, new Byte((byte) 0)));
    addCommand(new UploadCommandServer(handler, new Byte((byte) 0)));
    addCommand(new PauseCommand(handler, new Byte((byte) 0)));
    addCommand(new ResumeCommand(handler, new Byte((byte) 0)));
    addCommand(new CancelCommand(handler, new Byte((byte) 0)));
  }

  /**
   * Create new Server command.
   * @param keyword
   * @return Corresponding command or null.
   */
  @Override
  public Command createCommand(Keyword keyword, Byte requestId) {
    switch (keyword) {
      case EXIT:    return new ExitCommand(handler, requestId);
      case LS:      return new ListFilesCommandServer(handler, requestId);
      case DOWNLOAD:return new DownloadCommandServer(handler, requestId);
      case UPLOAD:  return new UploadCommandServer(handler, requestId);
      case PAUSE:   return new PauseCommand(handler, requestId);
      case RESUME:  return new ResumeCommand(handler, requestId);
      case CANCEL:  return new CancelCommand(handler, requestId);
      default:      return null;
    }
  }


}