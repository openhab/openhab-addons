/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ambientweather.internal.handler;

import static org.openhab.binding.ambientweather.internal.AmbientWeatherBindingConstants.CONFIG_MAC_ADDRESS;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ambientweather.internal.config.BridgeConfig;
import org.openhab.binding.ambientweather.internal.model.DeviceJson;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AmbientWeatherBridgeHandler} is responsible for handling the
 * bridge things created to use the Ambient Weather service.
 *
 * @author Mark Hilbush - Initial Contribution
 */
@NonNullByDefault
public class AmbientWeatherBridgeHandler extends BaseBridgeHandler {
    // URL to retrieve device list from Ambient Weather
    private static final String DEVICES_URL = "https://api.ambientweather.net/v1/devices?applicationKey=%APPKEY%&apiKey=%APIKEY%";

    // Timeout of the call to the Ambient Weather devices API
    public static final int DEVICES_API_TIMEOUT = 20000;

    // Time to wait after failed key validation
    public static final long KEY_VALIDATION_DELAY = 60L;

    private final Logger logger = LoggerFactory.getLogger(AmbientWeatherBridgeHandler.class);

    // Job to validate app and api keys
    @Nullable
    private ScheduledFuture<?> validateKeysJob;

    // Application key is granted only by request from developer
    private String applicationKey = "";

    // API key assigned to user in ambientweather.net dashboard
    private String apiKey = "";

    // Used Ambient Weather real-time API to retrieve weather data
    // for weather stations assigned to an API key
    private AmbientWeatherEventListener listener;

    private final Gson gson = new Gson();

    private Runnable validateKeysRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Validating application and API keys");

            String response = null;
            try {
                // Query weather stations (devices) from Ambient Weather
                String url = DEVICES_URL.replace("%APPKEY%", getApplicationKey()).replace("%APIKEY%", getApiKey());
                logger.debug("Bridge: Querying list of devices from ambient weather service");
                response = HttpUtil.executeUrl("GET", url, DEVICES_API_TIMEOUT);
                logger.trace("Bridge: Response = {}", response);
                // Got a response so the keys are good
                DeviceJson[] stations = gson.fromJson(response, DeviceJson[].class);
                logger.debug("Bridge: Application and API keys are valid with {} stations", stations.length);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Connecting to service");
                // Start up the real-time API listener
                listener.start(applicationKey, apiKey, gson);
            } catch (IOException e) {
                // executeUrl throws IOException when it gets a Not Authorized (401) response
                logger.debug("Bridge: Got IOException: {}", e.getMessage());
                setThingOfflineWithCommError(e.getMessage(), "Invalid API or application key");
                rescheduleValidateKeysJob();
            } catch (IllegalArgumentException e) {
                logger.debug("Bridge: Got IllegalArgumentException: {}", e.getMessage());
                setThingOfflineWithCommError(e.getMessage(), "Unable to get devices");
                rescheduleValidateKeysJob();
            } catch (JsonSyntaxException e) {
                logger.debug("Bridge: Got JsonSyntaxException: {}", e.getMessage());
                setThingOfflineWithCommError(e.getMessage(), "Error parsing json response");
                rescheduleValidateKeysJob();
            }
        }
    };

    public AmbientWeatherBridgeHandler(Bridge bridge) {
        super(bridge);
        listener = new AmbientWeatherEventListener(this);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        // If there are keys in the config, schedule the job to validate them
        if (hasApplicationKey() && hasApiKey()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Awaiting key validation");
            scheduleValidateKeysJob();
        }
    }

    /*
     * Check if an application key has been provided in the thing config
     */
    private boolean hasApplicationKey() {
        String configApplicationKey = getConfigAs(BridgeConfig.class).applicationKey;
        if (configApplicationKey == null || configApplicationKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing application key");
            return false;
        }
        applicationKey = configApplicationKey;
        return true;
    }

    /*
     * Check if an API key has been provided in the thing config
     */
    private boolean hasApiKey() {
        String configApiKey = getConfigAs(BridgeConfig.class).apiKey;
        if (configApiKey == null || configApiKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing API key");
            return false;
        }
        apiKey = configApiKey;
        return true;
    }

    public void setThingOfflineWithCommError(@Nullable String errorDetail, @Nullable String statusDescription) {
        String status = statusDescription != null ? statusDescription : "null";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, status);
    }

    @Override
    public void dispose() {
        cancelValidateKeysJob();
        listener.stop();
    }

    /**
     * Start the job to validate the API and Application keys.
     * A side-effect of this is that we will discover the devices
     * MAC addresses that are associated with the API key.
     */
    private void scheduleValidateKeysJob() {
        if (validateKeysJob == null) {
            validateKeysJob = scheduler.schedule(validateKeysRunnable, 5, TimeUnit.SECONDS);
        }
    }

    private void cancelValidateKeysJob() {
        if (validateKeysJob != null) {
            validateKeysJob.cancel(true);
            validateKeysJob = null;
        }
    }

    public void rescheduleValidateKeysJob() {
        logger.debug("Bridge: Key validation will run again in {} seconds", KEY_VALIDATION_DELAY);
        validateKeysJob = scheduler.schedule(validateKeysRunnable, KEY_VALIDATION_DELAY, TimeUnit.SECONDS);
    }

    /*
     * Keep track of the station handlers so that the listener can route data events
     * to the correct handler.
     */
    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        String macAddress = (String) childThing.getConfiguration().get(CONFIG_MAC_ADDRESS);
        listener.addHandler((AmbientWeatherStationHandler) childHandler, macAddress);
        logger.debug("Bridge: Station handler initialized for {} with MAC {}", childThing.getUID(), macAddress);
        listener.resubscribe();
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        String macAddress = (String) childThing.getConfiguration().get(CONFIG_MAC_ADDRESS);
        listener.removeHandler((AmbientWeatherStationHandler) childHandler, macAddress);
        logger.debug("Bridge: Station handler disposed for {} with MAC {}", childThing.getUID(), macAddress);
    }

    // Callback used by EventListener to update bridge status
    public void markBridgeOffline(String reason) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
    }

    // Callback used by EventListener to update bridge status
    public void markBridgeOnline() {
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Handler doesn't support any commands
    }
}
