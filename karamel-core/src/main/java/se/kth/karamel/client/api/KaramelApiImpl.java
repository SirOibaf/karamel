package se.kth.karamel.client.api;

import org.apache.log4j.Logger;
import org.jclouds.ContextBuilder;
import org.jclouds.domain.Credentials;
import org.jclouds.openstack.nova.v2_0.NovaApiMetadata;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.core.ClusterService;
import se.kth.karamel.core.launcher.amazon.Ec2Context;
import se.kth.karamel.core.launcher.amazon.Ec2Launcher;
import se.kth.karamel.core.launcher.google.GceContext;
import se.kth.karamel.core.launcher.google.GceLauncher;
import se.kth.karamel.core.launcher.nova.NovaContext;
import se.kth.karamel.core.launcher.nova.NovaLauncher;
import se.kth.karamel.core.launcher.novav3.NovaV3Context;
import se.kth.karamel.core.launcher.novav3.NovaV3Launcher;
import se.kth.karamel.core.launcher.occi.OcciContext;
import se.kth.karamel.core.launcher.occi.OcciLauncher;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;
import se.kth.karamel.common.exception.InvalidOcciCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.NovaCredentials;
import se.kth.karamel.common.util.OcciCredentials;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.util.SshKeyService;
import se.kth.karamel.common.util.settings.NovaSetting;


/**
 * Implementation of the Karamel Api for UI
 */
public class KaramelApiImpl implements KaramelApi {

  private static final Logger logger = Logger.getLogger(KaramelApiImpl.class);

  private ClusterService clusterService = ClusterService.getInstance();

  @Override
  public void loadClusterDefinition(String clusterDefinition) throws KaramelException {
    Yaml yaml = new Yaml(new Constructor(Cluster.class));
    Cluster cluster = (Cluster) yaml.load(clusterDefinition);
    clusterService.setCurrentCluster(cluster);
  }

  @Override
  public Cluster getCluster() {
    return clusterService.getCurrentCluster();
  }

