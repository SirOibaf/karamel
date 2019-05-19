package se.kth.karamel.common.cookbookmeta;

import lombok.Getter;
import lombok.Setter;

public class KaramelizedCookbook {

  @Getter @Setter
  private String cookbookName;
  @Getter @Setter
  private MetadataRb metadataRb;
  @Getter @Setter
  private KaramelFile karamelFile;

  public KaramelizedCookbook(MetadataRb metadata, KaramelFile karamelFile) {
    this.cookbookName = metadata.getName();
    this.metadataRb = metadata;
    this.karamelFile = karamelFile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KaramelizedCookbook that = (KaramelizedCookbook) o;

    return cookbookName != null ? cookbookName.equals(that.cookbookName) : that.cookbookName == null;

  }

  @Override
  public int hashCode() {
    return cookbookName != null ? cookbookName.hashCode() : 0;
  }
}
