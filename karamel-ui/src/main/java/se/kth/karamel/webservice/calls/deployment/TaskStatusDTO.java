package se.kth.karamel.webservice.calls.deployment;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.core.execution.FetchCookbooksTask;
import se.kth.karamel.core.execution.RunRecipeTask;
import se.kth.karamel.core.execution.Task;
import se.kth.karamel.core.execution.TaskStatus;

public class TaskStatusDTO {

  @Getter @Setter
  private int taskId;
  @Getter @Setter
  private TaskType taskType;
  @Getter @Setter
  private TaskStatus taskStatus;
  @Getter @Setter
  private String recipeName;
  @Getter @Setter
  private long queueLength;

  public TaskStatusDTO(Task task) {
    this.taskId = task.getTaskId();
    this.taskStatus = task.getTaskStatus();
    this.taskType = getType(task);
    this.recipeName = getRecipe(task);
    this.queueLength = task.getDependsOn().stream().filter(t ->
      t.getTaskStatus().equals(TaskStatus.WAITING) || t.getTaskStatus().equals(TaskStatus.SCHEDULED)
        || t.getTaskStatus().equals(TaskStatus.RUNNING)).count();
  }

  private TaskType getType(Task task) {
    if (task instanceof FetchCookbooksTask) {
      return TaskType.FETCH_COOKBOOKS;
    } else if (task instanceof RunRecipeTask) {
      return TaskType.RUN_RECIPE;
    } else {
      return TaskType.NODE_SETUP;
    }
  }

  private String getRecipe(Task task) {
    if (task instanceof RunRecipeTask) {
      return ((RunRecipeTask) task).getRecipe().getCanonicalName();
    } else {
      return null;
    }
  }

  enum TaskType {
    NODE_SETUP,
    FETCH_COOKBOOKS,
    RUN_RECIPE;
  }
}
