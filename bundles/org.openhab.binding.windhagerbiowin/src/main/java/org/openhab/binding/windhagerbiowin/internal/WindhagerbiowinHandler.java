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
package org.openhab.binding.windhagerbiowin.internal;

import static org.openhab.binding.windhagerbiowin.internal.WindhagerbiowinBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WindhagerbiowinHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author BenjiU - Initial contribution
 */
@NonNullByDefault
public class WindhagerbiowinHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WindhagerbiowinHandler.class);

    private @Nullable WindhagerbiowinConfiguration config;
    private @Nullable WindhagerbiowinConnector connector;
    private final Map<Integer, ScheduledFuture<?>> refreshJobs = new HashMap<>();

    public WindhagerbiowinHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshChannel(channelUID);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(WindhagerbiowinConfiguration.class);

        if (config.hostname.isBlank() || config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Hostname, username and password must be configured.");
            return;
        }

        connector = new WindhagerbiowinConnector(config.hostname, config.port, config.username, config.password);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            boolean thingReachable = connector.connect();
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
                scheduleChannelRefresh();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not connect to the BioWin webserver.");
            }
        });
    }

    private void scheduleChannelRefresh() {
        WindhagerbiowinConnector localConnector = connector;
        if (localConnector == null) {
            return;
        }

        Map<Integer, List<Channel>> channelsByRefreshInterval = new HashMap<>();
        boolean hasUsableChannel = false;
        for (Channel channel : getThing().getChannels()) {
            WindhagerbiowinChannelConfiguration channelConfig = channel.getConfiguration()
                    .as(WindhagerbiowinChannelConfiguration.class);
            if (channelConfig.path.isBlank()) {
                logger.debug("Skipping channel {} without a configured path.", channel.getUID());
                continue;
            }
            if (channelConfig.refreshInterval < 1) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Channel refresh interval must be configured.");
                return;
            }

            hasUsableChannel = true;
            channelsByRefreshInterval.computeIfAbsent(channelConfig.refreshInterval, key -> new ArrayList<>())
                    .add(channel);
        }

        if (!hasUsableChannel) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "At least one channel with a configured path must be defined.");
            return;
        }

        for (Map.Entry<Integer, List<Channel>> entry : channelsByRefreshInterval.entrySet()) {
            int refreshInterval = entry.getKey();
            List<Channel> channels = entry.getValue();
            ScheduledFuture<?> existingJob = refreshJobs.get(refreshInterval);
            if (existingJob != null && !existingJob.isCancelled()) {
                continue;
            }

            logger.info("Scheduling {} channel(s) with refresh interval {} seconds", channels.size(), refreshInterval);
            ScheduledFuture<?> job = scheduler.scheduleWithFixedDelay(() -> {
                for (Channel channel : channels) {
                    refreshChannel(channel.getUID());
                }
            }, 0, refreshInterval, TimeUnit.SECONDS);
            refreshJobs.put(refreshInterval, job);
        }
    }

    private void refreshChannel(ChannelUID channelUID) {
        WindhagerbiowinConnector localConnector = connector;
        Channel channel = getThing().getChannel(channelUID.getId());
        if (localConnector == null || channel == null) {
            return;
        }

        WindhagerbiowinChannelConfiguration channelConfig = channel.getConfiguration()
                .as(WindhagerbiowinChannelConfiguration.class);

        String acceptedItemType = channel.getAcceptedItemType();
        if ("String".equalsIgnoreCase(acceptedItemType)) {
            String value = localConnector.readString(channelConfig.path);
            if (value != null) {
                updateState(channelUID, new StringType(value));
            } else {
                logger.debug("Could not read configured BioWin string path {}", channelConfig.path);
            }
            return;
        }

        BigDecimal value = localConnector.readValue(channelConfig.path);
        if (value != null) {
            updateState(channelUID, new DecimalType(value));
        } else {
            logger.debug("Could not read configured BioWin path {}", channelConfig.path);
        }
    }

    @Override
    public void dispose() {
        for (ScheduledFuture<?> job : refreshJobs.values()) {
            job.cancel(true);
        }
        refreshJobs.clear();
        super.dispose();
    }
}
