/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.handler;

import static org.openhab.binding.ambientweather.internal.AmbientWeatherBindingConstants.CONFIG_MAC_ADDRESS;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.ambientweather.internal.config.BridgeConfig;
import org.openhab.binding.ambientweather.internal.json.DevicesJson;
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
    private final Logger logger = LoggerFactory.getLogger(AmbientWeatherBridgeHandler.class);

    // URL to retrieve device list from Ambient Weather
    private static String devicesUrl = "https://api.ambientweather.net/v1/devices?applicationKey=%APPKEY%&apiKey=%APIKEY%";

    // Timeout of the call to the Ambient Weather devices API
    public static final int DEVICES_API_TIMEOUT = 20000;

    // Time to wait after failed key validation
    public static final long KEY_VALIDATION_DELAY = 60L;

    // Job to validate app and api keys
    @Nullable
    private ScheduledFuture<?> validateKeysJob;

    // Application key is granted only by request from developer
    @Nullable
    private String applicationKey;

    // API key assigned to user in ambientweather.net dashboard
    @Nullable
    private String apiKey;

    // Used Ambient Weather real-time API to retrieve weather data
    // for weather stations assigned to an API key
    private AmbientWeatherEventListener listener;

    private final Gson gson;

    private Runnable validateKeysRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Validating application and API keys");

            String response = null;
            try {
                // Query weather stations (devices) from Ambient Weather
                String url = devicesUrl.replace("%APPKEY%", getApplicationKey()).replace("%APIKEY%", getApiKey());
                logger.debug("Bridge: Querying list of devices from ambient weather service");
                response = HttpUtil.executeUrl("GET", url, DEVICES_API_TIMEOUT);
                logger.trace("Bridge: Response = {}", response);
            } catch (IOException e) {
                // executeUrl throws IOException when it gets a Not Authorized (401) response
                logger.debug("Bridge: Got IOException: {}", e.getMessage());
                updateThingStatus(e.getMessage(), "Invalid API or application key");
                rescheduleValidateKeysJob();
                return;
            } catch (IllegalArgumentException e) {
                logger.debug("Bridge: Got IllegalArgumentException: {}", e.getMessage());
                updateThingStatus(e.getMessage(), "Unable to get devices");
                rescheduleValidateKeysJob();
                return;
            }
            try {
                // Got a response so the keys are good
                DevicesJson stations = gson.fromJson(response, DevicesJson.class);
                logger.debug("Bridge: Application and API keys are valid with {} stations", stations.size());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Connecting to service");
                // Start up the real-time API listener
                listener.start(applicationKey, apiKey);
            } catch (JsonSyntaxException e) {
                logger.debug("Bridge: Got JsonSyntaxException: {}", e.getMessage());
                updateThingStatus(e.getMessage(), "Error parsing json response");
                rescheduleValidateKeysJob();
                return;
            }
        }
    };

    public AmbientWeatherBridgeHandler(Bridge bridge) {
        super(bridge);
        gson = new Gson();
        listener = new AmbientWeatherEventListener(this);
    }

    @Override
    public void initialize() {
        logger.debug("Bridge: Initializing ambientweather bridge handler.");
        // If there are keys in the config, schedule the job to validate them
        if (haveApplicationKey() && haveApiKey()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Awaiting key validation");
            scheduleValidateKeysJob();
        }
    }

    /*
     * Check if an application key has been provided in the thing config
     */
    private boolean haveApplicationKey() {
        String configApplicationKey = getConfigAs(BridgeConfig.class).getApplicationKey();
        if (StringUtils.isEmpty(configApplicationKey)) {
            logger.debug("Bridge: Application key is missing for thing {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing application key");
            return false;
        }
        applicationKey = configApplicationKey;
        return true;
    }

    /*
     * Check if an API key has been provided in the thing config
     */
    private boolean haveApiKey() {
        String configApiKey = getConfigAs(BridgeConfig.class).getApiKey();
        if (StringUtils.isEmpty(configApiKey)) {
            logger.debug("Bridge: API key is missing for thing {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing API key");
            return false;
        }
        apiKey = configApiKey;
        return true;
    }

    public void updateThingStatus(String errorDetail, String statusDescription) {
        logger.debug("Bridge: Key validation FAILED. Setting bridge OFFLINE: {}", errorDetail);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, statusDescription);
    }

    @Override
    public void dispose() {
        logger.debug("Bridge: Disposing ambientweather bridge handler.");
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

    public @Nullable String getApplicationKey() {
        return applicationKey;
    }

    public @Nullable String getApiKey() {
        return apiKey;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Handler doesn't support any commands
    }
}
