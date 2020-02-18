package se.kth.karamel.core.execution;

import lombok.Getter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TaskOutputReader implements Runnable {

  private final static Logger LOGGER = Logger.getLogger(TaskOutputReader.class.getName());

  @Getter
  private StringBuilder output = new StringBuilder();

  // TODO(fabio) this needs to be some sort of a fifo
  private InputStream stream;

  public TaskOutputReader(InputStream stream) {
    this.stream = stream;
  }

  // TODO(Fabio): handle the case the task is retried
  @Override
  public void run() {
    byte[] data = new byte[1024];
    try {
      while (stream.read(data, 0, data.length) != -1) {
        output.append(new String(data, StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      LOGGER.warn("Error reading task output: ", e);
    }
  }
}
