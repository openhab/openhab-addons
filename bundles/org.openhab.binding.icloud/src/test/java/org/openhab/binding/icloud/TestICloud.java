package org.openhab.binding.icloud;

import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.openhab.binding.icloud.internal.ICloudConnectionV2;

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

    ICloudConnectionV2 connection = new ICloudConnectionV2();
    connection.authenticate(this.E_MAIL, this.PW);

    System.out.print("Enter code: ");
    String code = new Scanner(System.in).nextLine();
    connection.sendSecurityCode(code);
    connection.listServices();
    connection.sendSound(this.DEVICE_ID);
  }

}
