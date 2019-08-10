package se.kth.karamel.common.util;

import com.google.common.annotations.VisibleForTesting;
import se.kth.karamel.common.cookbookmeta.Attribute;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttributesValidator {
  public static void validateAttributes(Map<String, Object> attributeMap) throws KaramelException {

    List<KaramelizedCookbook> cookbooks = null;
    try {
      cookbooks = CookbookCache.getInstance().getKaramelizedCookbooks();
    } catch (IOException e) {
      throw new KaramelException(e);
    }

    // Validate cluster-wide attributes
    Map<String, Object> attributes = flattenAttrs(attributeMap, "");
    Map<String, Attribute> validAttrs = new HashMap<>();

    // Filtering invalid(not defined in metadata.rb) attributes from yaml model
    // Get all the valid attributes, also for transient dependency
    for (KaramelizedCookbook kcb : cookbooks) {
      validAttrs.putAll(kcb.getMetadataRb().getAttributes());
    }

    Set<String> missingAttrs = new HashSet<>();
    Set<String> wrongTypeAttrs = new HashSet<>();
    for (Map.Entry<String, Object> usedAttr: attributes.entrySet()) {
      Attribute validAttribute = validAttrs.get(usedAttr.getKey());
      if (validAttribute != null) {
        // If the attribute exists in the metadata.rb, check the type
        Class attributeType = getAttributeClass(validAttribute.getType());
        if (!(attributeType.isInstance(usedAttr.getValue()))) {
          wrongTypeAttrs.add(usedAttr.getKey());
        }
      } else {
        // Add the attribute in the list of invalid attributes
        missingAttrs.add(usedAttr.getKey());
      }
    }

    if (!wrongTypeAttrs.isEmpty()) {
      throw new ValidationException(String.format("Invalid type: %s", wrongTypeAttrs.toString()));
    }

    if (!missingAttrs.isEmpty()) {
      throw new ValidationException(String.format("Invalid attributes, all used attributes must be defined " +
          "in metadata.rb files: %s", missingAttrs.toString()));
    }
  }

  @VisibleForTesting
  public static Map<String, Object> flattenAttrs(Map<String, Object> map, String partialName)
      throws ValidationException {
    Map<String, Object> flatten = new HashMap<>();
    if (map == null) {
      return flatten;
    }

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = ((partialName.isEmpty()) ? "" : partialName + "/") + entry.getKey();
      Object value = entry.getValue();
      if (value == null) {
        throw new ValidationException(String.format("attribute '%s' doesn't have any value", key));
      } else if (value instanceof Map) {
        flatten.putAll(flattenAttrs((Map<String, Object>) value, key));
      } else {
        flatten.put(key, value);
      }
    }

    return flatten;
  }

  // TODO(Fabio): maybe this function is going to be needed somewhere else.
  public static Class getAttributeClass(String attribyteType) {
    // Attribute classes are: "string", "array", "hash", "symbol", "boolean", "numeric"
    // at the moment we don't support symbol and hash
    if (attribyteType.equalsIgnoreCase("String")) {
      return String.class;
    } else if (attribyteType.equalsIgnoreCase("array")) {
      return List.class;
    } else if (attribyteType.equalsIgnoreCase("boolean")) {
      return Boolean.class;
    } else {
      return Number.class;
    }
  }
}
