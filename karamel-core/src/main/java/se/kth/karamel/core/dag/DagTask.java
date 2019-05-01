package se.kth.karamel.core.dag;

import java.util.Set;

public interface DagTask {

  String dagNodeId();

  void prepareToStart();

  void terminate();
  
  void submit(DagTaskCallback callback);

  Set<String> dagDependencies();
  
  String asJson();
}
