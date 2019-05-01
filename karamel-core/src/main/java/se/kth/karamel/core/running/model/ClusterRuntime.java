package se.kth.karamel.core.running.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;

public class ClusterRuntime {

  public static enum ClusterPhases {
    NOT_STARTED, PRECLEANING, PRECLEANED, FORKING_GROUPS, GROUPS_FORKED, FORKING_MACHINES, MACHINES_FORKED, 
    RUNNING_DAG, DAG_DONE, TERMINATING ;
  }
  
  private final String name;
  
  private ClusterPhases phase = ClusterPhases.NOT_STARTED;
  
  private boolean paused = false;

  private List<GroupRuntime> groups = new ArrayList<>();
  
  private final Map<String, Failure> failures = new HashMap<>();
  
  public ClusterRuntime(String name) {
    this.name = name;
  }

  public ClusterRuntime(Cluster definition) {
    this.name = definition.getName();
    for (Group group : definition.getGroups()) {
      GroupRuntime groupRuntime = new GroupRuntime(this, group.getName());
      groups.add(groupRuntime);
    }
  }

  public String getName() {
    return name;
  }
  
  public synchronized void setGroups(List<GroupRuntime> groups) {
    this.groups = groups;
  }

  public List<GroupRuntime> getGroups() {
    return groups;
  }

  public ClusterPhases getPhase() {
    return phase;
  }

  public synchronized void setPhase(ClusterPhases phase) {
    this.phase = phase;
  }

  public boolean isFailed() {
    return !failures.isEmpty();
  }

  public synchronized void issueFailure(Failure failure) {
    failures.put(failure.hash(), failure);
  }
  
  public synchronized void resolveFailure(String hash) {
    failures.remove(hash);
  }
  
  public synchronized void resolveFailures() {
    failures.clear();
  }

  public Map<String, Failure> getFailures() {
    return failures;
  }

  public boolean isPaused() {
    return paused;
  }

  public synchronized void setPaused(boolean paused) {
    this.paused = paused;
  }

}
