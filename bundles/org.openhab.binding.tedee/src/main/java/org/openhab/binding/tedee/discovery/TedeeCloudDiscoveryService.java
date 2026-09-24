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
import org.openhab.binding.tedee.internal.api.TedeeCloudApi;
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
 * 
 * Configuration for the Tedee Cloud Discovery Service.
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

    public TedeeCloudDiscoveryService() {
        super(TedeeCloudBridgeHandler.class, Set.of(TedeeBindingConstants.LOCK), SCAN_TIMEOUT_SECONDS, true);
    }

    @Override
    public void initialize() {
        bridgeUID = thingHandler.getThing().getUID();
        super.initialize();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::discoverLocks, 0, DISCOVERY_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> currentJob = discoveryJob;

        if (currentJob != null) {
            currentJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public void startScan() {
        discoverLocks();
    }

    private synchronized void discoverLocks() {
        ThingUID currentBridgeUID = bridgeUID;

        if (currentBridgeUID == null) {
            return;
        }

        if (!(thingHandler.getClient() instanceof TedeeCloudApi api)) {
            logger.debug("Tedee Cloud discovery skipped because API is not initialized");
            return;
        }

        try {
            List<TedeeLock> locks = api.getLocks();

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

                if (label == null || label.isBlank()) {
                    label = "Tedee Lock " + lock.id;
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

    private TedeeClientHolder getClient() {
        return new TedeeClientHolder(thingHandler.getClient() instanceof TedeeCloudApi cloudApi ? cloudApi : null);
    }

    private static class TedeeClientHolder {
        private final @Nullable TedeeCloudApi api;

        TedeeClientHolder(@Nullable TedeeCloudApi api) {
            this.api = api;
        }
    }

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
