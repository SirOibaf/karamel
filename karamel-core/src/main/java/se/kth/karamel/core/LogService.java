package se.kth.karamel.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import se.kth.karamel.core.running.model.tasks.Task;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;

public class LogService {

  private static final Logger logger = Logger.getLogger(LogService.class);

  public static void cleanup(String clusterName) {
    logger.debug(String.format("Trashing old logs of '%s'", clusterName));
    String path = Settings.CLUSTER_LOG_FOLDER(clusterName);
    try {
      FileUtils.deleteDirectory(new File(path));
    } catch (IOException jex) {
    }
  }

  public static void serializeTaskLog(Task task, String machineIp, InputStream log) {
    String publicIp = task.getMachine().getPublicIp();
    String clusterName = task.getMachine().getGroup().getCluster().getName();
    String logFilePath = Settings.TASK_LOG_FILE_PATH(clusterName, publicIp, task.getName());

    File folder = new File(Settings.MACHINE_LOG_FOLDER(clusterName, machineIp));

    if (!folder.exists()) {
      folder.mkdirs();
    }
    appendToFile(logFilePath, log);
  }

  private static void appendToFile(String filePath, InputStream in) {
    try {
      File file = new File(filePath);
      file.createNewFile();
      try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
        final byte[] buffer = new byte[65536];
        int r;
        while ((r = in.read(buffer)) > 0) {
          String decoded = new String(buffer, "UTF-8");
          pw.print(decoded);
        }

      } catch (IOException e) {
        logger.error("", e);
      }
    } catch (IOException e) {
      logger.error("", e);
    }
  }

  public static String loadLog(String clusterName, String publicIp, String taskName) throws KaramelException {
    String errFilPath = Settings.TASK_LOG_FILE_PATH(clusterName, publicIp, taskName);
    return loadLogFile(errFilPath);
  }

  public static String loadLogFile(String filePath) throws KaramelException {
    byte[] encoded;
    try {
      encoded = Files.readAllBytes(Paths.get(filePath));
      return new String(encoded, "UTF-8");
    } catch (IOException ex) {
      throw new KaramelException(String.format("Couldn't read the log file '%s'", filePath), ex);
    }
  }
}
