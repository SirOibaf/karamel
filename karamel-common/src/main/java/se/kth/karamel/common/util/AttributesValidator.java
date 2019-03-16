package se.kth.karamel.common.util;

import se.kth.karamel.common.cookbookmeta.Attribute;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttributesValidator {
  public void validateAttributes(Map<String, Object> attributeMap) throws KaramelException {

    List<KaramelizedCookbook> cookbooks = CookbookCache.getInstance().getKaramelizedCookbooks();

    // Validate cluster-wide attributes
    Map<String, Object> attributes = flattenAttrs(attributeMap, "");
    Set<Attribute> validAttrs = new HashSet<>();

    // Filtering invalid(not defined in metadata.rb) attributes from yaml model
    // Get all the valid attributes, also for transient dependency
    for (KaramelizedCookbook kcb : cookbooks) {
      validAttrs.addAll(kcb.getMetadataRb().getAttributes());
    }

    Map<String, Object> invalidAttrs = new HashMap<>();
    for (String usedAttr: attributes.keySet()) {
      if (!validAttrs.contains(new Attribute(usedAttr))) {
        invalidAttrs.put(usedAttr, attributes.get(usedAttr));
      }
    }

    if (!invalidAttrs.isEmpty()) {
      throw new ValidationException(String.format("Invalid attributes, all used attributes must be defined " +
          "in metadata.rb files: %s", invalidAttrs.keySet().toString()));
    }
  }

  // TODO(Fabio) this can be probably simplified
  private Map<String, Object> flattenAttrs(Map<String, Object> map, String partialName) throws ValidationException {
    Map<String, Object> flatten = new HashMap<>();
    if (map == null) {
      return flatten;
    }

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = ((partialName.isEmpty()) ? "" : partialName + "/") + entry.getKey();
      Object value = entry.getValue();
      if (value instanceof Map) {
        flatten.putAll(flattenAttrs((Map<String, Object>) value, key));
      } else {
        if (value == null) {
          throw new ValidationException(String.format("attribute '%s' doesn't have any value", key));
        } else if (value instanceof List) {
          List<Object> list = (List<Object>) value;
          flatten.put(key, CollectionsUtil.asStringList(list));
        } else {
          flatten.put(key, value.toString());
        }
      }
    }
    return flatten;
  }
}
