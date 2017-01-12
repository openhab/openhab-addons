package org.openhab.binding.homepilot.internal;

import static org.openhab.binding.homepilot.HomePilotBindingConstants.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.Fields;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DefaultHttpGateway implements HomePilotGateway {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHttpGateway.class);

    private String id;
    private HomePilotConfig config;
    private HttpClient httpClient;
    // private Cache<String, String> responseCache;

    public DefaultHttpGateway(String id, HomePilotConfig config) {
        this.id = id;
        this.config = config;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void initialize() {
        httpClient = new HttpClient();
        // httpClient.setConnectTimeout(config.getTimeout() * 1000L);

        try {
            httpClient.start();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        // responseCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS)
        // .build(new CacheLoader<String, String>() {
        //
        // @Override
        // public String load(String url) throws Exception {
        // logger.info("Calling url " + url);
        // return httpClient.POST(url).header(HttpHeader.CONTENT_TYPE, "application/json;charset=utf-8")
        // .send().getContentAsString();
        // }
        // });
    }

    @Override
    public void cancelLoadAllDevices() {
        // nothing to do here
    }

    @Override
    public List<HomePilotDevice> loadAllDevices() {
        String url = String.format("http://%s/deviceajax.do?alldevices", config.getAddress());
        try {
            return transform2Devices(
                    httpClient.POST(url).header(HttpHeader.CONTENT_TYPE, "application/json;charset=utf-8").send()
                            .getContentAsString()/* responseCache.get(url) */);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<HomePilotDevice> transform2Devices(String json) {
        List<HomePilotDevice> devices = Lists.newArrayList();

        try {
            JsonObject responseJSON = new JsonParser().parse(json).getAsJsonObject();
            JsonArray devicesJSON = responseJSON.get("devices").getAsJsonArray();
            for (int i = 0; i < devicesJSON.size(); i++) {
                JsonObject deviceJSON = devicesJSON.get(i).getAsJsonObject();
                int deviceGroup = deviceJSON.get("deviceGroup").getAsInt();
                ThingTypeUID thingTypeUID;
                if (deviceGroup == 1) {
                    thingTypeUID = THING_TYPE_SWITCH;
                } else if (deviceGroup == 2) {
                    thingTypeUID = THING_TYPE_ROLLERSHUTTER;
                } else {
                    throw new RuntimeException(String.format("unknown serial %s for device %s", deviceGroup,
                            deviceJSON.get("did").getAsString()));
                }
                HomePilotDevice device = new HomePilotDeviceImpl(thingTypeUID, deviceJSON.get("did").getAsInt(),
                        deviceJSON.get("name").getAsString(), deviceJSON.get("description").getAsString(),
                        deviceJSON.get("position").getAsInt());
                devices.add(device);
            }
        } catch (JsonIOException e) {
            throw new RuntimeException(e);
        }
        return devices;
    }

    @Override
    public boolean handleSetPosition(String deviceId, int position) {
        Fields fields = new Fields();
        fields.add("cid", "9");
        fields.add("goto", String.valueOf(position));
        return sendFields(deviceId, fields);
    }

    @Override
    public boolean handleSetOnOff(String deviceId, boolean on) {
        Fields fields = new Fields();
        fields.add("cid", on ? "10" : "11");
        return sendFields(deviceId, fields);
    }

    @Override
    public boolean handleStop(String deviceId) {
        Fields fields = new Fields();
        fields.add("cid", "2");
        return sendFields(deviceId, fields);
    }

    private boolean sendFields(String deviceId, Fields fields) {
        String url = String.format("http://%s/deviceajax.do?", config.getAddress());
        try {
            fields.add("did", deviceId);
            fields.add("command", "1");

            String response = httpClient.POST(url)
                    .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8")
                    .header(HttpHeader.ACCEPT, "application/json;charset=utf-8")
                    .content(new FormContentProvider(fields)).send().getContentAsString();

            JsonObject responseJSON = new JsonParser().parse(response).getAsJsonObject();
            String status = responseJSON.get("status").getAsString();

            return "uisuccess".equals(status);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HomePilotDevice loadDevice(String deviceId) {
        for (HomePilotDevice device : loadAllDevices()) {
            if (deviceId.equals(device.getDeviceId())) {
                return device;
            }
        }
        throw new IllegalStateException("no device for id " + deviceId + " found");
    }
}
