package se.kth.karamel.webservice;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;

import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.SSHKeyPair;
import se.kth.karamel.webservice.calls.cluster.ClusterService;
import se.kth.karamel.webservice.calls.cluster.UploadService;
import se.kth.karamel.webservice.calls.sshkeys.SSHKeys;
import se.kth.karamel.webservice.calls.sshkeys.SetSudoPassword;
import se.kth.karamel.webservice.calls.system.ExitKaramel;
import se.kth.karamel.webservice.calls.system.PingServer;
import se.kth.karamel.webservice.utils.TemplateHealthCheck;

public class KaramelServiceApplication extends Application<KaramelServiceConfiguration> {

  private static KaramelApi karamelApi;

  private TemplateHealthCheck healthCheck;

  private static final Options options = new Options();
  private static final CommandLineParser parser = new GnuParser();

  private static boolean cli = false;
  private static boolean headless = false;
  private static boolean noSudoPasswd = false;

  static {
    options.addOption("help", false, "Print help message.");
    options.addOption(OptionBuilder.withArgName("yamlFile")
      .hasArg()
      .withDescription("Dropwizard configuration in a YAML file")
      .create("server"));
    options.addOption(OptionBuilder.withArgName("yamlFile")
      .hasArg()
      .withDescription("Karamel cluster definition in a YAML file")
      .create("launch"));
    options.addOption("headless", false, "Launch Karamel from a headless server (no terminal on the server).");
    options.addOption(OptionBuilder.withArgName("sudoPassword")
      .hasArg()
      .withDescription("Sudo password")
      .create("passwd"));
  }

  /**
   * Usage instructions
   *
   * @param exitValue
   */
  public static void usage(int exitValue) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("karamel", options);
    System.exit(exitValue);
  }

  public static void main(String[] args) throws Exception {

    System.setProperty("java.net.preferIPv4Stack", "true");

    // These args are sent to the Dropwizard app (thread)
    String[] modifiedArgs = new String[2];
    modifiedArgs[0] = "server";
    String sudoPasswd = "";

    karamelApi = new KaramelApiImpl();

    try {
      CommandLine line = parser.parse(options, args);
      if (line.getOptions().length == 0) {
        usage(0);
      }
      if (line.hasOption("help")) {
        usage(0);
      }
      if (line.hasOption("server")) {
        modifiedArgs[1] = line.getOptionValue("server");
      }
      if (line.hasOption("launch")) {
        cli = true;
        headless = true;
      }
      if (line.hasOption("headless")) {
        headless = true;
      }
      if (line.hasOption("passwd")) {
        sudoPasswd = line.getOptionValue("passwd");        
      } else {
        noSudoPasswd = true;
      }

      if (cli) {

        //ClusterManager.EXIT_ON_COMPLETION  = true;
        new KaramelServiceApplication().run(modifiedArgs);

        if (!noSudoPasswd && !sudoPasswd.isEmpty()) {
          karamelApi.registerSudoPassword(sudoPasswd);
        }

        SSHKeyPair pair = karamelApi.getAvailableSSHKeys().get(0);
        karamelApi.registerSshKeys(pair);
        karamelApi.startCluster();
      }
    } catch (ParseException e) {
      usage(-1);
    } catch (KaramelException e) {
      System.err.println("Inalid yaml file; " + e.getMessage());
      System.exit(-2);
    }

    if (!cli) {
      new KaramelServiceApplication().run(modifiedArgs);
    }
  }

// Name of the application displayed when application boots up.
  @Override
  public String getName() {
    return "karamel-core";
  }

  @Override
  public void initialize(Bootstrap<KaramelServiceConfiguration> bootstrap) {
    bootstrap.addBundle(new MultiPartBundle());
  }

  @Override
  public void run(KaramelServiceConfiguration configuration, Environment environment) throws Exception {
    healthCheck = new TemplateHealthCheck("%s");

    /*
     * To allow cross origin resource request from angular js client
     */
    FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

    // Allow cross origin requests.
    filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    filter.setInitParameter("allowedOrigins", "*"); // allowed origins comma separated
    filter.setInitParameter("preflightMaxAge", "5184000"); // 2 months
    filter.setInitParameter("allowCredentials", "true");

    environment.jersey().setUrlPattern("/api/*");

    environment.healthChecks().register("template", healthCheck);

    //definitions

    environment.jersey().register(new UploadService(karamelApi));

    //ssh
    environment.jersey().register(new SSHKeys(karamelApi));
    environment.jersey().register(new SetSudoPassword(karamelApi));

    //cluster
    environment.jersey().register(new ClusterService(karamelApi));

    environment.jersey().register(new ExitKaramel(karamelApi));
    environment.jersey().register(new PingServer(karamelApi));
  }
}
