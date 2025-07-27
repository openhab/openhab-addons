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
package org.openhab.binding.matter.internal.controller.devices.converter;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

import com.google.gson.Gson;

/**
 * A converter for translating {@link ThreadNetworkDiagnosticsCluster} events and attributes to openHAB channels and
 * back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ThreadNetworkDiagnosticsConverter extends GenericConverter<ThreadNetworkDiagnosticsCluster> {
    private Gson gson = new Gson();

    public ThreadNetworkDiagnosticsConverter(ThreadNetworkDiagnosticsCluster cluster, MatterBaseThingHandler handler,
            int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public void pollCluster() {
        // read the whole cluster
        handler.readCluster(ThreadNetworkDiagnosticsCluster.class, endpointNumber, initializingCluster.id)
                .thenAccept(cluster -> {
                    updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL, cluster.channel);
                    updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE,
                            cluster.routingRole);
                    updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME,
                            cluster.networkName);
                    updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID, cluster.panId);
                    updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID,
                            cluster.extendedPanId);
                    updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16, cluster.rloc16);
                    updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE,
                            gson.toJson(cluster.neighborTable));
                    updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE,
                            gson.toJson(cluster.routeTable));
                }).exceptionally(e -> {
                    logger.debug("Error polling thread network diagnostics", e);
                    return null;
                });
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        return Collections.emptyMap();
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        updateThingAttributeProperty(message.path.attributeName, message.value);
        super.onEvent(message);
    }

    @Override
    public void initState() {
        logger.debug("initState");
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL, initializingCluster.channel);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE,
                initializingCluster.routingRole);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME,
                initializingCluster.networkName);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID, initializingCluster.panId);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID,
                initializingCluster.extendedPanId);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16, initializingCluster.rloc16);
        String neighborTable = initializingCluster.neighborTable != null
                ? gson.toJson(initializingCluster.neighborTable)
                : null;
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE, neighborTable);
    }
}
