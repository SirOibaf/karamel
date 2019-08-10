package se.kth.karamel.core.execution;

import lombok.Getter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.dag.Dag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class has a TaskProducer thread that periodically scans the dag
 * looking for tasks available to be scheduled.
 * And n TaskConsumers which pull form the queue and execute the tasks
 */
public class ExecutionEngine {

  private static final Logger LOGGER = Logger.getLogger(ExecutionEngine.class.getName());

  private Settings settings;
  @Getter
  private ArrayBlockingQueue<Task> tasksQueue;
  private List<AtomicBoolean> nodeBusy;

  private final AtomicBoolean pause;

  private Thread producerThread;
  private List<Thread> consumerThreads;

  public ExecutionEngine(Settings settings) {
    this.settings = settings;
    tasksQueue = new ArrayBlockingQueue<>(10); // TODO(Fabio) make this programmable
    pause = new AtomicBoolean(false);
  }

  public void execute(Dag dag, int totalNodes) {
    nodeBusy = new ArrayList<>(totalNodes);

    producerThread = new Thread(new TaskProducer(dag, tasksQueue));
    producerThread.start();

    int numConsumerThreads = settings.getInt(Settings.SettingsKeys.EXECUTION_THREADS);
    consumerThreads = new ArrayList<>(numConsumerThreads);
    for (int i = 0; i<numConsumerThreads; i++) {
      Thread consumerThread = new Thread(new TaskConsumer(tasksQueue, pause));
      consumerThread.start();
      consumerThreads.add(consumerThread);
    }
  }

  /**
   * Pause execution - running recipes will complete
   */
  public void pause() {
    synchronized (pause) {
      pause.set(true);
    }
  }

  /**
   * Resume execution
   */
  public void resume() {
    synchronized (pause) {
      pause.set(false);
      pause.notifyAll();
    }
  }

  /**
   * Kill all threads
   */
  public void terminate() {
    for (Thread t : consumerThreads) {
      t.interrupt();
    }
  }

  /**
   * Skip a task and free up the machine for the next one
   * @param task
   */
  public void skipTask(Task task) {
    task.setTaskStatus(TaskStatus.SKIPPED);
    nodeBusy.get(task.getNode().getNodeId()).set(false);
  }


  /**
   * Put a task back in the queue for the execution
   * @param task
   */
  public void retryTask(Task task) throws InterruptedException {
    tasksQueue.put(task);
    task.setTaskStatus(TaskStatus.SCHEDULED);
  }

  private class TaskProducer implements Runnable {

    private Dag dag;
    private ArrayBlockingQueue<Task> tasksQueue;

    public TaskProducer(Dag dag, ArrayBlockingQueue<Task> taskQueue) {
      this.dag = dag;
      this.tasksQueue = taskQueue;
    }

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          for (Task task : dag.getSchedulableTasks()) {
            // If the node is not busy, i.e. no other recipe is scheduled/running/failed
            // we can enqueue the task
            if (nodeBusy.get(task.getNode().getNodeId()).compareAndSet(false, true)) {
              tasksQueue.put(task);
              task.setTaskStatus(TaskStatus.SCHEDULED);
            }
          }
        } catch (InterruptedException e) {
          LOGGER.log(Level.INFO, "Could not enqueue tasks", e);
        }
      }
    }
  }

  private class TaskConsumer implements Runnable {

    private ArrayBlockingQueue<Task> tasksQueue;
    private final AtomicBoolean pause;

    public TaskConsumer(ArrayBlockingQueue<Task> tasksQueue, AtomicBoolean pause) {
      this.tasksQueue = tasksQueue;
      this.pause = pause;
    }

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {

          // Check if the execution has been suspended
          synchronized (pause) {
            if (pause.get()) {
              pause.wait();
            }
          }

          Task task = tasksQueue.take();
          task.setTaskStatus(TaskStatus.RUNNING);
          try {
            task.execute();
            // Free the node for new tasks
            nodeBusy.get(task.getNode().getNodeId()).set(false);
          } catch (ExecutionException | IOException e) {
            task.setTaskStatus(TaskStatus.FAILED);
          }
        } catch (InterruptedException e) {
          LOGGER.log(Level.INFO, "Could not consume tasks", e);
        }
      }
    }
  }
}
