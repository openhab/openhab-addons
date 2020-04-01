/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.miio.internal.cloud;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link CloudConnector} is responsible for connecting OH to the Xiaomi cloud communication.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = CloudConnector.class)
@NonNullByDefault
public class CloudConnector {

    protected static final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(60);
    private static final int FAILED = -1;
    private static final int STARTING = 0;
    private static final int REFRESHING = 1;
    private static final int AVAILABLE = 2;
    private int deviceListState = STARTING;

    private String username = "";
    private String password = "";
    private String country = "ru,us,tw,sg,cn,de";
    private List<JsonObject> deviceList = new ArrayList<JsonObject>();
    private boolean connected;
    private final HttpClient httpClient;
    private @Nullable MiCloudConnector cloudConnector;
    private final Logger logger = LoggerFactory.getLogger(CloudConnector.class);
    private final JsonParser parser = new JsonParser();

    private ExpiringCache<Boolean> logonCache = new ExpiringCache<Boolean>(CACHE_EXPIRY, () -> {
        return logon();
    });

    private ExpiringCache<String> refreshDeviceList = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
        if (deviceListState == FAILED && !isConnected()) {
            return ("Could not connect to Xiaomi cloud");
        }
        final @Nullable MiCloudConnector cl = this.cloudConnector;
        if (cl == null) {
            return ("Could not connect to Xiaomi cloud");
        }
        deviceListState = REFRESHING;
        deviceList.clear();
        for (String server : country.split(",")) {
            try {
                JsonElement response = parser.parse(cl.getDevices(server));
                if (response.isJsonObject() && response.getAsJsonObject().has("result")
                        && response.getAsJsonObject().get("result").isJsonObject()) {
                    JsonObject result = response.getAsJsonObject().get("result").getAsJsonObject();
                    result.addProperty("server", server);
                    deviceList.add(result);
                }
            } catch (JsonParseException e) {
                logger.debug("Parsing error getting devices: {}", e.getMessage());
            }
        }
        deviceListState = AVAILABLE;
        return "done";// deviceList;
    });

    @Activate
    public CloudConnector(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.createHttpClient(BINDING_ID);
    }

    @Deactivate
    public void dispose() {
        final MiCloudConnector cl = cloudConnector;
        if (cl != null) {
            cl.stopClient();
        }
        cloudConnector = null;
    }

    public boolean isConnected() {
        final MiCloudConnector cl = cloudConnector;
        if (cl != null && cl.hasLoginToken()) {
            return true;
        }
        final @Nullable Boolean c = logonCache.getValue();
        if (c != null && c.booleanValue()) {
            return true;
        }
        deviceListState = FAILED;
        return false;
    }

    public @Nullable RawType getMap(String mapId, String country) throws MiCloudException {
        logger.info("Getting vacuum map {} from Xiaomi cloud server: {}", mapId, country);
        String mapCountry;
        String mapUrl = "";
        final @Nullable MiCloudConnector cl = this.cloudConnector;
        if (cl == null || !isConnected()) {
            throw new MiCloudException("Cannot execute request. Cloudservice not available");
        }
        if (country.isEmpty()) {
            logger.debug("Server not defined in thing. Trying servers: {}", this.country);
            for (String mapCountryServer : this.country.split(",")) {
                mapCountry = mapCountryServer.trim().toLowerCase();
                mapUrl = cl.getMapUrl(mapId, mapCountry);
                logger.debug("Map download from server {} returned {}", mapCountry, mapUrl);
                if (!mapUrl.isEmpty()) {
                    break;
                }
            }
        } else {
            mapCountry = country.trim().toLowerCase();
            mapUrl = cl.getMapUrl(mapId, mapCountry);
        }
        @Nullable
        RawType mapData = HttpUtil.downloadData(mapUrl, null, false, -1);
        if (mapData != null) {
            return mapData;
        } else {
            logger.debug("Could not download '{}'", mapUrl);
            return null;
        }
    }

    public void setCredentials(@Nullable String username, @Nullable String password, @Nullable String country) {
        if (country != null) {
            this.country = country;
        }
        if (username != null && password != null) {
            this.username = username;
            this.password = password;
            logon();
        }
    }

    private boolean logon() {
        if (username.isEmpty() || password.isEmpty()) {
            logger.info("No Xiaomi cloud credentials. Cloud connectivity diabled");
            logger.debug("Logon details: username: '{}', pass: '{}', country: '{}'", username,
                    password.replaceAll(".", "*"), country);
            return connected;
        }
        try {
            final MiCloudConnector cl = new MiCloudConnector(username, password, httpClient);
            this.cloudConnector = cl;
            connected = cl.login();
            if (connected) {
                getDevicesList();
            } else {
                deviceListState = FAILED;
            }
        } catch (MiCloudException e) {
            connected = false;
            deviceListState = FAILED;
            logger.debug("Xiaomi cloud login failed: {}", e.getMessage());
        }
        return connected;
    }

    public List<JsonObject> getDevicesList() {
        refreshDeviceList.getValue();
        return deviceList;
    }

    public JsonObject getDeviceInfo(String id) {
        getDevicesList();
        if (deviceListState < AVAILABLE) {
            JsonObject returnvalue = new JsonObject();
            returnvalue.addProperty("deviceListState", deviceListState);
            return returnvalue;
        }
        String did = Long.toString(Long.parseUnsignedLong(id, 16));
        List<JsonObject> devicedata = new ArrayList<JsonObject>();
        for (JsonObject countyDeviceList : deviceList) {
            if (countyDeviceList.has("list") && countyDeviceList.get("list").isJsonArray()) {
                for (JsonElement device : countyDeviceList.get("list").getAsJsonArray()) {
                    if (device.isJsonObject() && device.getAsJsonObject().has("did")
                            && device.getAsJsonObject().get("did").getAsString().contentEquals(did)
                            && device.getAsJsonObject().has("token")) {
                        JsonObject deviceDetails = device.getAsJsonObject();
                        deviceDetails.addProperty("server", countyDeviceList.get("server").getAsString());
                        devicedata.add(deviceDetails);
                    }
                }
            }
        }
        JsonObject returnvalue = new JsonObject();
        switch (devicedata.size()) {
            case 0:
                returnvalue.addProperty("connected", connected);
                break;
            case 1:
                returnvalue = devicedata.get(0);
                break;
            default:
                for (JsonObject device : devicedata) {
                    if (device.has("isOnline") && device.get("isOnline").getAsBoolean()) {
                        return device;
                    }
                }
                logger.debug("Found multiple servers for device, with device offline {} {} ",
                        devicedata.get(0).get("name").getAsString(), id);
                for (JsonObject device : devicedata) {
                    logger.debug("Server {} token: {}", device.get("server").getAsString(),
                            device.get("token").getAsString());
                }
                returnvalue = devicedata.get(0);
        }
        return returnvalue;
    }
}
