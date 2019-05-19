package se.kth.karamel.common.clusterdef;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.node.Node;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.List;
import java.util.stream.Stream;

public abstract class Provider {

  @Getter @Setter
  protected String username;

  @Getter @Setter
  protected List<Node> nodes = null;

  public abstract void validate() throws ValidationException;

  public void merge(Provider parent) throws IntrospectionException {
    // Get all the getter methods if they return null,
    // i.e. the value for the group provider is not set,
    // get the value from the parent
    Stream.of(Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors())
          .filter(pd -> !(pd.getReadMethod().getName().equals("getClass") ||
            pd.getReadMethod().getName().equals("getNodes")))
        .forEach(pd -> {
            try {
              if (pd.getReadMethod().invoke(this) == null) {
                // If the current value is null, get the parent one
                Object parentValue = pd.getReadMethod().invoke(parent);
                pd.getWriteMethod().invoke(this, parentValue);
              }
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
  }
}
