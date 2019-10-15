package se.kth.karamel.core.chef;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.ChefSoloConf;
import se.kth.karamel.common.util.Settings;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class TestChefSoloGen {

  @After
  public void afterTests() {
    ChefSoloConf.resetFlag();
  }

  @Test
  public void testBaseConf() throws KaramelException, IOException {
    Settings settings = new Settings();

    String confFilePath = ChefSoloConf.getConfLocalPath(settings, "fabio");
    String confFileContent = FileUtils.readFileToString(new File(confFilePath));
    assertTrue(confFileContent.contains("cookbook_path"));
    assertTrue(confFileContent.contains(settings.get(Settings.SettingsKeys.SOLO_FILE_CACHE_PATH)));
  }

  @Test
  public void testUserDefConf() throws KaramelException, IOException {
    Settings settings = new Settings();
    settings.set("solo.property", "ciao");

    String confFilePath = ChefSoloConf.getConfLocalPath(settings, "fabio");
    String confFileContent = FileUtils.readFileToString(new File(confFilePath));
    assertTrue(confFileContent.contains("cookbook_path"));
    assertTrue(confFileContent.contains(settings.get(Settings.SettingsKeys.SOLO_FILE_CACHE_PATH)));
    assertTrue(confFileContent.contains("property"));
  }
}
