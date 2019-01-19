/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.ValidationException;

/**
 * Represents Chef metadata.rb file
 *
 * @author kamal
 */
public class MetadataRb {

  private String name;
  private String description;
  private String version;
  private List<Recipe> recipes = new ArrayList<>();
  private List<Attribute> attributes = new ArrayList<>();
  
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
  
  public void setDefaults(DefaultRb defaultRb) {
    for (Attribute attr : attributes) {
      if (defaultRb.getValue(attr.getName()) != null) {
        attr.setDefault(defaultRb.getValue(attr.getName()));
      }
    }
  }
  
  public void normalizeRecipeNames() throws ValidationException {
    if (this.name == null || this.name.isEmpty()) {
      throw new ValidationException("name of cookbook is mandatory in metadata file");
    }
    for (Recipe recipe : recipes) {
      if (!recipe.getName().contains(Settings.COOKBOOK_DELIMITER)) {
        if (!recipe.getName().equals(name)) {
          recipe.setName(name + Settings.COOKBOOK_DELIMITER + recipe.getName());
        }
      }
      
    }
  }
  
}
