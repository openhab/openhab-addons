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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_WIFINETWORKDIAGNOSTICS_RSSI;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_WIFINETWORKDIAGNOSTICS_RSSI;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.WiFiNetworkDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link WiFiNetworkDiagnosticsCluster} events and attributes to openHAB channels and back
 * again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class WiFiNetworkDiagnosticsConverter extends GenericConverter<WiFiNetworkDiagnosticsCluster> {

    public WiFiNetworkDiagnosticsConverter(WiFiNetworkDiagnosticsCluster cluster, MatterBaseThingHandler handler,
            int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public void pollCluster() {
        // we only need to read a single attribute, not the whole cluster
        handler.readAttribute(endpointNumber, initializingCluster.name, WiFiNetworkDiagnosticsCluster.ATTRIBUTE_RSSI)
                .thenAccept(rssi -> {
                    updateState(CHANNEL_ID_WIFINETWORKDIAGNOSTICS_RSSI,
                            new QuantityType<>(Integer.parseInt(rssi), Units.DECIBEL_MILLIWATTS));
                    updateThingAttributeProperty(WiFiNetworkDiagnosticsCluster.ATTRIBUTE_RSSI, rssi);
                }).exceptionally(e -> {
                    logger.debug("Error polling wifi network diagnostics", e);
                    return null;
                });
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_WIFINETWORKDIAGNOSTICS_RSSI), "Number:Power")
                .withType(CHANNEL_WIFINETWORKDIAGNOSTICS_RSSI).build();
        return Collections.singletonMap(channel, null);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case WiFiNetworkDiagnosticsCluster.ATTRIBUTE_RSSI:
                if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_WIFINETWORKDIAGNOSTICS_RSSI,
                            new QuantityType<>(number, Units.DECIBEL_MILLIWATTS));
                    updateThingAttributeProperty(WiFiNetworkDiagnosticsCluster.ATTRIBUTE_RSSI, number);
                }
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_WIFINETWORKDIAGNOSTICS_RSSI,
                initializingCluster.rssi != null
                        ? new QuantityType<>(initializingCluster.rssi, Units.DECIBEL_MILLIWATTS)
                        : UnDefType.NULL);
        updateThingAttributeProperty(WiFiNetworkDiagnosticsCluster.ATTRIBUTE_RSSI, initializingCluster.rssi);
    }
}
