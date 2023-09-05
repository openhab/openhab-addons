/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link CloudConnector} is responsible for connecting OH to the Xiaomi cloud communication.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = CloudConnector.class)
@NonNullByDefault
public class CloudConnector {

    private static final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(60);

    private static enum DeviceListState {
        FAILED,
        STARTING,
        REFRESHING,
        AVAILABLE,
    }

    private volatile DeviceListState deviceListState = DeviceListState.STARTING;

    private String username = "";
    private String password = "";
    private String country = "ru,us,tw,sg,cn,de,i2";
    private List<CloudDeviceDTO> deviceList = new ArrayList<>();
    private boolean connected;
    private final HttpClient httpClient;
    private @Nullable MiCloudConnector cloudConnector;
    private final Logger logger = LoggerFactory.getLogger(CloudConnector.class);

    private ExpiringCache<Boolean> logonCache = new ExpiringCache<Boolean>(CACHE_EXPIRY, () -> {
        return logon();
    });

    private ExpiringCache<String> refreshDeviceList = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
        if (deviceListState == DeviceListState.FAILED && !isConnected()) {
            return ("Could not connect to Xiaomi cloud");
        }
        final @Nullable MiCloudConnector cl = this.cloudConnector;
        if (cl == null) {
            return ("Could not connect to Xiaomi cloud");
        }
        deviceListState = DeviceListState.REFRESHING;
        deviceList.clear();
        for (String server : country.split(",")) {
            try {
                deviceList.addAll(cl.getDevices(server));
            } catch (JsonParseException e) {
                logger.debug("Parsing error getting devices: {}", e.getMessage());
            }
        }
        deviceListState = DeviceListState.AVAILABLE;
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
        return isConnected(false);
    }

    public boolean isConnected(boolean force) {
        final MiCloudConnector cl = cloudConnector;
        if (cl != null && cl.hasLoginToken()) {
            return true;
        }
        if (force) {
            logonCache.invalidateValue();
        }
        final @Nullable Boolean c = logonCache.getValue();
        if (c != null && c.booleanValue()) {
            return true;
        }
        deviceListState = DeviceListState.FAILED;
        return false;
    }

    public String sendRPCCommand(String device, String country, MiIoSendCommand command) throws MiCloudException {
        final @Nullable MiCloudConnector cl = this.cloudConnector;
        if (cl == null || !isConnected()) {
            throw new MiCloudException("Cannot execute request. Cloud service not available");
        }
        return cl.sendRPCCommand(device, country.trim().toLowerCase(), command.getCommandString());
    }

    public String sendCloudCommand(String urlPart, String country, String parameters) throws MiCloudException {
        final @Nullable MiCloudConnector cl = this.cloudConnector;
        if (cl == null || !isConnected()) {
            throw new MiCloudException("Cannot execute request. Cloud service not available");
        }
        return cl.request(urlPart.startsWith("/") ? urlPart : "/" + urlPart, country, parameters);
    }

    public @Nullable RawType getMap(String mapId, String country) throws MiCloudException {
        logger.debug("Getting vacuum map {} from Xiaomi cloud server: '{}'", mapId, country);
        String mapCountry;
        String mapUrl = "";
        final @Nullable MiCloudConnector cl = this.cloudConnector;
        if (cl == null || !isConnected()) {
            throw new MiCloudException("Cannot execute request. Cloud service not available");
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
        if (mapUrl.isBlank()) {
            logger.debug("Cannot download map data: Returned map URL is empty");
            return null;
        }
        try {
            RawType mapData = HttpUtil.downloadData(mapUrl, null, false, -1);
            if (mapData != null) {
                return mapData;
            } else {
                logger.debug("Could not download '{}'", mapUrl);
                return null;
            }
        } catch (IllegalArgumentException e) {
            logger.debug("Error downloading map: {}", e.getMessage());
        }
        return null;
    }

    public void setCredentials(@Nullable String username, @Nullable String password, @Nullable String country) {
        if (country != null) {
            this.country = country;
        }
        if (username != null && password != null) {
            this.username = username;
            this.password = password;
        }
    }

    private boolean logon() {
        if (username.isEmpty() || password.isEmpty()) {
            logger.debug("No Xiaomi cloud credentials. Cloud connectivity disabled");
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
                deviceListState = DeviceListState.FAILED;
            }
        } catch (MiCloudException e) {
            connected = false;
            deviceListState = DeviceListState.FAILED;
            logger.debug("Xiaomi cloud login failed: {}", e.getMessage());
        }
        return connected;
    }

    public List<CloudDeviceDTO> getDevicesList() {
        refreshDeviceList.getValue();
        return deviceList;
    }

    public @Nullable CloudDeviceDTO getDeviceInfo(String id) {
        getDevicesList();
        if (deviceListState != DeviceListState.AVAILABLE) {
            return null;
        }
        List<CloudDeviceDTO> devicedata = new ArrayList<>();
        for (CloudDeviceDTO deviceDetails : deviceList) {
            if (deviceDetails.getDid().contentEquals(id)) {
                devicedata.add(deviceDetails);
            }
        }
        if (devicedata.isEmpty()) {
            return null;
        }
        for (CloudDeviceDTO device : devicedata) {
            if (device.getIsOnline()) {
                return device;
            }
        }
        if (devicedata.size() > 1) {
            logger.debug("Found multiple servers for device {} {} returning first", devicedata.get(0).getDid(),
                    devicedata.get(0).getName());
        }
        return devicedata.get(0);
    }
}
