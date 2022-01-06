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
package org.openhab.binding.wlanthermo.internal;

import static org.openhab.binding.wlanthermo.internal.WlanThermoUtil.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
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
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link WlanThermoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public abstract class WlanThermoHandler extends BaseThingHandler {

    private final boolean extendedConfig;
    protected WlanThermoConfiguration config = new WlanThermoConfiguration();
    protected final HttpClient httpClient;
    protected final Logger logger = LoggerFactory.getLogger(WlanThermoHandler.class);
    protected final Gson gson = new Gson();
    protected @Nullable ScheduledFuture<?> pollingScheduler;

    public WlanThermoHandler(Thing thing, HttpClient httpClient, boolean extendedConfig) {
        super(thing);
        this.httpClient = httpClient;
        this.extendedConfig = extendedConfig;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        try {
            if (extendedConfig) {
                config = getConfigAs(WlanThermoExtendedConfiguration.class);
                WlanThermoExtendedConfiguration extendedConfig = (WlanThermoExtendedConfiguration) config;
                if (extendedConfig.getUsername().isEmpty() && !extendedConfig.getPassword().isEmpty()) {
                    AuthenticationStore authStore = httpClient.getAuthenticationStore();
                    authStore.addAuthentication(new DigestAuthentication(config.getUri(), Authentication.ANY_REALM,
                            extendedConfig.getUsername(), extendedConfig.getPassword()));
                }
            } else {
                config = getConfigAs(WlanThermoConfiguration.class);
            }
            pollingScheduler = scheduler.scheduleWithFixedDelay(this::checkConnectionAndUpdate, 0,
                    config.getPollingInterval(), TimeUnit.SECONDS);
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Failed to initialize WlanThermo: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> oldScheduler = pollingScheduler;
        if (oldScheduler != null) {
            boolean stopped = oldScheduler.cancel(true);
            logger.debug("Stopped polling: {}", stopped);
        }
        pollingScheduler = null;
    }

    protected void checkConnectionAndUpdate() {
        if (this.thing.getStatus() != ThingStatus.ONLINE) {
            try {
                if (httpClient.GET(config.getUri()).getStatus() == 200) {
                    updateStatus(ThingStatus.ONLINE);
                    // rerun immediately to update state
                    checkConnectionAndUpdate();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "WlanThermo not found under given address.");
                }
            } catch (URISyntaxException | ExecutionException | TimeoutException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not connect to WlanThermo at " + config.getIpAddress() + ": " + e.getMessage());
            } catch (InterruptedException e) {
                logger.debug("Connection check interrupted. {}", e.getMessage());
            }
        } else {
            pull();
        }
    }

    protected boolean doPost(String endpoint, String json) throws InterruptedException {
        try {
            URI uri = config.getUri(endpoint);
            int status = httpClient.POST(uri).content(new StringContentProvider(json), "application/json")
                    .timeout(5, TimeUnit.SECONDS).send().getStatus();
            if (status == 401) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "No or wrong login credentials provided. Please configure username/password for write access to WlanThermo!");
                return false;
            } else if (status != 200) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to update channel on device, Statuscode " + status + " on URI " + uri.toString());
                logger.debug("Payload sent: {}", json);
                // Still continue to try next channel
                return true;
            } else {
                updateStatus(ThingStatus.ONLINE);
                return true;
            }
        } catch (TimeoutException | ExecutionException | URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to update channel on device: " + e.getMessage());
            return false;
        }
    }

    protected <T> T doGet(String endpoint, Class<T> object) throws InterruptedException, WlanThermoException {
        try {
            String json = httpClient.GET(config.getUri(endpoint)).getContentAsString();
            logger.debug("Received at {}: {}", endpoint, json);
            return requireNonNull(gson.fromJson(json, object));
        } catch (URISyntaxException | ExecutionException | TimeoutException | WlanThermoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Update failed: " + e.getMessage());
            for (Channel channel : thing.getChannels()) {
                updateState(channel.getUID(), UnDefType.UNDEF);
            }
            throw new WlanThermoException(e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            try {
                State s = getState(channelUID);
                updateState(channelUID, s);
            } catch (WlanThermoException e) {
                logger.debug("Could not handle command of type {} for channel {}!",
                        command.getClass().toGenericString(), channelUID.getId());
            }
        } else {
            if (setState(channelUID, command) && thing.getStatus() == ThingStatus.ONLINE) {
                logger.debug("Data updated, pushing changes");
                scheduler.execute(this::push);
            } else {
                logger.debug("Could not handle command of type {} for channel {}!",
                        command.getClass().toGenericString(), channelUID.getId());
            }
        }
    }

    protected abstract void push();

    protected abstract void pull();

    protected abstract State getState(ChannelUID channelUID)
            throws WlanThermoInputException, WlanThermoUnknownChannelException;

    protected abstract boolean setState(ChannelUID channelUID, Command command);
}
