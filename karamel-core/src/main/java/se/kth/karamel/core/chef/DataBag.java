package se.kth.karamel.core.chef;

import java.util.HashMap;
import java.util.Map;

public class DataBag extends HashMap<String, Object> {

  public DataBag(Map<String, Object> initMap) {
    super(initMap);
  }

  /**
   * Recursively merge the hashmap - only 1 thread thread at the time should be able to merge
   * @param dataBag
   */
  public synchronized void merge(Map<String, Object> dataBag) {
    mapInternal(this, dataBag);
  }

  private void mapInternal(Map<String, Object> current, Map<String, Object> dataBag) {
    for (Map.Entry<String, Object> dataBagEntry : dataBag.entrySet()) {
      Object currentKeyValue = current.get(dataBagEntry.getKey());

      if ((currentKeyValue == null && dataBagEntry.getValue() instanceof Map) ||
           currentKeyValue != null && !(currentKeyValue instanceof Map)) {
        // The current databag does not contain the key or the current value is not a map, just insert/overwrite
        current.put(dataBagEntry.getKey(), dataBagEntry.getValue());
      } else {
        // Recusively merge
        mapInternal((Map<String, Object>)current.get(dataBagEntry.getKey()),
            (Map<String, Object>)dataBagEntry.getValue());
      }
    }
  }
}
