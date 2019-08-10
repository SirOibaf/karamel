package se.kth.karamel.common.util;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import se.kth.karamel.common.exception.KaramelException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Settings {

  private static final Logger LOGGER = Logger.getLogger(Settings.class.getName());

  public enum SettingsKeys {
    // --------------------- GENERAL CONFIGURATION  --------------------- //
    WORKING_DIR("karamel.working.dir", "/tmp/karamel"),
    CHEFDK_VERSION_KEY("karamel.chefdk.version", "2.3.1"),

    // TODO(Fabio): what is this supposed to do?
    SKIP_EXISTINGTASKS_KEY("karamel.skip.existing.tasks", "false"),
    PROVISIONER_CLASS("karamel.provisioner.class",
        "se.kth.karamel.core.provisioner.jcloud.JCloudProvisioner"),
    EXECUTION_THREADS("karamel.execution.threads", "20"),

    // AWS specific configurations
    PREPARE_STORAGES_KEY("karamel.prepare.storages", "false"),

    // Chef solo configuration
    SOLO_FILE_CACHE_PATH("solo.file_cache_path", "/tmp/chef-solo");

    public String keyName;
    public String defaultValue;

    SettingsKeys(String keyName, String defaultValue) {
      this.keyName = keyName;
      this.defaultValue = defaultValue;
    }

  }

  private Map<String, String> confMap = new HashMap<>();

  public Settings() throws KaramelException, IOException {
    // Apply defaults
    for (SettingsKeys settingsKeys : SettingsKeys.values()) {
      confMap.put(settingsKeys.keyName, settingsKeys.defaultValue);
    }

    // Parse the configuration file in KARAMEL_HOME/conf/karamel.conf
    File confFile = Paths.get(System.getenv(Constants.KARAMEL_HOME),
       Constants.KARAMEL_CONF_DIRNAME,
        Constants.KARAMEL_CONF_NAME).toFile();
    if (!confFile.exists()) {
      // Try loading it from the resources
      URL resourceConfFilePath = this.getClass().getResource("/" + Constants.KARAMEL_CONF_NAME);
      if (resourceConfFilePath == null) {
        throw new KaramelException("Could not load Karamel configuration file");
      } else {
        confFile = new File(resourceConfFilePath.getFile());
      }
    }

    String confContent = FileUtils.readFileToString(confFile);

    Set<String> validConfigurations = Arrays.stream(SettingsKeys.values())
        .map(settingsKeys -> settingsKeys.keyName).collect(Collectors.toSet());

    // Iterate over specified configuration, if configuration doesn't exists, log a message
    for (String conf : confContent.split("\n")) {
      String[] confSplits = conf.split("=");

      if (confSplits.length  != 2) {
        LOGGER.log(Level.INFO, "Error processing: " + conf);
      } else if (!validConfigurations.contains(confSplits[0])) {
        LOGGER.log(Level.INFO, "Unrecognized property: " + confSplits[0]);
      } else {
        confMap.put(confSplits[0], confSplits[1]);
      }
    }
  }

  public Map<String, String> getConfMap(){
    return this.confMap;
  }

  public String get(SettingsKeys key) {
    return confMap.get(key.keyName);
  }

  public int getInt(SettingsKeys key) { return Integer.valueOf(confMap.get(key.keyName)); }
}
