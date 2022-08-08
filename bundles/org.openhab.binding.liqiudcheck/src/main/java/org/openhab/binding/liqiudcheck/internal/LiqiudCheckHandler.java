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
package org.openhab.binding.liqiudcheck.internal;

import static org.openhab.binding.liqiudcheck.internal.LiqiudCheckBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.liqiudcheck.internal.httpClient.LiquidCheckHttpClient;
import org.openhab.binding.liqiudcheck.internal.json.Response;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link LiqiudCheckHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class LiqiudCheckHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LiqiudCheckHandler.class);

    private Map<String, String> oldProps;

    private LiqiudCheckConfiguration config = getConfigAs(LiqiudCheckConfiguration.class);

    private @Nullable ScheduledFuture<?> polling;

    public LiqiudCheckHandler(Thing thing) {
        super(thing);
        oldProps = thing.getProperties();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (LEVEL_CHANNEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(LiqiudCheckConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {

            LiquidCheckHttpClient httpClient = new LiquidCheckHttpClient(config);
            boolean thingReachable = httpClient.isConnected(); // <background task with long running initialization
            // here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
                PollingForData pollingRunnable = new PollingForData(httpClient);
                polling = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, config.refreshInterval,
                        TimeUnit.SECONDS);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private Map<String, String> createPropertyMap(Response response) {
        Map<String, String> properties = new HashMap<>();
        properties.put(CONFIG_ID_FIRMWARE, response.payload.device.firmware);
        properties.put(CONFIG_ID_HARDWARE, response.payload.device.hardware);
        properties.put(CONFIG_ID_NAME, response.payload.device.name);
        properties.put(CONFIG_ID_MANUFACTURER, response.payload.device.manufacturer);
        properties.put(CONFIG_ID_UUID, response.payload.device.uuid);
        properties.put(CONFIG_ID_SECURITY_CODE, response.payload.device.security.code);
        properties.put(CONFIG_ID_IP, response.payload.wifi.station.ip);
        properties.put(CONFIG_ID_MAC, response.payload.wifi.station.mac);
        properties.put(CONFIG_ID_SSID, response.payload.wifi.accessPoint.ssid);
        return properties;
    }

    @Override
    public void dispose() {
        polling.cancel(true);
    }

    private class PollingForData implements Runnable {

        private final LiquidCheckHttpClient client;

        public PollingForData(LiquidCheckHttpClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                String response = client.pollData();
                Response json = new Gson().fromJson(response, Response.class);
                if (null != json) {
                    Map<String, String> properties = createPropertyMap(json);
                    if (!oldProps.equals(properties)) {
                        oldProps = properties;
                        updateProperties(properties);
                    }
                }

                updateState(CONTENT_CHANNEL, new QuantityType<>(json.payload.measure.content, Units.LITRE));
                updateState(LEVEL_CHANNEL, new QuantityType<>(json.payload.measure.level, SIUnits.METRE));
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.error("This went wrong: {}", e);
            }
        }
    }
}
