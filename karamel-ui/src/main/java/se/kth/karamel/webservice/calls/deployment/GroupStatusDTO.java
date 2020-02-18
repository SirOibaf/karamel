package se.kth.karamel.webservice.calls.deployment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
public class GroupStatusDTO {

  @Getter @Setter
  private String groupName;
  @Getter @Setter
  List<NodeStatusDTO> nodeStatusList;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GroupStatusDTO that = (GroupStatusDTO) o;

    return groupName != null ? groupName.equals(that.groupName) : that.groupName == null;
  }

  @Override
  public int hashCode() {
    return groupName != null ? groupName.hashCode() : 0;
  }
}
