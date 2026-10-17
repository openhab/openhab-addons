/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.windhager.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WindhagerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benjamin Utz - Initial contribution
 */
@NonNullByDefault
public class WindhagerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WindhagerHandler.class);

    private @Nullable WindhagerConnector connector;
    private final Map<Integer, ScheduledFuture<?>> refreshJobs = new HashMap<>();
    private final Object lifecycleLock = new Object();
    private final HttpClient httpClient;
    private @Nullable ScheduledFuture<?> initializationJob;
    private volatile boolean disposed;
    private volatile long lifecycleGeneration;

    public WindhagerHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            Channel channel = getThing().getChannel(channelUID.getId());
            if (channel != null) {
                refreshChannels(List.of(channel), lifecycleGeneration);
            }
        }
    }

    @Override
    public void initialize() {
        long generation;
        synchronized (lifecycleLock) {
            disposed = false;
            generation = ++lifecycleGeneration;
        }

        WindhagerConfiguration config = getConfigAs(WindhagerConfiguration.class);

        if (config.hostname.isBlank() || config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Hostname, username and password must be configured.");
            stopHttpClient();
            return;
        }

        @Nullable
        Map<Integer, List<Channel>> channelsByRefreshInterval = getConfiguredChannels();
        if (channelsByRefreshInterval == null) {
            stopHttpClient();
            return;
        }
        if (channelsByRefreshInterval.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "At least one channel with a configured oid must be defined.");
            stopHttpClient();
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        synchronized (lifecycleLock) {
            if (isActive(generation)) {
                initializationJob = scheduler.schedule(
                        () -> initializeClient(config, channelsByRefreshInterval, generation), 0, TimeUnit.SECONDS);
            }
        }
    }

    private @Nullable Map<Integer, List<Channel>> getConfiguredChannels() {
        Map<Integer, List<Channel>> channelsByRefreshInterval = new HashMap<>();
        for (Channel channel : getThing().getChannels()) {
            WindhagerChannelConfiguration channelConfig = channel.getConfiguration()
                    .as(WindhagerChannelConfiguration.class);
            if (channelConfig.oid.isBlank()) {
                logger.debug("Skipping channel {} without a configured oid.", channel.getUID());
                continue;
            }
            if (channelConfig.refreshInterval < 1) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Channel refresh interval must be configured.");
                return null;
            }

            List<Channel> channels = channelsByRefreshInterval.get(channelConfig.refreshInterval);
            if (channels == null) {
                channels = new ArrayList<>();
                channelsByRefreshInterval.put(channelConfig.refreshInterval, channels);
            }
            channels.add(channel);
        }
        channelsByRefreshInterval.replaceAll((interval, channels) -> List.copyOf(channels));
        return Collections.unmodifiableMap(channelsByRefreshInterval);
    }

    private void initializeClient(WindhagerConfiguration config, Map<Integer, List<Channel>> channelsByRefreshInterval,
            long generation) {
        if (!isActive(generation)) {
            return;
        }

        WindhagerConnector localConnector;
        try {
            localConnector = new WindhagerConnector(httpClient, config.hostname, config.port, config.username,
                    config.password);
        } catch (java.net.URISyntaxException | IllegalArgumentException e) {
            if (isActive(generation)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "The configured BioWin hostname is invalid.");
                stopHttpClient();
            }
            logger.debug("Invalid BioWin server URI", e);
            return;
        }

        try {
            httpClient.start();
        } catch (Exception e) {
            localConnector.dispose();
            if (isActive(generation)) {
                stopHttpClient();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not initialize the BioWin webserver connection.");
            }
            logger.debug("Could not start the BioWin HTTP client", e);
            return;
        }

        synchronized (lifecycleLock) {
            if (!isActive(generation)) {
                localConnector.dispose();
                return;
            }
            connector = localConnector;
            for (Map.Entry<Integer, List<Channel>> entry : channelsByRefreshInterval.entrySet()) {
                int refreshInterval = entry.getKey();
                List<Channel> channels = entry.getValue();
                logger.info("Scheduling {} channel(s) with refresh interval {} seconds", channels.size(),
                        refreshInterval);
                ScheduledFuture<?> job = scheduler.scheduleWithFixedDelay(() -> refreshChannels(channels, generation),
                        0, refreshInterval, TimeUnit.SECONDS);
                refreshJobs.put(refreshInterval, job);
            }
        }
    }

    private void refreshChannels(List<Channel> channels, long generation) {
        WindhagerConnector localConnector = connector;
        if (!isActive(generation) || localConnector == null) {
            return;
        }

        boolean communicationFailure = false;
        for (Channel channel : channels) {
            communicationFailure |= refreshChannel(localConnector, channel, generation);
        }
        if (isActive(generation)) {
            if (communicationFailure) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not communicate with the BioWin webserver.");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    private boolean refreshChannel(WindhagerConnector localConnector, Channel channel, long generation) {
        WindhagerChannelConfiguration channelConfig = channel.getConfiguration()
                .as(WindhagerChannelConfiguration.class);

        WindhagerConnector.ValueReadResult result = localConnector.readValue(channelConfig.oid);
        if (!isActive(generation)) {
            return result.communicationFailure();
        }
        BigDecimal value = result.value();
        if (value == null) {
            updateState(channel.getUID(), UnDefType.UNDEF);
        } else {
            updateState(channel.getUID(), new DecimalType(value));
        }
        return result.communicationFailure();
    }

    private boolean isActive(long generation) {
        return !disposed && lifecycleGeneration == generation;
    }

    @Override
    public void dispose() {
        WindhagerConnector localConnector;
        synchronized (lifecycleLock) {
            disposed = true;
            lifecycleGeneration++;
            ScheduledFuture<?> localInitializationJob = initializationJob;
            if (localInitializationJob != null) {
                localInitializationJob.cancel(true);
                initializationJob = null;
            }
            for (ScheduledFuture<?> job : refreshJobs.values()) {
                job.cancel(true);
            }
            refreshJobs.clear();
            localConnector = connector;
            connector = null;
        }
        if (localConnector != null) {
            localConnector.dispose();
        }
        stopHttpClient();
        super.dispose();
    }

    private void stopHttpClient() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Could not stop the BioWin HTTP client", e);
        }
    }
}
