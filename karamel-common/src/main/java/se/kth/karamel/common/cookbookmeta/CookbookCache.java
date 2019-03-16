package se.kth.karamel.common.cookbookmeta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import se.kth.karamel.common.util.ProcOutputConsumer;
import se.kth.karamel.common.util.Settings;

import static se.kth.karamel.common.clusterdef.Cookbook.CookbookType.GIT;

public class CookbookCache {

  private static final Logger logger = Logger.getLogger(CookbookCache.class);

  private Map<String, KaramelizedCookbook> cookbooks = new HashMap<>();
  private ObjectMapper objectMapper = new ObjectMapper();
  private ExecutorService es = Executors.newFixedThreadPool(2);

  //TODO(Fabio): This is a singleton. - Maybe it doesn't need to be.

  private static CookbookCache cookbookCacheInstance = null;

  private CookbookCache() { }

  public static CookbookCache getInstance() {
    if (cookbookCacheInstance == null) {
      cookbookCacheInstance = new CookbookCache();
    }

    return cookbookCacheInstance;
  }

  public KaramelizedCookbook get(String cookbookName) throws KaramelException {
    KaramelizedCookbook cb = cookbooks.get(cookbookName);
    if (cb == null) {
      throw new NoKaramelizedCookbookException(
          String.format("Cookbook could not be found '%s'", cookbookName));
    }
    return cb;
  }

  @VisibleForTesting
  public void addToCache(String cookbookName, KaramelizedCookbook kbc) {
    cookbooks.put(cookbookName, kbc);
  }

  public List<KaramelizedCookbook> getKaramelizedCookbooks() {
    return new ArrayList<>(cookbooks.values());
  }

  public List<KaramelizedCookbook> loadKaramelizedCookbooks(Map<String, Cookbook> rootCookbooks)
      throws KaramelException, IOException {

    File workingDir = Paths.get(Settings.WORKING_DIR).toFile();
    if (workingDir.exists()) {
      // Clean up directory
      FileUtils.deleteDirectory(workingDir);
      workingDir.mkdir();
    } else {
      workingDir.mkdir();
    }

    for (Map.Entry<String, Cookbook> cookbook : rootCookbooks.entrySet()) {
      if (cookbook.getValue().getCookbookType() == GIT) {
        cloneAndVendorCookbook(cookbook);
      } else {
        // Build cookbook objects for local cookbook
        buildCookbookObjects(cookbook.getValue().getLocalPath());
      }
    }

    // Load all cookbooks which were vendored
    buildCookbookObjects(Settings.WORKING_DIR);

    return new ArrayList<>(cookbooks.values());
  }

  private void cloneAndVendorCookbook(Map.Entry<String, Cookbook> cookbook) throws KaramelException {
    // Clone the repository
    Path cbTargetDir = Paths.get(Settings.WORKING_DIR, cookbook.getKey());

    try {
      if (!cbTargetDir.toFile().exists()) {
        Git.cloneRepository()
            // TODO(Fabio): make base url as setting in the cluster definition
            // So we can support also GitLab/Bitbucket and so on.
            .setURI(Settings.GITHUB_BASE_URL + "/" + cookbook.getValue().getGithub())
            .setBranch(cookbook.getValue().getBranch())
            .setDirectory(Paths.get(Settings.WORKING_DIR, cookbook.getKey()).toFile())
            .call();
      }
    } catch (GitAPIException e) {
      throw new KaramelException(e);
    }

    // Vendor the repository
    try {
      Process vendorProcess = Runtime.getRuntime().exec("berks vendor --berksfile=" +
          Paths.get(Settings.WORKING_DIR, cookbook.getKey(), "Berksfile") + " " + Settings.WORKING_DIR);

      Future<String> vendorOutput = es.submit(new ProcOutputConsumer(vendorProcess.getInputStream()));
      vendorProcess.waitFor(10, TimeUnit.MINUTES);

      if (vendorProcess.exitValue() != 0) {
        throw new KaramelException("Fail to vendor the cookbook: " + cookbook.getKey() + " " + vendorOutput.get());
      }
    } catch (IOException | InterruptedException | ExecutionException e) {
      throw new KaramelException(e);
    }
  }

  private void buildCookbookObjects(String cookbooksPath) throws KaramelException {
    try (Stream<Path> paths = Files.find(Paths.get(Settings.WORKING_DIR),
        Integer.MAX_VALUE, (path, attributes) -> attributes.isDirectory())) {
      paths.forEach(path -> {
          File rawKaramelFile = path.resolve("Karamelfile").toFile();
          File rawMetadataRb = path.resolve("metadata.rb").toFile();
          if (rawKaramelFile.exists()) {
            try {
              KaramelFile karamelFile = new KaramelFile(com.google.common.io.Files.toString(
                  rawKaramelFile, Charsets.UTF_8));
              MetadataRb metadataRb = objectMapper.readValue(com.google.common.io.Files.toString(
                  rawMetadataRb, Charsets.UTF_8), MetadataRb.class);
              cookbooks.put(metadataRb.getName(), new KaramelizedCookbook(metadataRb, karamelFile));
            } catch (IOException | MetadataParseException e) {
              logger.error(e);
              throw new RuntimeException(e);
            }
          }
        });
    } catch (IOException e) {
      throw new KaramelException(e);
    }
  }
}