  @Override
  public Ec2Credentials loadEc2CredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    return Ec2Launcher.readCredentials(confs);
  }

  @Override
  public boolean updateEc2CredentialsIfValid(Ec2Credentials credentials) throws KaramelException {
    Ec2Context context = Ec2Launcher.validateCredentials(credentials);
    Confs confs = Confs.loadKaramelConfs();
    confs.put(Settings.AWS_ACCESSKEY_KEY, credentials.getAccessKey());
    confs.put(Settings.AWS_SECRETKEY_KEY, credentials.getSecretKey());
    confs.writeKaramelConfs();
    clusterService.registerEc2Context(context);
    return true;
  }

  @Override
  public String loadGceCredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    String path = confs.getProperty(Settings.GCE_JSON_KEY_FILE_PATH);
    if (path != null) {
      Credentials credentials = GceLauncher.readCredentials(path);
      if (credentials != null) {
        return path;
      }
    }

    return null;
  }

  @Override
  public boolean updateGceCredentialsIfValid(String jsonFilePath) throws KaramelException {
    if (jsonFilePath.isEmpty() || jsonFilePath == null) {
      return false;
    }
    try {
      Credentials credentials = GceLauncher.readCredentials(jsonFilePath);
      GceContext context = GceLauncher.validateCredentials(credentials);
      Confs confs = Confs.loadKaramelConfs();
      confs.put(Settings.GCE_JSON_KEY_FILE_PATH, jsonFilePath);
      confs.writeKaramelConfs();
      clusterService.registerGceContext(context);
    } catch (Throwable ex) {
      throw new KaramelException(ex.getMessage());
    }
    return true;
  }

  @Override
  public NovaCredentials loadNovaCredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    return NovaLauncher.readCredentials(confs);
  }

  @Override
  public boolean updateNovaCredentialsIfValid(NovaCredentials credentials) throws InvalidNovaCredentialsException {
    if (credentials.getVersion().equals("v2")) {
      NovaContext context = NovaLauncher.validateCredentials(credentials,
        ContextBuilder.newBuilder(new NovaApiMetadata()));
      clusterService.registerNovaContext(context);
    } else if (credentials.getVersion().equals("v3")) {
      NovaV3Context context = NovaV3Launcher.validateCredentials(credentials);
      clusterService.registerNovaV3Context(context);
    } else {
      // Hej
    }

    Confs confs = Confs.loadKaramelConfs();
    confs.put(NovaSetting.NOVA_ACCOUNT_ID_KEY.getParameter(), credentials.getAccountName());
    confs.put(NovaSetting.NOVA_ACCESSKEY_KEY.getParameter(), credentials.getAccountPass());
    confs.put(NovaSetting.NOVA_ACCOUNT_ENDPOINT.getParameter(), credentials.getEndpoint());
    confs.put(NovaSetting.NOVA_REGION.getParameter(), credentials.getRegion());
    confs.put(NovaSetting.NOVA_REGION.getParameter(), credentials.getRegion());
    confs.put(NovaSetting.NOVA_VERSION.getParameter(), credentials.getVersion());
    confs.put(NovaSetting.NOVA_NETWORKID.getParameter(), credentials.getNetworkId());
    confs.writeKaramelConfs();
    return true;
  }

  public OcciCredentials loadOcciCredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    return OcciLauncher.readCredentials(confs);
  }

  @Override
  public boolean updateOcciCredentialsIfValid(OcciCredentials credentials) throws InvalidOcciCredentialsException {
    OcciContext context = OcciLauncher.validateCredentials(credentials);
    Confs confs = Confs.loadKaramelConfs();
    confs.put("occi.user.certificate.path", credentials.getUserCertificatePath());
    confs.put("occi.certificate.dir", credentials.getSystemCertDir());
    confs.writeKaramelConfs();
    clusterService.registerOcciContext(context);
    return true;
  }

  @Override
  public void pauseCluster(String clusterName) throws KaramelException {
    clusterService.pauseDag();
  }

  @Override
  public void resumeCluster(String clusterName) throws KaramelException {
    clusterService.resumeDag();
  }

  @Override
  public void terminateCluster(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void startCluster() throws KaramelException {
    clusterService.startCluster();
  }

  @Override
  public String getInstallationDag(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SshKeyPair loadSshKeysIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    return SshKeyService.loadSshKeys(confs);
  }

  @Override
  public SshKeyPair loadSshKeysIfExist(String clusterName) throws KaramelException {
    Confs confs = Confs.loadAllConfsForCluster(clusterName);
    return SshKeyService.loadSshKeys(confs);
  }

  @Override
  public SshKeyPair registerSshKeys(SshKeyPair keypair) throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    saveSshConfs(keypair, confs);
    confs.writeKaramelConfs();
    keypair = SshKeyService.loadSshKeys(keypair.getPublicKeyPath(), keypair.getPrivateKeyPath(),
        keypair.getPassphrase());
    clusterService.registerSshKeyPair(keypair);
    return keypair;
  }

  private void saveSshConfs(SshKeyPair keypair, Confs confs) {
    confs.put(Settings.SSH_PRIVKEY_PATH_KEY, keypair.getPrivateKeyPath());
    confs.put(Settings.SSH_PUBKEY_PATH_KEY, keypair.getPublicKeyPath());
  }

  @Override
  public SshKeyPair registerSshKeys(String clusterName, SshKeyPair keypair) throws KaramelException {
    Confs confs = Confs.loadJustClusterConfs(clusterName);
    saveSshConfs(keypair, confs);
    confs.writeClusterConfs(clusterName);
    keypair = SshKeyService.loadSshKeys(keypair.getPublicKeyPath(), keypair.getPrivateKeyPath(),
        keypair.getPassphrase());
    clusterService.registerSshKeyPair(keypair);
    return keypair;
  }

  @Override
  public void registerSudoPassword(String password) {
    ClusterService.getInstance().registerSudoAccountPassword(password);
  }
}
