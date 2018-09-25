/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.kandy;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.stats.PhaseStat;
import se.kth.karamel.common.stats.TaskStat;
import se.kth.karamel.common.util.IoUtils;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class KandyRestClientTest {

  @Test
  public void dummyTest() {

  }

  //  @Test
  public void testPushStats() throws IOException {
    ClusterStats stats = new ClusterStats();
    String yml = IoUtils.readContentFromClasspath("se/kth/karamel/client/model/test-definitions/flink.yml");
    stats.setDefinition(yml);
    stats.setUserId("kamal-mac");
    PhaseStat phase = new PhaseStat("PRECLEANING", "SUCCEED", 10 * Settings.SEC_IN_MS);
    stats.addPhase(phase);
    phase = new PhaseStat("FORKING GROUPS", "SUCCEED", 7 * Settings.SEC_IN_MS);
    stats.addPhase(phase);
    phase = new PhaseStat("FORKING MACHINES", "SUCCEED", 20 * Settings.SEC_IN_MS);
    stats.addPhase(phase);
    phase = new PhaseStat("INSTALLING", "SUCCEED", 10 * 60 * Settings.SEC_IN_MS);
    stats.addPhase(phase);
    phase = new PhaseStat("EXPERIMENTING", "SUCCEED", 100 * 60 * Settings.SEC_IN_MS);
    stats.addPhase(phase);
    TaskStat task = new TaskStat("test-task1", "test-machine1", "succeed", 5 * Settings.SEC_IN_MS);
    stats.addTask(task);
    task = new TaskStat("test-task2", "test-machine2", "succeed", 6 * Settings.SEC_IN_MS);
    stats.addTask(task);
    KandyRestClient.pushClusterStats("test-cluster", stats);
    task = new TaskStat("test-task3", "test-machine3", "succeed", 6 * Settings.SEC_IN_MS);
    stats.addTask(task);
    KandyRestClient.pushClusterStats("test-cluster", stats);
  }

  //  @Test
  public void testEstimateCost() throws IOException, KaramelException {
    String yml = IoUtils.readContentFromClasspath("se/kth/karamel/client/model/test-definitions/flink_ec2.yml");
    String cost = KandyRestClient.estimateCost(yml);
    Assert.assertNotNull(cost);
    System.out.println(cost);
  }
}
