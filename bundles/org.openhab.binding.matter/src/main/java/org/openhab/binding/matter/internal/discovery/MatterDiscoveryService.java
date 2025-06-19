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
package org.openhab.binding.matter.internal.discovery;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.math.BigInteger;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.Endpoint;
import org.openhab.binding.matter.internal.client.dto.Node;
import org.openhab.binding.matter.internal.util.MatterLabelUtils;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MatterDiscoveryService} is the service that discovers Matter devices and endpoints.
 * 
 * If a code is provided, it will be used to discover a specific device and commission it to our Fabric
 * 
 * If no code is provided, it will scan for existing devices and endpoints and add them to the inbox for further
 * processing
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(MatterDiscoveryService.class);
    private @Nullable ThingHandler thingHandler;
    private @Nullable ScheduledFuture<?> discoveryJob;
    private static final int REFRESH_INTERVAL = 60 * 5;

    public MatterDiscoveryService() throws IllegalArgumentException {
        // set a 2 min timeout, which should be plenty of time to discover devices, but stopScan will be called when the
        // Matter client is done looking for new Nodes/Endpoints
        super(Set.of(THING_TYPE_NODE, THING_TYPE_ENDPOINT), REFRESH_INTERVAL);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        logger.debug("setThingHandler {}", handler);
        if (handler instanceof MatterDiscoveryHandler childDiscoveryHandler) {
            childDiscoveryHandler.setDiscoveryService(this);
            this.thingHandler = handler;
            this.i18nProvider = childDiscoveryHandler.getTranslationProvider();
            this.localeProvider = childDiscoveryHandler.getLocaleProvider();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        stopBackgroundDiscovery();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start background discovery");
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            this.discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, REFRESH_INTERVAL, REFRESH_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop background discovery");
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
    }

    @Override
    public @Nullable String getScanInputLabel() {
        return DISCOVERY_MATTER_SCAN_INPUT_LABEL;
    }

    @Override
    public @Nullable String getScanInputDescription() {
        return DISCOVERY_MATTER_SCAN_INPUT_DESCRIPTION;
    }

    @Override
    protected void startScan() {
        startScan("");
    }

    @Override
    public void startScan(String input) {
        ThingHandler handler = this.thingHandler;
        if (handler != null && handler instanceof MatterDiscoveryHandler childDiscoveryHandler) {
            childDiscoveryHandler.startScan(input.length() > 0 ? input : null).whenComplete((value, e) -> {
                logger.debug("startScan complete");
                stopScan();
            });
        }
    }

    public void discoverBridgeEndpoint(ThingUID thingUID, ThingUID bridgeUID, Endpoint root) {
        String label = String.format("%s [\"%s\"]", DISCOVERY_MATTER_BRIDGE_ENDPOINT_LABEL,
                MatterLabelUtils.labelForBridgeEndpoint(root)).trim();
        discoverThing(thingUID, bridgeUID, root, root.number.toString(), "endpointId", label);
    }

    public void discoverNodeDevice(ThingUID thingUID, ThingUID bridgeUID, Node node) {
        String label = String.format("%s [\"%s\"]", DISCOVERY_MATTER_NODE_DEVICE_LABEL,
                MatterLabelUtils.labelForNode(node.rootEndpoint)).trim();
        discoverThing(thingUID, bridgeUID, node.rootEndpoint, node.id.toString(), "nodeId", label);
    }

    public void discoverUnknownNodeDevice(ThingUID thingUID, ThingUID bridgeUID, BigInteger nodeId) {
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(DISCOVERY_MATTER_UNKNOWN_NODE_LABEL)
                .withProperty("nodeId", nodeId.toString()).withRepresentationProperty("nodeId").withBridge(bridgeUID)
                .build();
        thingDiscovered(result);
    }

    private void discoverThing(ThingUID thingUID, ThingUID bridgeUID, Endpoint root, String id,
            String representationProperty, String label) {
        logger.debug("discoverThing: {} {} {} {}", label, thingUID, bridgeUID, id);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperty(representationProperty, id).withRepresentationProperty(representationProperty)
                .withBridge(bridgeUID).build();
        thingDiscovered(result);
    }
}
