package se.kth.karamel.core.stats;

public class ClusterStatistics {

  private static long startTime = 0;

  public static void startTimer() {
    startTime = System.currentTimeMillis();
  }

  public static long stopTimer() {
    return System.currentTimeMillis() - startTime;
  }
}
