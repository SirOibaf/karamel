package se.kth.karamel.core.execution;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.node.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Task {

  @Getter
  protected int taskId;

  @Getter @Setter
  private TaskStatus taskStatus = TaskStatus.WAITING;

  @Getter @Setter
  protected Node node;

  @Getter @Setter
  private List<Task> dependsOn = new ArrayList<>();

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
