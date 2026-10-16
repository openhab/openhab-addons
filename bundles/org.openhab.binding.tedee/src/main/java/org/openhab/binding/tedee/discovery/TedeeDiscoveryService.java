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

package org.openhab.binding.tedee.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;
import org.openhab.binding.tedee.internal.api.TedeeApiException;
import org.openhab.binding.tedee.internal.api.TedeeLock;
import org.openhab.binding.tedee.internal.handler.TedeeBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers Tedee locks connected to a local Tedee Bridge and adds them as
 * child Things.
 *
 * @author Alex Goll - Initial contribution
 */

@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = TedeeDiscoveryService.class)
public class TedeeDiscoveryService extends AbstractThingHandlerDiscoveryService<TedeeBridgeHandler> {

    private static final int DISCOVERY_INTERVAL_SECONDS = 300;
    private static final int SCAN_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(TedeeDiscoveryService.class);

    private @Nullable ThingUID bridgeUID;
    private @Nullable ScheduledFuture<?> discoveryJob;

    /**
     * Creates a discovery service for locks connected to a local Tedee Bridge.
     */
    public TedeeDiscoveryService() {
        super(TedeeBridgeHandler.class, Set.of(TedeeBindingConstants.LOCK), SCAN_TIMEOUT_SECONDS, true);
    }

    /**
     * Initializes the discovery service and records the UID of the associated bridge.
     */
    @Override
    public void initialize() {
        bridgeUID = thingHandler.getThing().getUID();
        super.initialize();
    }

    /**
     * Starts periodic discovery of locks connected to the bridge.
     */
    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> currentJob = discoveryJob;

        if (currentJob == null || currentJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::discoverLocks, 0, DISCOVERY_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Stops periodic lock discovery and cancels the scheduled discovery job.
     */
    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> currentJob = discoveryJob;
        if (currentJob != null) {
            currentJob.cancel(true);
            discoveryJob = null;
        }
    }

    /**
     * Starts an immediate scan for locks connected to the bridge.
     */
    @Override
    public void startScan() {
        discoverLocks();
    }

    private synchronized void discoverLocks() {
        ThingUID currentBridgeUID = bridgeUID;
        if (currentBridgeUID == null) {
            return;
        }

        if (!thingHandler.ready()) {
            logger.debug("Tedee discovery skipped because bridge is not ready");
            return;
        }

        try {
            List<TedeeLock> locks = thingHandler.getLocks();

            logger.debug("Tedee discovery found {} lock(s) on bridge {}", locks.size(), currentBridgeUID);

            for (TedeeLock lock : locks) {
                if (lock.type != 2) {
                    logger.debug("Ignoring unsupported Tedee device type {} with id {}", lock.type, lock.id);
                    continue;
                }

                discoverLock(currentBridgeUID, lock);
            }
        } catch (TedeeApiException e) {
            logger.warn("Tedee lock discovery failed for bridge {}: {}", currentBridgeUID, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error during Tedee lock discovery for bridge {}", currentBridgeUID, e);
        }
    }

    private void discoverLock(ThingUID currentBridgeUID, TedeeLock lock) {
        ThingUID thingUID = new ThingUID(TedeeBindingConstants.LOCK, currentBridgeUID, "lock" + lock.id);

        Map<String, Object> properties = new HashMap<>();
        properties.put("deviceId", lock.id);
        properties.put("pollInterval", 300);

        String label = lock.name;
        if (label == null || label.isBlank()) {
            label = "Tedee Lock " + lock.id;
        }

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(currentBridgeUID)
                .withProperties(properties).withLabel(label).withRepresentationProperty("deviceId").build();

        thingDiscovered(result);

        logger.debug("Discovered Tedee lock {} '{}' as {}", lock.id, label, thingUID);
    }

    /**
     * Cancels the scheduled discovery job and releases the resources associated
     * with this service.
     */
    @Override
    public void dispose() {
        ScheduledFuture<?> currentJob = discoveryJob;
        if (currentJob != null) {
            currentJob.cancel(true);
            discoveryJob = null;
        }

        super.dispose();
    }
}
