package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;

public class MetadataRb {

  private String name;
  private String description;
  private String version;
  private List<Recipe> recipes = new ArrayList<>();
  private List<Attribute> attributes = new ArrayList<>();

  public MetadataRb() { }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }
  
  public List<Attribute> getAttributes() {
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
  
  public void setRecipes(List<Recipe> recipes) {
    this.recipes = recipes;
  }
  
  public List<Recipe> getRecipes() {
    return recipes;
  }
  
  public void setVersion(String version) {
    this.version = version;
  }
  
  public String getVersion() {
    return version;
  }
}
