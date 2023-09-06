/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.FroniusHttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge for Fronius devices.
 *
 * @author Gerrit Beine - Initial contribution
 * @author Thomas Rokohl - Refactoring to merge the concepts.
 *         Check if host is reachable.
 * @author Jimmy Tanagra - Refactor the child services registration
 *         Refactor host online check
 */
@NonNullByDefault
public class FroniusBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusBridgeHandler.class);
    private static final int DEFAULT_REFRESH_PERIOD = 10;
    private final Set<FroniusBaseThingHandler> services = new HashSet<>();
    private @Nullable ScheduledFuture<?> refreshJob;

    public FroniusBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
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
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof FroniusBaseThingHandler) {
            this.services.add((FroniusBaseThingHandler) childHandler);
            restartAutomaticRefresh();
        } else {
            logger.debug("Child handler {} not added because it is not an instance of FroniusBaseThingHandler",
                    childThing.getUID().getId());
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        this.services.remove((FroniusBaseThingHandler) childHandler);
    }

    private void restartAutomaticRefresh() {
        if (refreshJob != null) { // refreshJob should be null if the config isn't valid
            refreshJob.cancel(false);
            startAutomaticRefresh();
        }
    }

    /**
     * Start the job refreshing the data
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            final FroniusBridgeConfiguration config = getConfigAs(FroniusBridgeConfiguration.class);
            Runnable runnable = () -> {
                try {
                    checkBridgeOnline(config);
                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    for (FroniusBaseThingHandler service : services) {
                        service.refresh(config);
                    }
                } catch (FroniusCommunicationException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                }
            };

            int delay = (config.refreshInterval != null) ? config.refreshInterval.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 1, delay, TimeUnit.SECONDS);
        }
    }

    private void checkBridgeOnline(FroniusBridgeConfiguration config) throws FroniusCommunicationException {
        FroniusHttpUtil.executeUrl("http://" + config.hostname, 5000);
    }
}
