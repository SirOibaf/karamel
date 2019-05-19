package se.kth.karamel.common.util;

import org.jclouds.aws.domain.Region;
import org.jclouds.ec2.domain.InstanceType;

import java.util.Arrays;
import java.util.List;

public class Constants {
  public static final String KARAMEL_HOME = "KARAMEL_HOME";

  // Local directories filenames
  public static final String KARAMEL_CONF_DIRNAME = "conf";
  public static final String KARAMEL_CLUSTERS_DIRNAME = "clusters";
  public static final String KARAMEL_TMP_DIRNAME = "tmp";
  public static final String KARAMEL_COOKBOOK_DIRNAME = "cookbooks";
  public static final String KARAMEL_CONF_NAME = "karamel.conf";
  public static final String SOLO_CONF_NAME = "solo.rb";

  // Remote directories filenames
  public static final String REMOTE_WORKING_DIR_NAME = ".karamel";
  public static final String REMOTE_INSTALL_DIR_NAME = "install";
  public static final String REMOTE_COOKBOOKS_DIR_NAME = "cookbooks";

  public static final String CHEFDK_BUILDINFO_REDHAT_DEFAULT = ".el7.x86_64.rpm";
  public static final String CHEFDK_BUILDINFO_UBUNTU_DEFAULT = "_amd64.deb";

  public static final String RECIPE_RESULT_POSFIX = "__out.json";

  // TODO(Stupid Stuff)
  public static final String GITHUB_BASE_URL = "https://github.com";

  // Cookbooks name conventions
  public static final String ATTR_DELIMITER = "/";
  public static final String COOKBOOK_DELIMITER = "::";
  public static final String INSTALL_RECIPE = "install";
  public static final String DEFAULT_RECIPE = "default";
  public static final String PURGE_RECIPE = "purge";

  public static final String DATABAG_NAME_SEPARATOR = "__";
  public static final String DATABAG_NAME_POSTFIX = ".json";

  public static final String CHEF_BIN_NAME = "chef-solo";

  //-----------------------------------------------JCLOUDS--------------------------------------------------------------
  public static final int JCLOUDS_PROPERTY_MAX_RETRIES = 100;
  public static final int JCLOUDS_PROPERTY_RETRY_DELAY_START = 1000; //ms
  public static final int EC2_MAX_FORK_VMS_PER_REQUEST = 50;


  //--------------------------------------------Baremetal---------------------------------------------------------------
  public static final String IP_RANGE_DIVIDER = "-";

  public static final String PROVIDER_BAREMETAL_DEFAULT_USERNAME = "root";
  public static final int BAREMETAL_DEFAULT_SSH_PORT = 22;

  //--------------------------------------------AWS EC2-----------------------------------------------------------------
  public static final String AWS_VM_TYPE_DEFAULT = InstanceType.M3_MEDIUM;
  public static final String AWS_REGION_CODE_DEFAULT = Region.EU_WEST_1;
  public static final String AWS_VM_USERNAME_DEFAULT = "ubuntu";
  public static final String AWS_STORAGE_MAPPINGNAME_PREFIX = "/dev/sd";
  public static final String AWS_STORAGE_VIRTUALNAME_PREFIX = "ephemeral";
  public static final String AWS_STORAGE_KERNELALIAS_PREFIX = "/dev/xvd";
  public static final String AWS_STORAGE_MOUNTPOINT_PREFIX = "/mnt/disk";
  public static final List<String> AWS_VM_PORTS_DEFAULT = Arrays.asList(new String[]{"22"});
  public static final String AWS_GEOUPNAME_PATTERN = "[a-z0-9][[a-z0-9]|[-]]*";

  public static final String AWS_ACCESSKEY_KEY = "aws.access.key";
  public static final String AWS_ACCESSKEY_ENV_VAR = "AWS_ACCESS_KEY_ID";
  public static final String AWS_SECRETKEY_KEY = "aws.secret.key";
  public static final String AWS_SECRETKEY_ENV_VAR = "AWS_SECRET_ACCESS_KEY";
  public static final Integer AWS_BATCH_SIZE_DEFAULT = 1;
  public static final String AWS_BATCH_SIZE_KEY = "aws.batch.size";
  public static final int AWS_RETRY_INTERVAL = 6 * 1000;
  public static final int AWS_RETRY_MAX = 300;

  //--------------------------------------Google Compute Engine---------------------------------------------------------
  public static final String GCE_JSON_KEY_FILE_PATH = "gce.jsonkey.path";
  public static final String GCE_DEFAULT_IP_RANGE = "10.240.0.0/16";
  public static final String GCE_DEFAULT_NETWORK_NAME = "default";
  public static final String GCE_DEFAULT_IMAGE = "ubuntu-1404-trusty-v20150316";
  public static final String GCE_DEFAULT_ZONE = "europe-west1-b";
  public static final String GCE_DEFAULT_MACHINE_TYPE = "n1-standard-1";
  public static final Long GCE_DEFAULT_DISKSIZE_IN_GB = 15l;
  public static final Boolean GCE_DEFAULT_IS_PRE_EMPTIBLE = false;

  //--------------------------------------OCCI Engine---------------------------------------------------------
  public static final String OCCI_DEFAULT_USERNAME = "ubuntu";
  public static final String OCCI_DEFAULT_ENDPOINT = "https://carach5.ics.muni.cz:11443";
  public static final String OCCI_DEFAULT_IMAGE = "uuid_training_ubuntu_server_12_04_lts_fedcloud_warg_122";
  public static final String OCCI_DEFAULT_IMAGE_SIZE = "atlas";
  public static final String OCCI_USER_CERTIFICATE_PATH = "/tmp/x509up_u1000";
  public static final String OCCI_CERTIFICATE_DIR = "/etc/grid-security/certificates/";

  public static final String SUCCEED_TASKLIST_FILENAME = "succeed_list";

  //------------------------------------------Karamel Machine-----------------------------------------------------------
  public static final String SSH_PUBKEY_PATH_KEY = "ssh.publickey.path";
  public static final String SSH_PRIVKEY_PATH_KEY = "ssh.privatekey.path";
  public static final String YAML_FILE_NAME = "definition.yaml";
  public static final String SSH_FOLDER_NAME = ".ssh";
  public static final String STATS_FOLDER_NAME = "stats";
}
