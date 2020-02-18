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

  // TODO(Fabio): Implement locking mechanism for task status manipulation
  @Getter
  private TaskStatus taskStatus = TaskStatus.WAITING;

  @Getter @Setter
  protected Node node;

  @Getter @Setter
  private List<Task> dependsOn = new ArrayList<>();

  @Getter
  protected TaskOutputReader taskOutputReader;

  protected Settings settings;

  abstract void execute() throws ExecutionException, IOException;

  public synchronized void setTaskStatus(TaskStatus taskStatus) {
    this.taskStatus = taskStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof Task)) return false;

    Task task = (Task) o;

    return taskId == task.getTaskId();
  }

  @Override
  public int hashCode() {
    return taskId;
  }
}
