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
import org.openhab.binding.tedee.internal.handler.TedeeCloudBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers Tedee locks accessible through a Tedee Cloud bridge and adds them
 * as child Things.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = TedeeCloudDiscoveryService.class)
public class TedeeCloudDiscoveryService extends AbstractThingHandlerDiscoveryService<TedeeCloudBridgeHandler> {

    private static final int DISCOVERY_INTERVAL_SECONDS = 600;
    private static final int SCAN_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(TedeeCloudDiscoveryService.class);

    private @Nullable ThingUID bridgeUID;
    private @Nullable ScheduledFuture<?> discoveryJob;

    /**
     * Creates a discovery service for locks accessible through a Tedee Cloud bridge.
     */
    public TedeeCloudDiscoveryService() {
        super(TedeeCloudBridgeHandler.class, Set.of(TedeeBindingConstants.LOCK), SCAN_TIMEOUT_SECONDS, true);
    }

    /**
     * Initializes discovery and records the UID of the associated cloud bridge.
     */
    @Override
    public void initialize() {
        bridgeUID = thingHandler.getThing().getUID();
        super.initialize();
    }

    /**
     * Starts periodic discovery of locks accessible through the cloud bridge.
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
     * Stops periodic discovery and cancels the scheduled discovery job.
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
     * Starts an immediate scan for locks accessible through the cloud bridge.
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
            logger.debug("Tedee Cloud discovery skipped because bridge is not ready");
            return;
        }

        try {
            List<TedeeLock> locks = thingHandler.getLocks();

            logger.debug("Tedee Cloud discovery found {} lock(s)", locks.size());

            for (TedeeLock lock : locks) {
                if (lock.type != 2) {
                    logger.debug("Ignoring unsupported Tedee Cloud device type {} with id {}", lock.type, lock.id);
                    continue;
                }

                ThingUID thingUID = new ThingUID(TedeeBindingConstants.LOCK, currentBridgeUID, "lock" + lock.id);

                Map<String, Object> properties = new HashMap<>();
                properties.put("deviceId", lock.id);
                properties.put("pollInterval", 300);

                String label = lock.name;

                if (label.isBlank()) {
                    label = "@text/discovery.lock.label [ \"%s\" ]".formatted(lock.id);
                }

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(currentBridgeUID)
                        .withProperties(properties).withLabel(label).withRepresentationProperty("deviceId").build();

                thingDiscovered(result);

                logger.debug("Discovered Tedee Cloud lock {} '{}' as {}", lock.id, label, thingUID);
            }
        } catch (TedeeApiException e) {
            logger.warn("Tedee Cloud discovery failed: {}", e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error during Tedee Cloud discovery", e);
        }
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
