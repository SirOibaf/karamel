package se.kth.karamel.common.node;

import net.schmizz.sshj.connection.channel.direct.Session;

import java.io.IOException;

public interface Node {

  int getNodeId();
  String getWorkDir();
  String getUser();

  Session.Command execCommand(String command, boolean requiresRoot) throws IOException;
  void scpFile(String localPath, String targetPath) throws IOException;

  String getHostname();
  String getPrivateIP();
  String getPublicIP();


}