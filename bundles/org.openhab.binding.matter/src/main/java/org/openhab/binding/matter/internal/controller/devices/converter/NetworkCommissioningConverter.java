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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.NetworkCommissioningCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

import com.google.gson.Gson;

/**
 * A converter for translating {@link NetworkCommissioningCluster} events and attributes to openHAB channels and
 * back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class NetworkCommissioningConverter extends GenericConverter<NetworkCommissioningCluster> {
    private Gson gson = new Gson();

    public NetworkCommissioningConverter(NetworkCommissioningCluster cluster, MatterBaseThingHandler handler,
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
            case NetworkCommissioningCluster.ATTRIBUTE_MAX_NETWORKS:
            case NetworkCommissioningCluster.ATTRIBUTE_SCAN_MAX_TIME_SECONDS:
            case NetworkCommissioningCluster.ATTRIBUTE_CONNECT_MAX_TIME_SECONDS:
            case NetworkCommissioningCluster.ATTRIBUTE_INTERFACE_ENABLED:
            case NetworkCommissioningCluster.ATTRIBUTE_LAST_NETWORKING_STATUS:
            case NetworkCommissioningCluster.ATTRIBUTE_LAST_NETWORK_ID:
            case NetworkCommissioningCluster.ATTRIBUTE_LAST_CONNECT_ERROR_VALUE:
            case NetworkCommissioningCluster.ATTRIBUTE_THREAD_VERSION:
                updateThingAttributeProperty(message.path.attributeName, message.value);
                break;
            case NetworkCommissioningCluster.ATTRIBUTE_NETWORKS:
            case NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_WI_FI_BANDS:
            case NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_THREAD_FEATURES:
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

    private void updateThingProperties(NetworkCommissioningCluster cluster) {
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_MAX_NETWORKS, cluster.maxNetworks);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_NETWORKS,
                cluster.networks != null ? gson.toJson(cluster.networks) : null);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_SCAN_MAX_TIME_SECONDS,
                cluster.scanMaxTimeSeconds);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_CONNECT_MAX_TIME_SECONDS,
                cluster.connectMaxTimeSeconds);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_INTERFACE_ENABLED, cluster.interfaceEnabled);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_LAST_NETWORKING_STATUS,
                cluster.lastNetworkingStatus);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_LAST_NETWORK_ID, cluster.lastNetworkId);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_LAST_CONNECT_ERROR_VALUE,
                cluster.lastConnectErrorValue);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_WI_FI_BANDS,
                cluster.supportedWiFiBands != null ? gson.toJson(cluster.supportedWiFiBands) : null);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_THREAD_FEATURES,
                cluster.supportedThreadFeatures != null ? gson.toJson(cluster.supportedThreadFeatures) : null);
        updateThingAttributeProperty(NetworkCommissioningCluster.ATTRIBUTE_THREAD_VERSION, cluster.threadVersion);
    }
}
