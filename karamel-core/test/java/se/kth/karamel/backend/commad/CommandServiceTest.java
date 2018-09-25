/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.commad;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.*;
import org.junit.Test;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.backend.command.CommandService;
import se.kth.karamel.common.util.IoUtils;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;
import static se.kth.karamel.common.util.Settings.REPO_WITH_SUBCOOKBOOK_PATTERN;

/**
 *
 * @author kamal
 */
public class CommandServiceTest {

  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CommandServiceTest.class);
  
  @Test
  public void testCommands() throws KaramelException {
    CommandResponse commandResponse = CommandService.processCommand("home");
    assertNotNull(commandResponse);
    assertNotNull(commandResponse.getResult());
    assertEquals(CommandResponse.Renderer.INFO, commandResponse.getRenderer());
    commandResponse = CommandService.processCommand("help");
    assertNotNull(commandResponse);
    assertNotNull(commandResponse.getResult());
    assertEquals(CommandResponse.Renderer.INFO, commandResponse.getRenderer());
    try {
      CommandService.processCommand("yaml");
      CommandService.processCommand("yaml hadoop");
    } catch (KaramelException e) {
    }
  }
  
  @Test
  public void testOfflineDag() throws IOException, KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String yaml = IoUtils.readContentFromClasspath("se/kth/karamel/client/model/test-definitions/hopsworks.yml");
    ClusterDefinitionService.saveYaml(yaml);
    CommandResponse commandResponse = CommandService.processCommand("tdag hopsworks");
    assertEquals(CommandResponse.Renderer.INFO, commandResponse.getRenderer());
    assertNotNull(commandResponse.getResult());
    logger.info(commandResponse.getResult());
  }
}
