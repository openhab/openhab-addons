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
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        return Collections.emptyMap();
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16:
                updateThingAttributeProperty(message.path.attributeName, message.value);
                break;
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE:
            case ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS:
                updateThingAttributeProperty(message.path.attributeName,
                        message.value != null ? gson.toJson(message.value) : null);
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        logger.debug("initState");
        updateThingProperties(initializingCluster);
    }

    private void updateThingProperties(ThreadNetworkDiagnosticsCluster cluster) {
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL, cluster.channel);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE, cluster.routingRole);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME, cluster.networkName);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID, cluster.panId);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID, cluster.extendedPanId);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16, cluster.rloc16);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE,
                cluster.neighborTable != null ? gson.toJson(cluster.neighborTable) : null);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE,
                cluster.routeTable != null ? gson.toJson(cluster.routeTable) : null);
        updateThingAttributeProperty(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS,
                cluster.extAddress != null ? gson.toJson(cluster.extAddress) : null);
    }
}
