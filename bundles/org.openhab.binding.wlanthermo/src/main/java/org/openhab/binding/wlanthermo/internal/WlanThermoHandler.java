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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
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
import org.openhab.binding.wlanthermo.internal.api.data.Data;
import org.openhab.binding.wlanthermo.internal.api.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WlanThermoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WlanThermoHandler.class);

    private @Nullable WlanThermoConfiguration config;
    private HttpClient httpClient = new HttpClient();
    @Nullable
    private ScheduledFuture<?> pollingScheduler;
    private Gson gson = new Gson();
    private Data data = new Data();
    private Settings settings = new Settings();

    public WlanThermoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(WlanThermoConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        try {
            if (config.getUsername() != null && !config.getUsername().isEmpty() && config.getPassword() != null
                    && !config.getPassword().isEmpty()) {
                AuthenticationStore authStore = httpClient.getAuthenticationStore();
                authStore.addAuthentication(new DigestAuthentication(config.getUri(), Authentication.ANY_REALM,
                        config.getUsername(), config.getPassword()));
            }
            httpClient.start();

            scheduler.schedule(() -> {
                checkConnection();
            }, config.getPollingInterval(), TimeUnit.SECONDS);

            logger.debug("Finished initializing!");
        } catch (Exception e) {
            logger.error("Failed to initialize!", e);
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
                pollingScheduler = scheduler.scheduleWithFixedDelay(() -> {
                    update();
                }, 0, config.getPollingInterval(), TimeUnit.SECONDS);
                updateState(SYSTEM + "#" + SYSTEM_ONLINE, OnOffType.ON);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "WlanThermo not found under given address.");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException | URISyntaxException e) {
            logger.debug("Failed to connect.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not connect to WlanThermo at "+config.getIpAddress());
            if (pollingScheduler != null) {
                pollingScheduler.cancel(true);
            }
            pollingScheduler = scheduler.schedule(() -> {
                checkConnection();
            }, config.getPollingInterval(), TimeUnit.SECONDS);
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
                logger.error("Could not handle command of type " + command.getClass().toGenericString()
                        + " for channel " + channelUID.getId() + "!");
            }
        }
    }

    private void update() {
        try {
            //Update objects with data from device
            String json = httpClient.GET(config.getUri("/data")).getContentAsString();
            data = gson.fromJson(json, Data.class);
            logger.debug("Received at /data: " + json);
            json = httpClient.GET(config.getUri("/settings")).getContentAsString();
            settings = gson.fromJson(json, Settings.class);
            logger.debug("Received at /settings: " + json);
            
            
            //Update channels
            for (Channel channel : thing.getChannels()) {
                State state = data.getState(channel.getUID(), this);
                if (state != null) {
                    updateState(channel.getUID(), state);
                } else {
                    String trigger = data.getTrigger(channel.getUID());
                    if (trigger != null) {
                        triggerChannel(channel.getUID(), trigger);
                    }
                }

            }
            

        } catch (InterruptedException | ExecutionException | TimeoutException | URISyntaxException e) {
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
        for (org.openhab.binding.wlanthermo.internal.api.data.Channel c : data.getChannel()) {
            try {
                String json = gson.toJson(c);
                logger.debug("Pushing: " + json);
                URI uri = config.getUri("/setchannels");
                int status = httpClient.POST(uri)
                                .content(new StringContentProvider(json), "application/json")
                                .timeout(5, TimeUnit.SECONDS)
                                .send()
                                .getStatus();
                if (status == 401) {
                    logger.error(
                            "No or wrong login credentials provided. Please configure username/password for write access to WlanThermo!");
                } else if (status != 200) {
                    logger.error("Failed to update channel " + c.getName() + " on device, Statuscode " + status
                            + " on URI " + uri.toString());
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | URISyntaxException e) {
                logger.error("Failed to update channel " + c.getName() + " on device", e);
            }
        }
    }

    @Override
    public void handleRemoval() {
        if (pollingScheduler != null) {
            boolean stopped = pollingScheduler.cancel(true);
            logger.debug("Stopped polling: " + stopped);
        }
        try {
            httpClient.stop();
            logger.debug("HTTP client stopped");
        } catch (Exception e) {
            logger.error("Failed to stop HttpClient", e);
        } 
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void dispose() {
        if (pollingScheduler != null) {
            boolean stopped = pollingScheduler.cancel(true);
            logger.debug("Stopped polling: " + stopped);
        }
        try {
            httpClient.stop();
            logger.debug("HTTP client stopped");
        } catch (Exception e) {
            logger.error("Failed to stop HttpClient", e);
        }
        for (Channel channel : thing.getChannels()) {
            if (channel.getUID().getId().equals(SYSTEM + "#" + SYSTEM_ONLINE)) {
                updateState(channel.getUID(), OnOffType.OFF);
            } else {
                updateState(channel.getUID(), UnDefType.UNDEF);
            }
        }
        
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
    }

    public Settings getSettings() {
        return settings;
    }
}
