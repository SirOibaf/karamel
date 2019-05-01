package se.kth.karamel.core.dag;

import lombok.Getter;
import se.kth.karamel.core.execution.Task;
import se.kth.karamel.core.execution.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Dag {

  @Getter
  private List<Task> taskList = new ArrayList<>();

  public void addTask(Task task) {
    taskList.add(task);
  }

  /**
   * Make sure that the graph doesn't have any dependency cycle
   */
  public boolean hasCycle() {
    boolean[] visited = new boolean[taskList.size()];
    boolean[] cycle = new boolean[taskList.size()];

    for (Task task : taskList) {
      if (!visited[task.getTaskId()] && cyclicUtil(task, visited, cycle)) {
        return true;
      }
    }

    return false;
  }

  private boolean cyclicUtil(Task task, boolean[] visited, boolean[] cycle) {
    visited[task.getTaskId()] = true;
    cycle[task.getTaskId()] = true;

    for (Task dependency : task.getDependsOn()) {
      if (!visited[dependency.getTaskId()] && cyclicUtil(dependency, visited, cycle)) {
        return true;
      } else if (cycle[dependency.getTaskId()]) {
        return true;
      }
    }

    cycle[task.getTaskId()] = false;
    return false;
  }

  /**
   * Iterate over the graph and find tasks which
   *   1. haven't been executed yet
   *   2. have either no dependencies or all the dependencies ran successfully.
   * @return Task
   */
  public Set<Task> getSchedulableTasks() {
    return taskList.stream()
        .filter(task -> task.getTaskStatus() == TaskStatus.WAITING)
        .filter(task -> task.getDependsOn().isEmpty() ||
            task.getDependsOn().stream().noneMatch(dependency -> dependency.getTaskStatus() != TaskStatus.SUCCESS))
        .collect(Collectors.toSet());
  }
}
