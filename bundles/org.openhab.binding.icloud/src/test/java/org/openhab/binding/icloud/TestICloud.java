package org.openhab.binding.icloud;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationParser;
import org.openhab.binding.icloud.internal.ICloudService;
import org.openhab.binding.icloud.internal.json.response.ICloudAccountDataResponse;
import org.openhab.core.storage.json.internal.JsonStorage;

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

    File jsonStorageFile = new File(System.getProperty("user.home"), "openhab.json");
    System.out.println(jsonStorageFile.toString());

    JsonStorage<String> stateStorage = new JsonStorage<String>(jsonStorageFile, TestICloud.class.getClassLoader(), 2,
        1000, 1000);

    ICloudService service = new ICloudService(this.E_MAIL, this.PW, stateStorage);
    if (service.requires2fa()) {
      System.out.print("Code: ");
      String code = new Scanner(System.in).nextLine();
      System.out.println(service.validate2faCode(code));
      if (!service.isTrustedSession()) {
        service.trustSession();
      }
      if (!service.isTrustedSession()) {
        System.err.println("Trust failed!!!");
      }

      ICloudAccountDataResponse deviceInfo = new ICloudDeviceInformationParser()
          .parse(service.getDevices().refreshClient());
    }
    stateStorage.flush();
  }

}
