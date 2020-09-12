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
package org.openhab.binding.wlanthermo.internal;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.SYSTEM;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.SYSTEM_ONLINE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wlanthermo.internal.api.nano.data.Data;
import org.openhab.binding.wlanthermo.internal.api.nano.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link WlanThermoNanoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoNanoHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WlanThermoNanoHandler.class);

    private WlanThermoNanoConfiguration config = new WlanThermoNanoConfiguration();
    private final HttpClient httpClient;
    private @Nullable ScheduledFuture<?> pollingScheduler;
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(WlanThermoBindingConstants.WLANTHERMO_THREAD_POOL);
    private final Gson gson = new Gson();
    private Data data = new Data();
    private Settings settings = new Settings();

    public WlanThermoNanoHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing WlanThermo Nano!");
        config = getConfigAs(WlanThermoNanoConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        try {
            if (config.getUsername() != null && !config.getUsername().isEmpty() && config.getPassword() != null
                    && !config.getPassword().isEmpty()) {
                AuthenticationStore authStore = httpClient.getAuthenticationStore();
                authStore.addAuthentication(new DigestAuthentication(config.getUri(), Authentication.ANY_REALM,
                        config.getUsername(), config.getPassword()));
            }
            httpClient.start();

            scheduler.schedule(this::checkConnection, config.getPollingInterval(), TimeUnit.SECONDS);

            logger.debug("Finished initializing WlanThermo Nano!");
        } catch (Exception e) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Failed to initialize WlanThermo Nano!");
            logger.debug("Failed to initialize WlanThermo Nano!", e);
        }
    }

    private void checkConnection() {
        updateState(SYSTEM + "#" + SYSTEM_ONLINE, OnOffType.OFF);
        try {
            if (httpClient.GET(config.getUri()).getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
                if (pollingScheduler != null) {
                    pollingScheduler.cancel(true);
                }
                pollingScheduler = scheduler.scheduleWithFixedDelay(this::update, 0, config.getPollingInterval(),
                        TimeUnit.SECONDS);
                updateState(SYSTEM + "#" + SYSTEM_ONLINE, OnOffType.ON);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "WlanThermo not found under given address.");
            }
        } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Failed to connect.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to WlanThermo at " + config.getIpAddress());
            if (pollingScheduler != null) {
                pollingScheduler.cancel(false);
            }
            pollingScheduler = scheduler.schedule(this::checkConnection, config.getPollingInterval(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            State s = data.getState(channelUID, this);
            if (s != null)
                updateState(channelUID, s);
        } else {
            if (data.setState(channelUID, command)) {
                logger.debug("Data updated, pushing changes");
                push();
            } else {
                logger.warn("Could not handle command of type {} for channel {}!", command.getClass().toGenericString(),
                        channelUID.getId());
            }
        }
    }

    private void update() {
        try {
            // Update objects with data from device
            String json = httpClient.GET(config.getUri("/data")).getContentAsString();
            data = gson.fromJson(json, Data.class);
            logger.debug("Received at /data: {}", json);
            json = httpClient.GET(config.getUri("/settings")).getContentAsString();
            settings = gson.fromJson(json, Settings.class);
            logger.debug("Received at /settings: {}", json);

            // Update channels
            for (Channel channel : thing.getChannels()) {
                State state = data.getState(channel.getUID(), this);
                if (state != null) {
                    updateState(channel.getUID(), state);
                } else {
                    // if we could not obtain a state, try trigger instead
                    String trigger = data.getTrigger(channel.getUID());
                    if (trigger != null) {
                        triggerChannel(channel.getUID(), trigger);
                    }
                }
            }
        } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Update failed, checking connection", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Update failed, reconnecting...");
            if (pollingScheduler != null) {
                pollingScheduler.cancel(false);
            }
            for (Channel channel : thing.getChannels()) {
                if (channel.getUID().getId().equals(SYSTEM + "#" + SYSTEM_ONLINE)) {
                    updateState(channel.getUID(), OnOffType.OFF);
                } else {
                    updateState(channel.getUID(), UnDefType.UNDEF);
                }
            }
            checkConnection();
        }
    }

    private void push() {
        for (org.openhab.binding.wlanthermo.internal.api.nano.data.Channel c : data.getChannel()) {
            try {
                String json = gson.toJson(c);
                logger.debug("Pushing: {}", json);
                URI uri = config.getUri("/setchannels");
                int status = httpClient.POST(uri).content(new StringContentProvider(json), "application/json")
                        .timeout(5, TimeUnit.SECONDS).send().getStatus();
                if (status == 401) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "No or wrong login credentials provided. Please configure username/password for write access to WlanThermo!");
                    logger.warn(
                            "No or wrong login credentials provided. Please configure username/password for write access to WlanThermo!");
                } else if (status != 200) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to update channel "
                            + c.getName() + " on device, Statuscode " + status + " on URI " + uri.toString());
                    logger.warn("Failed to update channel {} on device, Statuscode {} on URI {}", c.getName(), status,
                            uri.toString());
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | URISyntaxException e) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to update channel " + c.getName() + " on device!");
                logger.warn("Failed to update channel {} on device", c.getName(), e);
            }
        }
    }

    @Override
    public void dispose() {
        if (pollingScheduler != null) {
            boolean stopped = pollingScheduler.cancel(true);
            logger.debug("Stopped polling: {}", stopped);
        }
        try {
            httpClient.stop();
            logger.debug("HTTP client stopped");
        } catch (Exception e) {
            logger.error("Failed to stop HttpClient", e);
        }
    }

    public Settings getSettings() {
        return settings;
    }
}
