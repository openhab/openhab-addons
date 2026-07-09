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
package org.openhab.binding.tuya.internal;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_DEVICE;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.cloud.TuyaOpenAPI;
import org.openhab.binding.tuya.internal.handler.ProjectHandler;
import org.openhab.binding.tuya.internal.local.UdpDiscoverySender;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TuyaDiscoveryService} implements the discovery service for Tuya devices from the cloud
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TuyaDiscoveryService.class)
@NonNullByDefault
public class TuyaDiscoveryService extends AbstractThingHandlerDiscoveryService<ProjectHandler> {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_TUYA_DEVICE);
    private static final int SEARCH_TIME = 5;

    private final Logger logger = LoggerFactory.getLogger(TuyaDiscoveryService.class);

    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable ScheduledFuture<?> broadcastJob;

    private final UdpDiscoverySender udpDiscoverySender = new UdpDiscoverySender();

    public TuyaDiscoveryService() {
        super(ProjectHandler.class, SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Override
    public void initialize() {
        ((ProjectHandler) thingHandler).setDiscoveryService(this);

        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();

        removeOlderResults(Instant.now());

        ((ProjectHandler) thingHandler).setDiscoveryService(null);
    }

    @Override
    public void startScan() {
        startScan(false);
    }

    public void startScan(boolean allSchemas) {
        TuyaOpenAPI api = thingHandler.getApi();
        if (!api.isConnected()) {
            logger.trace("Tried to start scan but API for bridge '{}' is not connected.",
                    thingHandler.getThing().getUID());
            return;
        }

        api.getAllDevices(allSchemas, (discoveryResult) -> thingDiscovered(discoveryResult));
    }

    @Override
    protected synchronized void stopScan() {
        ScheduledFuture<?> broadcastJob = this.broadcastJob;
        if (broadcastJob != null) {
            broadcastJob.cancel(true);
            this.broadcastJob = null;
        }
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startBackgroundDiscovery() {
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            this.discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 1, 5, TimeUnit.MINUTES);
        }

        ScheduledFuture<?> broadcastJob = this.broadcastJob;
        if (broadcastJob == null || broadcastJob.isDone() || broadcastJob.isCancelled()) {
            this.broadcastJob = scheduler.scheduleWithFixedDelay(udpDiscoverySender::sendMessage, 5, 10,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopBackgroundDiscovery() {
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
        ScheduledFuture<?> broadcastJob = this.broadcastJob;
        if (broadcastJob != null) {
            broadcastJob.cancel(true);
            this.broadcastJob = null;
        }
    }
}
