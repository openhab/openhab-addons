package org.openhab.binding.icloud;

import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.openhab.binding.icloud.internal.ICloudService;

/**
 * TODO simon This type ...
 *
 */

public class TestICloud {

  private final String E_MAIL = System.getProperty("icloud.test.email");

  private final String PW = System.getProperty("icloud.test.pw");

  private final String DEVICE_ID = System.getProperty("icloud.test.device");

  @Test
  public void testAuth() throws IOException, InterruptedException {

    ICloudService service = new ICloudService(this.E_MAIL, this.PW);
    if (service.requires2fa()) {
      System.out.print("Code: ");
      String code = new Scanner(System.in).nextLine();
      System.out.println(service.validate2faCode(code));
      if (!service.isTrustedSession()) {
        service.trustSession();
      }
    }
  }

}
