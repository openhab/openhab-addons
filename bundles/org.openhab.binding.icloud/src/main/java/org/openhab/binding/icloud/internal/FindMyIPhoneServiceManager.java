package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * TODO simon This type ...
 *
 */
public class FindMyIPhoneServiceManager {

  private ICloudSession session;

  private URI fmipRefreshUrl;

  private URI fmipSoundUrl;

  private final static String FMIP_ENDPOINT = "/fmipservice/client/web";

  private Map<String, Object> data;

  private final Gson gson = new GsonBuilder().create();

  private boolean withFamily;

  private final static Logger LOGGER = LoggerFactory.getLogger(FindMyIPhoneServiceManager.class);

  public FindMyIPhoneServiceManager(ICloudSession session, String serviceRoot, Object params, boolean withFamily)
      throws IOException, InterruptedException {

    this.session = session;
    this.withFamily = withFamily;
    this.fmipRefreshUrl = URI.create(serviceRoot + FMIP_ENDPOINT + "/refreshClient");
    this.fmipSoundUrl = URI.create(serviceRoot + FMIP_ENDPOINT + "/playSound");

    // FIXME not in constructor?!
    refreshClient();
  }

  /**
   * @throws InterruptedException
   * @throws IOException
   *
   */
  private void refreshClient() throws IOException, InterruptedException {

    Map localdata = Map.of("clientContext",
        Map.of("fmly", this.withFamily, "shouldLocate", true, "selectedDevice", "all", "deviceListVersion", 1));

    String result = this.session.post(this.fmipRefreshUrl.toString(), this.gson.toJson(localdata), null);
    this.data = this.gson.fromJson(result, Map.class);
    LOGGER.debug("Device data {}", this.data);
  }
}
