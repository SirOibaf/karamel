package se.kth.karamel.core.node;

import net.schmizz.sshj.connection.channel.direct.Session;

import java.io.IOException;

public interface Node {

  String getWorkDir();
  String getUser();

  Session.Command execCommand(String command, boolean requiresRoot) throws IOException;
  void scpFile(String localPath, String targetPath) throws IOException;

  String getHostAddr();

}
