package se.kth.karamel.core.execution;

import java.io.IOException;

public class NoopTask extends Task {
  @Override
  void execute() throws ExecutionException, IOException {
    // This is just for testing, do nothing here.
  }
}
