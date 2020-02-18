package se.kth.karamel.common.clusterdef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.util.AttributesValidator;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;

public class Group extends Scope {

  @Getter @Setter
  private String name;

  @Getter @Setter
  private int size;

  @Getter @Setter
  private List<Recipe> recipes = new ArrayList<>();

  public Group(String name) {
    this.name = name;
  }

  public Group() { }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Group group = (Group) o;

    return name != null ? name.equals(group.name) : group.name == null;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  @Override
  public void validate() throws KaramelException, InterruptedException {
    super.validate();

    if (recipes == null || recipes.isEmpty()) {
      throw new ValidationException("The group does not contain any recipe");
    }

    // Validate recipe name
    for (Recipe r : recipes) {
      try {
        r.validate();
      } catch (IOException e) {
        throw new KaramelException(e);
      }
    }

    // Validate duplicated recipes in the cluster definition
    if (recipes.stream().map(Recipe::getCanonicalName).collect(Collectors.toSet()).size() !=
        recipes.size()) {
      throw new ValidationException("Duplicated recipes found in group");
    }

    // Validate Group attributes
    AttributesValidator.validateAttributes(attributes);
  }

  @JsonIgnore
  public Set<KaramelizedCookbook> getKaramelizedCookbooks() {
    return recipes.stream().map(Recipe::getCookbook).collect(Collectors.toSet());
  }
}
