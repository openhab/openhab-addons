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
package org.openhab.binding.matter.internal.controller.devices.converter;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster.RoutingRoleEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

/**
 * A converter for translating {@link ThreadNetworkDiagnosticsCluster} events and attributes to openHAB channels and
 * back again.
 *
 * Thread extended addresses are 64-bit values, which JavaScript clients cannot parse as JSON numbers without losing
 * precision, so they are written to properties as 16 digit hex strings.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ThreadNetworkDiagnosticsConverter extends GenericConverter<ThreadNetworkDiagnosticsCluster> {
    private static final Gson THREAD_GSON = new GsonBuilder()
            .registerTypeAdapter(BigInteger.class,
                    (JsonSerializer<BigInteger>) (src, type, context) -> new JsonPrimitive(toExtAddressHex(src)))
            .create();

    private static final Map<String, Function<ThreadNetworkDiagnosticsCluster, @Nullable Object>> POLLED_ATTRIBUTES = Map
            .of(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE, c -> c.routingRole,
                    ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME, c -> c.networkName,
                    ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID, c -> c.extendedPanId,
                    ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PARTITION_ID, c -> c.partitionId,
                    ThreadNetworkDiagnosticsCluster.ATTRIBUTE_LEADER_ROUTER_ID, c -> c.leaderRouterId,
                    ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE, c -> c.neighborTable,
                    ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE, c -> c.routeTable,
                    ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16, c -> c.rloc16,
                    ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS, c -> c.extAddress);
    // Only read when the device reported them initially, they are new in Matter 1.4
    private static final Set<String> OPTIONAL_ATTRIBUTES = Set.of(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16,
            ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS);

    private final AtomicBoolean pollInProgress = new AtomicBoolean();
    private volatile @Nullable RoutingRoleEnum routingRole;

    public ThreadNetworkDiagnosticsConverter(ThreadNetworkDiagnosticsCluster cluster, MatterBaseThingHandler handler,
            int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
        routingRole = cluster.routingRole;
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        return Collections.emptyMap();
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        updateAttribute(message.path.attributeName, message.value);
        super.onEvent(message);
    }

    @Override
    public void initState() {
        logger.debug("initState");
        ThreadNetworkDiagnosticsCluster cluster = initializingCluster;
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL, cluster.channel);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE, cluster.routingRole);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME, cluster.networkName);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID, cluster.panId);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID, cluster.extendedPanId);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16, cluster.rloc16);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PARTITION_ID, cluster.partitionId);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_LEADER_ROUTER_ID, cluster.leaderRouterId);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE, cluster.neighborTable);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE, cluster.routeTable);
        updateAttribute(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS, cluster.extAddress);
    }

    /**
     * Devices rarely report Thread topology changes on their own, so read them from the device in one request.
     * Sleepy end devices are skipped, as their tables only hold their parent and reading them wakes the device.
     * A reply can overwrite a newer subscription report that arrived while it was in flight, the next report or poll
     * corrects it.
     */
    @Override
    public void pollCluster() {
        if (routingRole == RoutingRoleEnum.SLEEPY_END_DEVICE || !pollInProgress.compareAndSet(false, true)) {
            return;
        }
        List<String> attributeNames = POLLED_ATTRIBUTES.keySet().stream()
                .filter(name -> !OPTIONAL_ATTRIBUTES.contains(name) || wasReported(name)).toList();
        handler.readAttributes(ThreadNetworkDiagnosticsCluster.class, endpointNumber,
                ThreadNetworkDiagnosticsCluster.CLUSTER_NAME, attributeNames).thenAccept(cluster -> {
                    for (String name : attributeNames) {
                        // Attributes missing from the reply keep their last known value
                        Object value = POLLED_ATTRIBUTES.get(name).apply(cluster);
                        if (value != null) {
                            updateAttribute(name, value);
                        }
                    }
                }).exceptionally(e -> {
                    logger.debug("Error polling Thread network diagnostics: {}", e.getMessage());
                    return null;
                }).whenComplete((result, e) -> pollInProgress.set(false));
    }

    private boolean wasReported(String attributeName) {
        return switch (attributeName) {
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16 -> initializingCluster.rloc16 != null;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS -> initializingCluster.extAddress != null;
            default -> true;
        };
    }

    private void updateAttribute(String attributeName, @Nullable Object value) {
        switch (attributeName) {
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE:
                routingRole = value instanceof RoutingRoleEnum role ? role : null;
                updateThingAttributeProperty(attributeName, value);
                break;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PARTITION_ID:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_LEADER_ROUTER_ID:
                updateThingAttributeProperty(attributeName, value);
                break;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE:
                updateThingAttributeProperty(attributeName, value != null ? THREAD_GSON.toJson(value) : null);
                break;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS:
                updateThingAttributeProperty(attributeName,
                        value instanceof BigInteger extAddress ? toExtAddressHex(extAddress) : null);
                break;
        }
    }

    static String toExtAddressHex(BigInteger extAddress) {
        return String.format("%016X", extAddress);
    }
}
