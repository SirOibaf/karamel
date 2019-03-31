package se.kth.karamel.common.cookbookmeta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

// Ignore all the properties not specified in this class
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attribute {
  private String type;
  private String description;
  @SerializedName("default")
  private Object defaultVal;
  private String required;

  public Attribute() { }

  public void setDefault(Object defaultVal) {
    this.defaultVal = defaultVal;
  }

  public Object getDefault() {
    return defaultVal;
  }

  public void setRequired(String required) {
    this.required = required;
  }

  public String getRequired() {
    return required;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
