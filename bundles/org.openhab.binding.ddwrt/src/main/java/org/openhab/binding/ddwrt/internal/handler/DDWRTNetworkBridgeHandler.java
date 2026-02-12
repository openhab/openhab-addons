/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.handler;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_TOTAL_CLIENTS;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDiscoveryService;
import org.openhab.binding.ddwrt.internal.DDWRTNetworkConfiguration;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetwork;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DDWRTNetworkBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTNetworkBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(DDWRTNetworkBridgeHandler.class));

    private DDWRTNetworkConfiguration config = new DDWRTNetworkConfiguration();

    private volatile DDWRTNetwork network = new DDWRTNetwork(); /* volatile because accessed from multiple threads */

    private @Nullable ScheduledFuture<?> refreshJob;

    public DDWRTNetworkBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    // Public API

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(DDWRTDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Ignoring command = {} for channel = {} - the DDWRT Network is read-only!", command, channelUID);

        if (CHANNEL_TOTAL_CLIENTS.equals(channelUID.getId())) {
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
        config = getConfigAs(DDWRTNetworkConfiguration.class);

        logger.debug("Initializing DDWRT Network Bridge handler '{}' with config = {}.", getThing().getUID(), config);

        updateStatus(ThingStatus.UNKNOWN);
        network.setBridgeUID(getThing().getUID());

        // execute setconfig in the background because it can trigger a refresh
        scheduler.schedule(() -> {
            network.setConfig(config);
            synchronized (this) {
                if (refreshJob == null) {
                    logger.debug("Scheduling refresh job every {}s", config.refreshInterval);
                    refreshJob = scheduler.scheduleWithFixedDelay(() -> network.refresh(), 0, config.refreshInterval,
                            TimeUnit.SECONDS);
                } else {
                    network.refresh();
                }
            }

        }, 10, TimeUnit.MILLISECONDS);
    }

    public @Nullable DDWRTNetwork getNetwork() {
        return network;
    }

    private void cancelRefreshJob() {
        synchronized (this) {
            final ScheduledFuture<?> rj = refreshJob;

            if (rj != null) {
                logger.debug("Cancelling refresh job");
                rj.cancel(true);
                refreshJob = null;
            }
        }
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
        network.dispose();
    }
}
