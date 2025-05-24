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

import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_TYPE_ENDPOINT;
import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_TYPE_NODE;

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
        return "@text/discovery.matter.scan-input.label";
    }

    @Override
    public @Nullable String getScanInputDescription() {
        return "@text/discovery.matter.scan-input.description";
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
        String label = ("@text/discovery.matter.bridge-endpoint.label : "
                + MatterLabelUtils.labelForBridgeEndpoint(root)).trim();
        discoverThing(thingUID, bridgeUID, root, root.number.toString(), "endpointId", label);
    }

    public void discoverNodeDevice(ThingUID thingUID, ThingUID bridgeUID, Node node) {
        String label = ("@text/discovery.matter.node-device.label : "
                + MatterLabelUtils.labelForNode(node.rootEndpoint)).trim();
        discoverThing(thingUID, bridgeUID, node.rootEndpoint, node.id.toString(), "nodeId", label);
    }

    public void discoverUnknownNodeDevice(ThingUID thingUID, ThingUID bridgeUID, BigInteger nodeId) {
        String label = ("@text/discovery.matter.unknown-node.label");
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperty("nodeId", nodeId.toString()).withRepresentationProperty("nodeId").withBridge(bridgeUID)
                .build();
        thingDiscovered(result);
    }

    private void discoverThing(ThingUID thingUID, ThingUID bridgeUID, Endpoint root, String id,
            String representationProperty, String label) {
        logger.debug("discoverThing: {} {} {}", thingUID, bridgeUID, id);

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperty(representationProperty, id).withRepresentationProperty(representationProperty)
                .withBridge(bridgeUID).build();
        thingDiscovered(result);
    }
}
