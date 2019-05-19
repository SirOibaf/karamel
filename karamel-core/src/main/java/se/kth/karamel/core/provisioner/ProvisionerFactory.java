package se.kth.karamel.core.provisioner;

import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;

public class ProvisionerFactory {

  private static Provisioner provisioner = null;

  public static Provisioner getProvisioner(Settings settings) throws KaramelException {
    if (provisioner == null) {
      String provisionerClassname = settings.get(Settings.SettingsKeys.PROVISIONER_CLASS);
      try {
        provisioner = (Provisioner) Class.forName(provisionerClassname).newInstance();
      } catch (Exception e) {
        throw new KaramelException("Provisioner class not found: " + provisionerClassname);
      }
    }

    return provisioner;
  }

}
