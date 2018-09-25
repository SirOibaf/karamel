/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class DefaultRbTest {

  @Test
  public void testLoadAttributes() throws CookbookUrlException, MetadataParseException, ValidationException, NoKaramelizedCookbookException {
    Settings.CB_CLASSPATH_MODE = true;
    KaramelizedCookbook cb = new KaramelizedCookbook("testorg/testrepo/tree/master/cookbooks/biobankcloud/hiway-chef", false);
    DefaultRb defaultRb = cb.getDefaultRb();
    
    Object value = defaultRb.getValue("hiway/variantcall/reads/run_ids");
    Assert.assertEquals(Lists.newArrayList("SRR359188", "SRR359195"), value);

    value = defaultRb.getValue("hiway/variantcall/reference/chromosomes");
    Assert.assertEquals(Lists.newArrayList("chr22", "chrY"), value);
  }
}
