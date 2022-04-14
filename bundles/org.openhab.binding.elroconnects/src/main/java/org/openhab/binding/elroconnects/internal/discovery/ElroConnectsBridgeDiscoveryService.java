/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsConnector;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ElroConnectsBridgeDiscoveryService} is used to discover K1 Hubs attached to an ELRO Connects cloud account.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class ElroConnectsBridgeDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsBridgeDiscoveryService.class);

    private @Nullable ElroConnectsAccountHandler accountHandler;

    private volatile @Nullable ScheduledFuture<?> discoveryJob;

    private static final int TIMEOUT_S = 5;
    private static final int REFRESH_INTERVAL = 60;

    public ElroConnectsBridgeDiscoveryService() {
        super(ElroConnectsBindingConstants.SUPPORTED_CONNECTOR_TYPES_UIDS, TIMEOUT_S);
        logger.debug("Bridge discovery service started");
        super.activate(null); // Makes sure the background discovery for devices is enabled
    }

    @Override
    protected void startScan() {
        discoverConnectors();
    }

    private void discoverConnectors() {
        logger.debug("Starting hub discovery scan");
        ElroConnectsAccountHandler account = accountHandler;
        if (account == null) {
            return;
        }

        ThingUID bridgeUID = account.getThing().getUID();

        Map<String, ElroConnectsConnector> connectors = account.getDevices();
        if (connectors == null) {
            return;
        }

        connectors.entrySet().forEach(c -> {
            if (c.getValue().online) {
                String connectorId = c.getKey();
                final Map<String, Object> properties = new HashMap<>();
                properties.put(CONFIG_CONNECTOR_ID, connectorId);
                properties.put("binVersion", c.getValue().binVersion);
                properties.put("binType", c.getValue().binType);
                properties.put("sdkVer", c.getValue().sdkVer);
                properties.put("model", c.getValue().model);
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
    protected void startBackgroundDiscovery() {
        logger.debug("Start background bridge discovery");
        ScheduledFuture<?> job = discoveryJob;
        if (job == null || job.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::discoverConnectors, 0, REFRESH_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop bridge background discovery");
        ScheduledFuture<?> job = discoveryJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ElroConnectsAccountHandler) {
            accountHandler = (ElroConnectsAccountHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accountHandler;
    }
}
