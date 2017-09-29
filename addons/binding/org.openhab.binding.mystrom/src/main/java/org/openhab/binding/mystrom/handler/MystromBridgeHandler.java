/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystrom.handler;

import static org.openhab.binding.mystrom.MystromBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.mystrom.config.MystromBridgeConfiguration;
import org.openhab.binding.mystrom.discovery.MystromDiscoveryService;
import org.openhab.binding.mystrom.internal.AuthResponse;
import org.openhab.binding.mystrom.internal.Device;
import org.openhab.binding.mystrom.internal.MystromDeviceAddedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This bridge handler connects to mystrom and handles all the api requests. It pulls down the
 * updated data, polls the system and does all the co-ordination with the other handlers
 * to get the data updated to the correct things.
 *
 * @author Stéphane Raemy - initial contribution
 */
public class MystromBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(MystromBridgeHandler.class);

    // REST Client API variables
    protected final Client mystromClient = ClientBuilder.newClient();
    public final WebTarget mystromTarget = mystromClient.target(MYSTROM_URI);
    public final WebTarget authTarget = mystromTarget.path(MYSTROM_AUTH_PATH);
    public final WebTarget devicesTarget = mystromTarget.path(MYSTROM_DEVICES_PATH);
    public final WebTarget deviceTarget = mystromTarget.path(MYSTROM_DEVICE_PATH);
    public final WebTarget deviceSwitchTarget = mystromTarget.path(MYSTROM_DEVICE_SWITCH_PATH);

    private List<MystromDeviceAddedListener> listeners = new ArrayList<MystromDeviceAddedListener>();

    // Will refresh the data each time it runs.
    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            refreshData();
        }
    };

    private ScheduledFuture<?> pollingJob;

    protected Gson gson = new Gson();

    /**
     * Creates the bridge handler to connect to mystrom.
     *
     * @param bridge The bridge to connect to mystrom with.
     * @param bindingProperties
     */
    public MystromBridgeHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    /**
     * Initialize the connection to mystrom.
     */
    @Override
    public void initialize() {
        logger.debug("Initialize the Mystrom bridge handler");

        MystromBridgeConfiguration config = getConfigAs(MystromBridgeConfiguration.class);
        logger.debug("Email       {}.", config.email);
        logger.debug("Password   {}.", config.password);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Starting poll query");

        startAutomaticRefresh(config.refreshInterval);
    }

    /**
     * Do something useful when the configuration update happens. Triggers changing
     * polling intervals as well as re-doing the access token.
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Config update");
        super.handleConfigurationUpdate(configurationParameters);

        stopAutomaticRefresh();
        startAutomaticRefresh(getConfigAs(MystromBridgeConfiguration.class).refreshInterval);
    }

    /**
     * Clean up the handler.
     */
    @Override
    public void dispose() {
        logger.debug("Mystrom bridge disposed");
        stopAutomaticRefresh();
        this.pollingJob = null;
        this.pollingRunnable = null;
    }

    /**
     * Handles an incoming command update
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refresh command received");
            refreshData();
        }
    }

    /**
     * Read the data from mystrom and then parse it into something useful.
     */
    private void refreshData() {
        logger.debug("Starting refreshData");

        try {
            List<Device> devices = queryDevices();
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Received update from mystrom");
            compareThings(devices);
        } catch (Exception e) {
            logger.error("Error parsing data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error parsing data " + e.getMessage());
        }
    }

    private void compareThings(List<Device> devices) {
        Bridge bridge = getThing();
        List<Thing> things = bridge.getThings();

        for (Device device : devices) {
            Thing thing = getDevice(device.id, things);
            if (device.type.equals(DEVICE_TYPE_WIFISWITCH)) {
                if (thing != null) {
                    MystromWifiSwitchHandler handler = (MystromWifiSwitchHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateDevice(device);
                    }
                } else {
                    for (MystromDeviceAddedListener listener : listeners) {
                        logger.debug("Found new Wifi Switch device {}", device.id);
                        listener.onWifiSwitchAdded(device);
                    }
                }
            }
        }
    }

    protected String getAuthToken() {
        MystromBridgeConfiguration config = getConfigAs(MystromBridgeConfiguration.class);
        String email = config.email;
        String password = config.password;

        Response response = authTarget.queryParam(EMAIL, email).queryParam(PASSWORD, password)
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        if (response != null) {
            logger.debug("Authenticating Response : {}:{}", response.getStatus(), response.getStatusInfo());

            if (response.getStatus() == 200 && response.hasEntity()) {

                // AuthResponse authResponse = response.readEntity(AuthResponse.class);
                String responsePayLoad = response.readEntity(String.class);
                AuthResponse authResponse = gson.fromJson(responsePayLoad.trim(), AuthResponse.class);

                if (StringUtils.isNotEmpty(authResponse.authToken)) {
                    return authResponse.authToken;
                }

            }
        }
        return null;
    }

    protected List<Device> queryDevices() {

        // get a list of devices
        Response response = devicesTarget.queryParam(AUTH_TOKEN, getAuthToken())
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        logger.debug("Querying the devices : Response : {}:{}", response.getStatus(), response.getStatusInfo());

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
        logger.debug("Query Devices Response: {}", jsonObject);
        Device[] deviceArray = gson.fromJson(jsonObject.getAsJsonArray("devices"), Device[].class);
        List<Device> devices = Arrays.asList(deviceArray);
        return devices;
    }

    protected Device queryDevice() {

        // get a device
        Response response = deviceTarget.queryParam(AUTH_TOKEN, getAuthToken()).queryParam("id", getConfig().get(ID))
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        logger.debug("Querying the device : Response : {}:{}", response.getStatus(), response.getStatusInfo());

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response.readEntity(String.class)).getAsJsonObject();
        Device device = gson.fromJson(jsonObject.getAsJsonObject("device"), Device.class);
        return device;
    }

    protected void switchDevice(String id, boolean state) {
        logger.debug("Switch Device Request for id:{}, state:{}", id, state);
        // switch a device
        Response response = deviceSwitchTarget.queryParam(AUTH_TOKEN, getAuthToken()).queryParam("id", id)
                .queryParam("on", state).request(MediaType.APPLICATION_JSON_TYPE).get();

        logger.debug("Switch Device Response : {}:{}", response.getStatus(), response.getStatusInfo());
        refreshData();
    }

    private Thing getDevice(String deviceId, List<Thing> things) {
        for (Thing thing : things) {
            String thingDeviceId = thing.getUID().getId();
            if (thingDeviceId.equals(deviceId)) {
                return thing;
            }
        }
        return null;
    }

    private synchronized void startAutomaticRefresh(int refreshInterval) {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private synchronized void stopAutomaticRefresh() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * @param mystromDiscoveryService The device added listener to add
     */
    public void addDeviceAddedListener(MystromDeviceAddedListener mystromDiscoveryService) {
        this.listeners.add(mystromDiscoveryService);
    }

    /**
     * @param mystromDiscoveryService The device added listener to remove
     */
    public void removeDeviceAddedListener(MystromDiscoveryService mystromDiscoveryService) {
        this.listeners.remove(mystromDiscoveryService);
    }

    /**
     * Called to start the discovery scan. Forces a data refresh.
     */
    public void startDiscoveryScan() {
        refreshData();
    }
}
