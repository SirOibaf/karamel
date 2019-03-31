package se.kth.karamel.common.cookbookmeta;

public class KaramelizedCookbook {

  private String cookbookName;
  private MetadataRb metadataRb;
  private KaramelFile karamelFile;
  private String json;

  public KaramelizedCookbook(MetadataRb metadata, KaramelFile karamelFile) {
    this.cookbookName = metadata.getName();
    this.metadataRb = metadata;
    this.karamelFile = karamelFile;
  }

  public MetadataRb getMetadataRb() {
    return metadataRb;
  }

  public KaramelFile getKaramelFile() {
    return karamelFile;
  }

  public String getCookbookName() {
    return cookbookName;
  }

  public void setCookbookName(String cookbookName) {
    this.cookbookName = cookbookName;
  }

  public void setMetadataRb(MetadataRb metadataRb) {
    this.metadataRb = metadataRb;
  }

  public void setKaramelFile(KaramelFile karamelFile) {
    this.karamelFile = karamelFile;
  }
}
