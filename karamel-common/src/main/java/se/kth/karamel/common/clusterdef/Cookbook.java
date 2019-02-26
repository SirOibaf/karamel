package se.kth.karamel.common.clusterdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.kth.karamel.common.exception.ValidationException;

public class Cookbook {

  private String github;
  private String branch;

  private String localPath;

  public Cookbook() {
  }

  public Cookbook(String github, String branch) {
    this.github = github;
    this.branch = branch;
  }

  public Cookbook(String localPath) {
    this.localPath = localPath;
  }

  public String getGithub() {
    return github;
  }

  public void setGithub(String github) {
    this.github = github;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getLocalPath() {
    return localPath;
  }

  public void setLocalPath(String localPath) {
    this.localPath = localPath;
  }

  @JsonIgnore
  public CookbookType getCookbookType() {
    if (localPath != null) {
      return CookbookType.LOCAL;
    }

    return CookbookType.GIT;
  }

  public void validate() throws ValidationException {
    if (localPath != null && github != null) {
      throw new ValidationException("For each cookbook specify either the " +
          "localPath or the github repository");
    }
  }

  public enum CookbookType {
    GIT,
    LOCAL
  }
}
