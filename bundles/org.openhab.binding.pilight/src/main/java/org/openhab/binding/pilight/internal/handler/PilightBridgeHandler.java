/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pilight.internal.IPilightCallback;
import org.openhab.binding.pilight.internal.PilightBridgeConfiguration;
import org.openhab.binding.pilight.internal.PilightConnector;
import org.openhab.binding.pilight.internal.discovery.PilightDeviceDiscoveryService;
import org.openhab.binding.pilight.internal.dto.Action;
import org.openhab.binding.pilight.internal.dto.Config;
import org.openhab.binding.pilight.internal.dto.DeviceType;
import org.openhab.binding.pilight.internal.dto.Status;
import org.openhab.binding.pilight.internal.dto.Version;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PilightBridgeHandler} is responsible dispatching commands for the child
 * things to the Pilight daemon and sending status updates to the child things.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
public class PilightBridgeHandler extends BaseBridgeHandler {

    private static final int REFRESH_CONFIG_MSEC = 500;

    private final Logger logger = LoggerFactory.getLogger(PilightBridgeHandler.class);

    private @Nullable PilightConnector connector = null;

    private @Nullable ScheduledFuture<?> refreshJob = null;

    private @Nullable PilightDeviceDiscoveryService discoveryService = null;

    private @Nullable ExecutorService connectorExecutor = null;

    public PilightBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Pilight Bridge is read-only and does not handle commands.");
    }

    @Override
    public void initialize() {
        PilightBridgeConfiguration pilightConfig = getConfigAs(PilightBridgeConfiguration.class);

        final @Nullable PilightDeviceDiscoveryService discoveryService = this.discoveryService;
        PilightConnector connector = new PilightConnector(pilightConfig, new IPilightCallback() {
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

                if (discoveryService != null) {
                    discoveryService.setStatus(allStatus);
                }
            }

            @Override
            public void configReceived(Config config) {
                processConfig(config);
            }

            @Override
            public void versionReceived(Version version) {
                getThing().setProperty(Thing.PROPERTY_FIRMWARE_VERSION, version.getVersion());
            }
        }, scheduler);

        updateStatus(ThingStatus.UNKNOWN);

        ExecutorService connectorExecutor = Executors
                .newSingleThreadExecutor(new NamedThreadFactory(getThing().getUID().getAsString(), true));
        connectorExecutor.execute(connector);
        this.connectorExecutor = connectorExecutor;
        this.connector = connector;
    }

    @Override
    public void dispose() {
        final @Nullable ScheduledFuture<?> future = this.refreshJob;
        if (future != null) {
            future.cancel(true);
        }

        final @Nullable PilightConnector connector = this.connector;
        if (connector != null) {
            connector.close();
            this.connector = null;
        }

        final @Nullable ExecutorService connectorExecutor = this.connectorExecutor;
        if (connectorExecutor != null) {
            connectorExecutor.shutdown();
            this.connectorExecutor = null;
        }
    }

    /**
     * Is background discovery for this bridge enabled?
     *
     * @return background discovery
     */
    public boolean isBackgroundDiscoveryEnabled() {
        return getConfigAs(PilightBridgeConfiguration.class).getBackgroundDiscovery();
    }

    /**
     * send action to pilight daemon
     *
     * @param action action to send
     */
    public void sendAction(Action action) {
        final @Nullable PilightConnector connector = this.connector;
        if (connector != null) {
            connector.sendAction(action);
        }
    }

    /**
     * refresh config and status by requesting config and all values from pilight daemon
     */
    public synchronized void refreshConfigAndStatus() {
        if (thing.getStatus() == ThingStatus.ONLINE) {
            final @Nullable ScheduledFuture<?> refreshJob = this.refreshJob;
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
        final @Nullable PilightConnector connector = this.connector;
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
                final @Nullable ThingHandler handler = thing.getHandler();
                if (handler instanceof PilightBaseHandler baseHandler) {
                    baseHandler.updateFromStatusIfMatches(status);
                }
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(PilightDeviceDiscoveryService.class);
    }

    /**
     * Register discovery service to this bridge instance.
     */
    public boolean registerDiscoveryListener(PilightDeviceDiscoveryService listener) {
        if (discoveryService == null) {
            discoveryService = listener;
            return true;
        }
        return false;
    }

    /**
     * Unregister discovery service from this bridge instance.
     */
    public boolean unregisterDiscoveryListener() {
        if (discoveryService != null) {
            discoveryService = null;
            return true;
        }

        return false;
    }

    /**
     * Processes a config received from pilight
     *
     * @param config The new config
     */
    private void processConfig(Config config) {
        for (Thing thing : getThing().getThings()) {
            final @Nullable ThingHandler handler = thing.getHandler();
            if (handler instanceof PilightBaseHandler baseHandler) {
                baseHandler.updateFromConfigIfMatches(config);
            }
        }

        final @Nullable PilightDeviceDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.setConfig(config);
        }
    }
}
