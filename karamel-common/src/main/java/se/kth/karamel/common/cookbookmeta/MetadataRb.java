package se.kth.karamel.common.cookbookmeta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

// Ignore all the properties not specified in this class
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataRb {

  private String name;
  private String description;
  private String version;
  private Map<String, String> recipes = new HashMap<>();
  private Map<String, Attribute> attributes = new HashMap<>();

  public MetadataRb() { }

  public void setAttributes(Map<String, Attribute> attributes) {
    this.attributes = attributes;
  }
  
  public Map<String,Attribute> getAttributes() {
    return attributes;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setRecipes(Map<String, String> recipes) {
    this.recipes = recipes;
  }
  
  public Map<String, String> getRecipes() {
    return recipes;
  }
  
  public void setVersion(String version) {
    this.version = version;
  }
  
  public String getVersion() {
    return version;
  }
}
