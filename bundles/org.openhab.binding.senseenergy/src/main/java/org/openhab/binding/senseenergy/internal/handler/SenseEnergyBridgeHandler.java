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
package org.openhab.binding.senseenergy.internal.handler;

import static org.openhab.binding.senseenergy.internal.SenseEnergyBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyApi;
import org.openhab.binding.senseenergy.internal.api.SenseEnergyApiException;
import org.openhab.binding.senseenergy.internal.config.SenseEnergyBridgeConfiguration;
import org.openhab.binding.senseenergy.internal.discovery.SenseEnergyDiscoveryService;
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
 * {@link SenseEnergyBridgeHandler}
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
public class SenseEnergyBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SenseEnergyBridgeHandler.class);

    private @Nullable SenseEnergyDiscoveryService discoveryService;

    private SenseEnergyApi api;
    private SenseEnergyBridgeConfiguration config;

    protected @Nullable ScheduledFuture<?> heartbeatJob;

    private Set<Long> monitorIDs = Collections.emptySet();

    public SenseEnergyBridgeHandler(final Bridge thing, HttpClient httpClient) {
        super(thing);
        api = new SenseEnergyApi(httpClient);

        config = getConfigAs(SenseEnergyBridgeConfiguration.class);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SenseEnergyDiscoveryService.class);
    }

    public SenseEnergyApi getApi() {
        return api;
    }

    @Override
    public void initialize() {
        if (config.email.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "@text/offline.configuration-error.user-credentials-missing");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::goOnline);
    }

    public void goOnline() {
        try {
            this.monitorIDs = api.initialize(config.email, config.password);
        } catch (SenseEnergyApiException e) {
            handleApiException(e);
            return;
        }

        refreshMonitors();

        updateStatus(ThingStatus.ONLINE);

        this.heartbeatJob = scheduler.scheduleWithFixedDelay(this::heartbeat, 0, HEARTBEAT_MINUTES, TimeUnit.MINUTES);
    }

    private void heartbeat() {
        logger.trace("heartbeat");
        ThingStatus thingStatus = getThing().getStatus();

        if (thingStatus == ThingStatus.OFFLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
            goOnline(); // only attempt to goOnline if not a configuration error
            return;
        }

        if (thingStatus != ThingStatus.ONLINE) {
            return;
        }

        // token is verified on each api call, called here in case no API calls are made in the alloted period
        try {
            getApi().verifyToken();
        } catch (SenseEnergyApiException e) {
            handleApiException(e);
        }

        // call heartbeat for each thing to check health
        getThing().getThings().stream() //
                .map(t -> t.getHandler()) //
                .filter(h -> h instanceof SenseEnergyMonitorHandler) //
                .map(h -> (SenseEnergyMonitorHandler) h) //
                .forEach(h -> h.heartbeat());
    }

    public void handleApiException(Exception e) {
        if (e instanceof SenseEnergyApiException apiException) {
            switch (apiException.severity) {
                case TRANSIENT:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                    break;
                case CONFIG:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
                    break;
                case FATAL:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, e.getMessage());
                    break;
                case DATA:
                    logger.warn("Data exception: {}", e.toString());
                    break;
                default:
                    logger.warn("SenseEnergyApiException: {}", e.toString());
                    break;
            }
        } else {
            logger.warn("Unhandled Exception", e);
        }
    }

    /*
     * iterate through the monitor IDs (currently initialized when the api initializes) and checks if a thing is
     * already created. If not, will notify discovery service.
     */
    public void refreshMonitors() {
        SenseEnergyDiscoveryService localDiscoveryService = discoveryService;
        if (localDiscoveryService == null) {
            logger.warn("Discovery service is not initialized. Skipping monitor refresh.");
            return;
        }

        for (Long id : monitorIDs) {
            if (getMonitorHandler(id) == null) {
                logger.info("Found Sense Energy monitor with ID: {}", id);
                localDiscoveryService.notifyDiscoveryMonitor(id);
            }
        }
    }

    @Nullable
    public SenseEnergyMonitorHandler getMonitorHandler(long id) {
        return getThing().getThings().stream() //
                .filter(t -> t.getThingTypeUID().equals(MONITOR_THING_TYPE)) //
                .map(t -> (SenseEnergyMonitorHandler) t.getHandler()) //
                .filter(Objects::nonNull) //
                .filter(h -> h.getId() == id) //
                .findFirst() //
                .orElse(null); //
    }

    /*
     * rediscover the monitors again to add any back to inbox
     */
    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        refreshMonitors();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no channels associated with bridge
    }

    public boolean registerDiscoveryListener(SenseEnergyDiscoveryService listener) {
        if (discoveryService == null) {
            discoveryService = listener;
            return true;
        }

        return false;
    }

    public boolean unregisterDiscoveryListener() {
        if (discoveryService != null) {
            discoveryService = null;
            return true;
        }

        return false;
    }

    @Override
    public synchronized void dispose() {
        ScheduledFuture<?> localHeartbeatJob = this.heartbeatJob;
        if (localHeartbeatJob != null && !localHeartbeatJob.isCancelled()) {
            localHeartbeatJob.cancel(true);
        }
        this.heartbeatJob = null;
    }
}
