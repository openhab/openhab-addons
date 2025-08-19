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
package org.openhab.binding.elroconnects.internal.discovery;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsConnector;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ElroConnectsBridgeDiscoveryService} is used to discover K1 Hubs attached to an ELRO Connects cloud account.
 *
 * @author Mark Herwege - Initial Contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ElroConnectsBridgeDiscoveryService.class)
@NonNullByDefault
public class ElroConnectsBridgeDiscoveryService
        extends AbstractThingHandlerDiscoveryService<ElroConnectsAccountHandler> {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsBridgeDiscoveryService.class);

    private volatile @Nullable ScheduledFuture<?> discoveryJob;

    private static final int TIMEOUT_S = 5;
    private static final int INITIAL_DELAY_S = 5; // initial delay for polling to allow time for login and retrieval in
                                                  // ElroConnectsAccountHandler to complete
    private static final int REFRESH_INTERVAL_S = 60;

    public ElroConnectsBridgeDiscoveryService() {
        super(ElroConnectsAccountHandler.class, ElroConnectsBindingConstants.SUPPORTED_CONNECTOR_TYPES_UIDS, TIMEOUT_S);
        logger.debug("Bridge discovery service started");
    }

    @Override
    protected void startScan() {
        scheduler.execute(this::discoverConnectors); // If background account polling is not enabled for the handler,
                                                     // this will trigger http requests, therefore do in separate thread
                                                     // to be able to return quickly
    }

    private void discoverConnectors() {
        logger.debug("Starting hub discovery scan");

        ThingUID bridgeUID = thingHandler.getThing().getUID();

        Map<String, ElroConnectsConnector> connectors = thingHandler.getDevices();
        if (connectors == null) {
            return;
        }

        connectors.entrySet().forEach(c -> {
            if (c.getValue().online) {
                String connectorId = c.getKey();
                String firmwareVersion = c.getValue().binVersion;
                boolean legacy = false;
                try {
                    legacy = Integer.valueOf(firmwareVersion.substring(firmwareVersion.lastIndexOf(".") + 1)) <= 14;
                } catch (NumberFormatException e) {
                    // Assume new firmware if we cannot decode firmwareVersion
                    logger.debug("Cannot get firmware version from {}, assume new firmware", firmwareVersion);
                }
                final Map<String, Object> properties = new HashMap<>();
                properties.put(CONFIG_CONNECTOR_ID, connectorId);
                properties.put(CONFIG_LEGACY_FIRMWARE, legacy);
                properties.put("binVersion", c.getValue().binVersion);
                properties.put("binType", c.getValue().binType);
                properties.put("sdkVer", c.getValue().sdkVer);
                properties.put(Thing.PROPERTY_MODEL_ID, c.getValue().model);
                properties.put("desc", c.getValue().desc);

                thingDiscovered(
                        DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_CONNECTOR, bridgeUID, connectorId))
                                .withLabel(c.getValue().desc).withBridge(bridgeUID).withProperties(properties)
                                .withRepresentationProperty(CONFIG_CONNECTOR_ID).build());
            }
        });
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void startBackgroundDiscovery() {
        logger.debug("Start bridge background discovery");
        ScheduledFuture<?> job = discoveryJob;
        if (job == null || job.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::discoverConnectors, INITIAL_DELAY_S,
                    REFRESH_INTERVAL_S, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopBackgroundDiscovery() {
        logger.debug("Stop bridge background discovery");
        ScheduledFuture<?> job = discoveryJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now());
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }
}
