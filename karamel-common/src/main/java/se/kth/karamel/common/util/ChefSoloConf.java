package se.kth.karamel.common.util;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class ChefSoloConf {

  private static boolean generated = false;

  // TODO(Fabio): this code does not allow for machine specific configuration. So, for instance,
  //  we can't have different users on different machines

  // Generate a solo.rb configuration file
  public synchronized static String getConfLocalPath(Settings settings, String user) throws IOException{
    Path tmpSoloConf = Paths.get(settings.get(Settings.SettingsKeys.WORKING_DIR),
        Constants.KARAMEL_TMP_DIRNAME, Constants.SOLO_CONF_NAME);
    if (!generated) {
      templateLocalConf(tmpSoloConf, settings, user);
      generated = true;
    }

    return tmpSoloConf.toString();
  }

  private static void templateLocalConf(Path tmpSoloConf, Settings settings, String user) throws IOException {

    // Add user defined configuration
    Set<String> soloConf = settings.getConfMap().entrySet().stream()
        .filter(e -> e.getKey().startsWith("solo."))
        .map(e -> e.getKey().substring(e.getKey().indexOf(".") + 1) + " \"" + e.getValue() + "\"")
        .collect(Collectors.toSet());

    // Add cookbook path - static
    Path remoteCookbookPath = Paths.get("home", user,
        Constants.REMOTE_WORKING_DIR_NAME, Constants.REMOTE_COOKBOOKS_DIR_NAME);
    soloConf.add("cookbook_path [\"" + remoteCookbookPath.toString() + "\"]");

    FileUtils.writeLines(tmpSoloConf.toFile(), soloConf);
  }
}
