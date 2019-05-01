package se.kth.karamel.common.util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Constants {
  public static final String KARAMEL_HOME = "KARAMEL_HOME";

  // Local directories filenames
  public static final String KARAMEL_CONF_DIRNAME = "conf";
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

  //read
  public static final String ATTR_DELIMITER = "/";
  public static final String COOKBOOK_DELIMITER = "::";
  public static final String INSTALL_RECIPE = "install";
  public static final String DEFAULT_RECIPE = "default";
  public static final String PURGE_RECIPE = "purge";
  public static final int DAY_IN_MS = 24 * 3600 * 1000;
  public static final int DAY_IN_MIN = 24 * 60;
  public static final int SEC_IN_MS = 1000;
  public static final int MIN_IN_MS = 60 * SEC_IN_MS;
  public static final String IP_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
      + "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
      + "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
      + "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";



  //------------------------------Cluster Runtime Dynamics--------------------------------------------------------------
  public static final int INSTALLATION_DAG_THREADPOOL_SIZE = 100;
  public static final int CLUSTER_STATUS_CHECKING_INTERVAL = 1000;
  public static final int CLUSTER_FAILURE_DETECTION_INTERVAL = 5000;
  public static final int CLUSTER_STAT_REPORT_INTERVAL = Settings.MIN_IN_MS;
  public static final int MACHINE_TASKRUNNER_BUSYWAITING_INTERVALS = 100;
  public static final int MACHINES_TASKQUEUE_SIZE = 100;
  public static final int SSH_CONNECTION_TIMEOUT = DAY_IN_MS;
  public static final int SSH_SESSION_TIMEOUT = DAY_IN_MS;
  public static final int SSH_PING_INTERVAL = 10 * SEC_IN_MS;
  public static final int SSH_SESSION_RETRY_NUM = 10;
  public static final int SSH_CMD_RETRY_NUM = 2;
  public static final int SSH_CMD_RETRY_INTERVALS = 3 * SEC_IN_MS;
  public static final float SSH_CMD_RETRY_SCALE = 1.5f;
  public static final int SSH_CMD_MAX_TIOMEOUT = DAY_IN_MIN;


  //-----------------------------------------------JCLOUDS--------------------------------------------------------------
  public static final int JCLOUDS_PROPERTY_MAX_RETRIES = 100;
  public static final int JCLOUDS_PROPERTY_RETRY_DELAY_START = 1000; //ms
  public static final int EC2_MAX_FORK_VMS_PER_REQUEST = 50;


  //--------------------------------------------Baremetal---------------------------------------------------------------
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

  //--------------------------------------OCCI Engine---------------------------------------------------------
  public static final String OCCI_DEFAULT_USERNAME = "ubuntu";
  public static final String OCCI_DEFAULT_ENDPOINT = "https://carach5.ics.muni.cz:11443";
  public static final String OCCI_DEFAULT_IMAGE = "uuid_training_ubuntu_server_12_04_lts_fedcloud_warg_122";
  public static final String OCCI_DEFAULT_IMAGE_SIZE = "atlas";
  public static final String OCCI_USER_CERTIFICATE_PATH = "/tmp/x509up_u1000";
  public static final String OCCI_CERTIFICATE_DIR = "/etc/grid-security/certificates/";

  public static final String SUCCEED_TASKLIST_FILENAME = "succeed_list";

  //--------------------------------Target Macines----------------------------------------------------------------------
  public static final String REMOTE_COOKBOOKS_DIR_NAME = "cookbooks";
  public static final String REMOTE_HOME_ROOT = "/home";
  public static final String REMOTE_CB_FS_PATH_DELIMITER = "__";
  public final static String REMOTE_CHEFJSON_PRIVATEIPS_TAG = "private_ips";
  public final static String REMOTE_CHEFJSON_PUBLICIPS_TAG = "public_ips";
  public final static String REMOTE_CHEFJSON_HOSTS_TAG = "hosts";
  public static final String REMOTE_CHEFJSON_RUNLIST_TAG = "run_list";


  //------------------------------------------Karamel Machine-----------------------------------------------------------
  public static final String SSH_PUBKEY_PATH_KEY = "ssh.publickey.path";
  public static final String SSH_PRIVKEY_PATH_KEY = "ssh.privatekey.path";
  public static final String YAML_FILE_NAME = "definition.yaml";
  public static final String SSH_FOLDER_NAME = ".ssh";
  public static final String STATS_FOLDER_NAME = "stats";
  public static final String RECIPE_RESULT_POSFIX = "__out.json";

  public static String loadIpAddress() {
    String address = "UnknownHost";
    try {
      address = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException ex) {
    }
    return address;
  }

  public static String CLUSTER_LOG_FOLDER(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + "logs";
  }

  public static String MACHINE_LOG_FOLDER(String clusterName, String machineIp) {
    return CLUSTER_LOG_FOLDER(clusterName) + File.separator + machineIp;
  }

  public static String TASK_LOG_FILE_PATH(String clusterName, String machinIp, String taskName) {
    return MACHINE_LOG_FOLDER(clusterName, machinIp) + File.separator
        + taskName.toLowerCase().replaceAll("\\W", "_") + ".log";
  }

  public static String CLUSTER_ROOT_PATH(String clusterName) {
    return KARAMEL_ROOT_PATH + File.separator + clusterName.toLowerCase();
  }

  public static String CLUSTER_SSH_PATH(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + SSH_FOLDER_NAME;
  }

  public static String CLUSTER_YAML_PATH(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + YAML_FILE_NAME;
  }

  public static String CLUSTER_STATS_FOLDER(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + STATS_FOLDER_NAME;
  }

  public static String RECIPE_CANONICAL_NAME(String recipeName) {
    if (!recipeName.contains(COOKBOOK_DELIMITER)) {
      return recipeName + COOKBOOK_DELIMITER + "default";
    } else {
      return recipeName;
    }
  }

  public static String CLUSTER_TEMP_FOLDER(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + "tmp";
  }

  public static String MACHINE_TEMP_FOLDER(String clusterName, String machineIp) {
    return CLUSTER_TEMP_FOLDER(clusterName) + File.separator + machineIp;
  }

  public static String MACHINE_SUCCEEDTASKS_PATH(String clusterName, String machineIp) {
    return MACHINE_TEMP_FOLDER(clusterName, machineIp) + File.separator + SUCCEED_TASKLIST_FILENAME;
  }

  public static String MACHINE_OSTYPE_PATH(String clusterName, String machineIp) {
    return MACHINE_TEMP_FOLDER(clusterName, machineIp) + File.separator + OSTYPE_FILE_NAME;
  }

  public static String RECIPE_RESULT_LOCAL_PATH(String recipeName, String clusterName, String machineIp) {
    String recName;
    if (!recipeName.contains(COOKBOOK_DELIMITER)) {
      recName = recipeName + COOKBOOK_DELIMITER + "default";
    } else {
      recName = recipeName;
    }
    return MACHINE_TEMP_FOLDER(clusterName, machineIp) + File.separator
        + recName.replace(COOKBOOK_DELIMITER, REMOTE_CB_FS_PATH_DELIMITER) + RECIPE_RESULT_POSFIX;
  }
}