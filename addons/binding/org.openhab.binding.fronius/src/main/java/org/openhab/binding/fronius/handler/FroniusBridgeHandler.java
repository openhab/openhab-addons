/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.fronius.FroniusBridgeConfiguration;
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
        if (StringUtils.trimToNull(config.hostname) == null) {
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
                    InetAddress inet;
                    inet = InetAddress.getByName(config.hostname);
                    if (inet.isReachable(5000)) {
                        online = true;
                    }
                } catch (IOException e) {
                    logger.debug("Connection Error: {}", e.getMessage());
                    return;
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
