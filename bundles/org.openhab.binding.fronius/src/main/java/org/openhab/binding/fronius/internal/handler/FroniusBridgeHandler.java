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
package org.openhab.binding.fronius.internal.handler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge for Fronius devices.
 *
 * @author Gerrit Beine - Initial contribution
 * @author Thomas Rokohl - Refactoring to merge the concepts.
 *         Check if host is reachable.
 */
public class FroniusBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusBridgeHandler.class);
    private static final int DEFAULT_REFRESH_PERIOD = 10;
    private final Set<FroniusBaseThingHandler> services = new HashSet<>();
    private ScheduledFuture<?> refreshJob;

    public FroniusBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void registerService(final FroniusBaseThingHandler service) {
        this.services.add(service);
    }

    @Override
    public void initialize() {
        final FroniusBridgeConfiguration config = getConfigAs(FroniusBridgeConfiguration.class);

        boolean validConfig = true;
        String errorMsg = null;

        String hostname = config.hostname;
        if (hostname == null || hostname.isBlank()) {
            errorMsg = "Parameter 'hostname' is mandatory and must be configured";
            validConfig = false;
        }

        if (config.refreshInterval != null && config.refreshInterval <= 0) {
            errorMsg = "Parameter 'refresh' must be at least 1 second";
            validConfig = false;
        }

        if (validConfig) {
            startAutomaticRefresh(config);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        services.clear();
    }

    /**
     * Start the job refreshing the data
     */
    private void startAutomaticRefresh(FroniusBridgeConfiguration config) {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                boolean online = false;
                try {
                    if (HttpUtil.executeUrl("GET", "http://" + config.hostname, 5000) != null) {
                        online = true;
                    }
                } catch (IOException e) {
                    logger.debug("Connection Error: {}", e.getMessage());
                }

                if (!online) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "hostname or ip is not reachable");
                } else {
                    updateStatus(ThingStatus.ONLINE);
                    for (FroniusBaseThingHandler service : services) {
                        service.refresh(config);
                    }
                }
            };

            int delay = (config.refreshInterval != null) ? config.refreshInterval.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.SECONDS);
        }
    }
}
