/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.discovery;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.NodeId;
import org.openhab.binding.hive.internal.client.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of {@link HiveDiscoveryService}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class DefaultHiveDiscoveryService extends AbstractDiscoveryService implements HiveDiscoveryService {
    /**
     * Number of seconds before discovery should timeout.
     *
     * <p>
     *     N.B. Set to 0 because I want to manually mark when discovery is
     *     finished.
     * </p>
     */
    private static final int DISCOVERY_TIMEOUT_SECONDS = 0;

    private final Logger logger = LoggerFactory.getLogger(DefaultHiveDiscoveryService.class);

    private final @Nullable Phaser testingPhaser;

    /**
     * The {@linkplain ThingUID} of the
     * {@linkplain org.eclipse.smarthome.core.thing.Bridge} this discovery
     * service belongs to.
     */
    private final ThingUID bridgeUid;

    /**
     * The collection of currently known {@linkplain Node}s from the Hive
     * API.
     */
    private final AtomicReference<Map<NodeId, Node>> lastKnownNodes = new AtomicReference<>(Collections.emptyMap());

    /**
     * Create a new {@linkplain DefaultHiveDiscoveryService}.
     *
     * @param bridgeUid
     *      The {@linkplain ThingUID} of the bridge that owns this discovery
     *      service.
     */
    public DefaultHiveDiscoveryService(final ThingUID bridgeUid) {
        this(bridgeUid, null);
    }

    DefaultHiveDiscoveryService(
            final ThingUID bridgeUid,
            final @Nullable Phaser testingPhaser
    ) {
        super(
                HiveBindingConstants.DISCOVERABLE_THING_TYPES_UIDS,
                DISCOVERY_TIMEOUT_SECONDS
        );

        Objects.requireNonNull(bridgeUid);

        this.testingPhaser = testingPhaser;
        this.bridgeUid = bridgeUid;

        if (testingPhaser != null) {
            testingPhaser.register();
        }
    }

    @Override
    public void updateKnownNodes(final Set<Node> knownNodes) {
        Objects.requireNonNull(knownNodes);

        this.scheduler.execute(() -> {
            // Create a map from NodeId -> Node to help with following links later.
            final Map<NodeId, Node> nodeMap = Collections.unmodifiableMap(
                    knownNodes.stream().collect(Collectors.toMap(Node::getId, Function.identity()))
            );
            this.lastKnownNodes.set(nodeMap);

            // Trigger a new scan with the updated information.
            this.startScan(null);

            final @Nullable Phaser testingPhaser = this.testingPhaser;
            if (testingPhaser != null) {
                testingPhaser.arriveAndAwaitAdvance();
            }
        });
    }

    @Override
    protected void startScan() {
        // Get a local copy of nodes to prevent concurrency problems
        final @Nullable Map<NodeId, Node> nodes = this.lastKnownNodes.get();
        // I never allow lastKnownNodes to be set to null.
        assert nodes != null;

        // Go through the set of nodes and report their discovery.
        for (final Node node : nodes.values()) {
            // Convert Hive API ProductTypes into ThingTypes.
            final ThingTypeUID thingTypeUID;
            if (node.getProductType().equals(ProductType.BOILER_MODULE)) {
                thingTypeUID = HiveBindingConstants.THING_TYPE_BOILER_MODULE;
            } else if (node.getProductType().equals(ProductType.HEATING)) {
                thingTypeUID = HiveBindingConstants.THING_TYPE_HEATING;
            } else if (node.getProductType().equals(ProductType.HOT_WATER)) {
                thingTypeUID = HiveBindingConstants.THING_TYPE_HOT_WATER;
            } else if (node.getProductType().equals(ProductType.HUB)) {
                thingTypeUID = HiveBindingConstants.THING_TYPE_HUB;
            } else if (node.getProductType().equals(ProductType.THERMOSTAT_UI)) {
                thingTypeUID = HiveBindingConstants.THING_TYPE_THERMOSTAT;
            } else if (node.getProductType().equals(ProductType.TRV)) {
                thingTypeUID = HiveBindingConstants.THING_TYPE_TRV;
            } else if (node.getProductType().equals(ProductType.TRV_GROUP)) {
                thingTypeUID = HiveBindingConstants.THING_TYPE_TRV_GROUP;
            } else if (node.getProductType().equals(ProductType.ACTIONS)
                    || node.getProductType().equals(ProductType.DAYLIGHT_SD)
                    || node.getProductType().equals(ProductType.UNKNOWN)
            ) {
                // We do not care about these so skip.
                continue;
            } else {
                logger.trace("Found a node that I cannot handle: {} - {} ({})", node.getProductType(), node.getName(), node.getId());

                // We do not have a thing type for this yet so skip.
                continue;
            }

            final ThingUID thingUID = new ThingUID(thingTypeUID, this.bridgeUid, node.getId().toString());

            // Do some fiddling with node names to get more descriptive
            // thing labels.
            final String label;
            if (node.getProductType().equals(ProductType.BOILER_MODULE)) {
                label = node.getName().toString() + " (Boiler Module)";
            } else if (node.getProductType().equals(ProductType.HEATING)) {
                // If node is heating node use the parent name because
                // the real name is just a generic Thermostat X.
                final @Nullable Node parentNode = nodes.get(node.getParentNodeId());

                if (parentNode != null) {
                    label = parentNode.getName().toString() + " (Thermostat Heating Zone)";
                } else {
                    logger.warn("Could not find parent node with id {}", node.getParentNodeId());
                    label = node.getName().toString();
                }
            } else if (node.getProductType().equals(ProductType.HOT_WATER)) {
                label = "Hot Water";
            } else if (node.getProductType().equals(ProductType.THERMOSTAT_UI)) {
                label = node.getName().toString() + " (Thermostat)";
            } else if (node.getProductType().equals(ProductType.TRV)) {
                label = node.getName().toString() + " (Radiator Valve)";
            } else if (node.getProductType().equals(ProductType.TRV_GROUP)) {
                label = node.getName().toString() + " (Radiator Valve Heating Zone)";
            } else {
                label = node.getName().toString();
            }

            final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withThingType(thingTypeUID)
                    .withBridge(this.bridgeUid)
                    .withLabel(label)
                    .withProperty(HiveBindingConstants.CONFIG_NODE_ID, node.getId().toString())
                    .withRepresentationProperty(HiveBindingConstants.CONFIG_NODE_ID)
                    .build();

            thingDiscovered(discoveryResult);
        }

        stopScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deactivate() {
        super.deactivate();

        // Remove Things from the inbox were discovered by this service.
        removeOlderResults(Instant.now().toEpochMilli(), this.bridgeUid);
    }
}
