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

package org.openhab.binding.pilight.internal.handler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.pilight.internal.IPilightCallback;
import org.openhab.binding.pilight.internal.PilightBridgeConfiguration;
import org.openhab.binding.pilight.internal.PilightConnector;
import org.openhab.binding.pilight.internal.dto.Action;
import org.openhab.binding.pilight.internal.dto.Config;
import org.openhab.binding.pilight.internal.dto.DeviceType;
import org.openhab.binding.pilight.internal.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PilightBridgeHandler} is responsible dispatching commands for the child
 * things to the Pilight daemon and sending status updates to the child things.
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public class PilightBridgeHandler extends BaseBridgeHandler {

    private static final Integer REFRESH_CONFIG_MSEC = 500;

    private final Logger logger = LoggerFactory.getLogger(PilightBridgeHandler.class);

    private @Nullable PilightConnector connector = null;

    private @Nullable ScheduledFuture<?> refreshJob = null;

    public PilightBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Pilight Bridge is read-only and does not handle commands.");
    }

    @Override
    public void initialize() {
        PilightBridgeConfiguration config = getConfigAs(PilightBridgeConfiguration.class);

        PilightConnector connector = new PilightConnector(config, new IPilightCallback() {
            @Override
            public void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail,
                    @Nullable String description) {
                updateStatus(status, statusDetail, description);
                if (status == ThingStatus.ONLINE) {
                    refreshConfigAndStatus();
                }
            }

            @Override
            public void statusReceived(List<Status> allStatus) {
                for (Status status : allStatus) {
                    processStatus(status);
                }
            }

            @Override
            public void configReceived(Config config) {
                processConfig(config);
            }
        });

        updateStatus(ThingStatus.UNKNOWN);

        connector.setName("OH-binding-" + getThing().getUID().getAsString());
        connector.start();
        this.connector = connector;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> future = this.refreshJob;
        if (future != null) {
            future.cancel(true);
        }

        final PilightConnector connector = this.connector;
        if (connector != null) {
            connector.close();
            this.connector = null;
        }
    }

    /**
     * send action to pilight daemon
     *
     * @param action action to send
     */
    public void sendAction(Action action) {
        final PilightConnector connector = this.connector;
        if (connector != null) {
            connector.sendAction(action);
        }
    }

    /**
     * refresh config and status by requesting config and all values from pilight daemon
     */
    public synchronized void refreshConfigAndStatus() {
        if (thing.getStatus() == ThingStatus.ONLINE) {
            final ScheduledFuture<?> refreshJob = this.refreshJob;
            if (refreshJob == null || refreshJob.isCancelled() || refreshJob.isDone()) {
                logger.debug("schedule refresh of config and status");
                this.refreshJob = scheduler.schedule(this::doRefreshConfigAndStatus, REFRESH_CONFIG_MSEC,
                        TimeUnit.MILLISECONDS);
            }
        } else {
            logger.warn("Bridge is not online - ignoring refresh of config and status.");
        }
    }

    private void doRefreshConfigAndStatus() {
        logger.trace("do refresh config and status");
        final PilightConnector connector = this.connector;
        if (connector != null) {
            // the config is required for dimmers to get the minimum and maximum dim levels
            connector.refreshConfig();
            connector.refreshStatus();
        }
    }

    /**
     * Processes a status update received from pilight
     *
     * @param status The new Status
     */
    private void processStatus(Status status) {
        final Integer type = status.getType();
        logger.trace("processStatus device '{}' type {}", status.getDevices().get(0), type);

        if (!DeviceType.SERVER.equals(type)) {
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler != null && handler instanceof PilightBaseHandler) {
                    ((PilightBaseHandler) handler).updateFromStatusIfMatches(status);
                }
            }
        }
    }

    /**
     * Processes a config received from pilight
     *
     * @param config The new config
     */
    private void processConfig(Config config) {
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler != null && handler instanceof PilightBaseHandler) {
                ((PilightBaseHandler) handler).updateFromConfigIfMatches(config);
            }
        }
    }
}
