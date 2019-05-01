package se.kth.karamel.core.execution;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.node.Node;

import java.io.IOException;
import java.util.List;

public abstract class Task {

  @Getter
  private int taskId;

  @Getter @Setter
  private TaskStatus taskStatus;

  @Getter @Setter
  protected Node node;

  @Getter @Setter
  private List<Task> dependsOn;
  @Getter @Setter
  private List<Task> block;

  protected Settings settings;

  abstract void execute() throws ExecutionException, IOException;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Task task = (Task) o;

    return taskId == task.getTaskId();
  }

  @Override
  public int hashCode() {
    return taskId;
  }
}
