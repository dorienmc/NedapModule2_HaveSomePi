package com.nedap.university.clientAndServer;


import com.nedap.university.clientAndServer.commands.*;
import com.nedap.university.clientAndServer.commands.server.CancelCommandServer;
import com.nedap.university.clientAndServer.commands.server.ListFilesCommandServer;
import com.nedap.university.clientAndServer.commands.server.UploadCommandServer;

/**
 * Created by dorien.meijercluwen on 14/04/2017.
 */
public class CommandFactoryServer extends CommandFactory {
  public final static Byte ZERO = new Byte((byte)0);

  public CommandFactoryServer(Handler handler) {
    super(handler);

    addCommand(new ListFilesCommandServer(handler, ZERO));
    addCommand(new DownloadCommandServer(handler, ZERO));
    addCommand(new UploadCommandServer(handler, ZERO));
    addCommand(new PauseCommand(handler, ZERO));
    addCommand(new ResumeCommand(handler, ZERO));
    addCommand(new CancelCommandServer(handler, ZERO));
  }

  /**
   * Create new Server command.
   * @param keyword
   * @return Corresponding command or null.
   */
  @Override
  public Command createCommand(Keyword keyword, Byte requestId) {
    switch (keyword) {
      case LS:      return new ListFilesCommandServer(handler, requestId);
      case DOWNLOAD:return new DownloadCommandServer(handler, requestId);
      case UPLOAD:  return new UploadCommandServer(handler, requestId);
      case PAUSE:   return new PauseCommand(handler, requestId);
      case RESUME:  return new ResumeCommand(handler, requestId);
      case CANCEL:  return new CancelCommandServer(handler, requestId);
      default:      return null;
    }
  }


}
