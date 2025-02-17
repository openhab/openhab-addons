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

package org.openhab.binding.mqtt.awtrixlight.internal.discovery;

import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.Helper;
import org.openhab.binding.mqtt.discovery.AbstractMQTTDiscovery;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AwtrixLightBridgeDiscoveryService} is responsible for finding awtrix
 * clocks and setting them up for the handlers.
 *
 * @author Thomas Lauterbach - Initial contribution
 */

@Component(service = DiscoveryService.class, configurationPid = "discovery.awtrixlight")
@NonNullByDefault
public class AwtrixLightDiscoveryService extends AbstractMQTTDiscovery {
    protected final MQTTTopicDiscoveryService discoveryService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Activate
    public AwtrixLightDiscoveryService(@Reference MQTTTopicDiscoveryService discoveryService) {
        super(Set.of(THING_TYPE_BRIDGE), 3, true, TOPIC_BASE + "/+" + TOPIC_STATS);
        this.discoveryService = discoveryService;
    }

    @Override
    public void receivedMessage(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
            byte[] payload) {
        resetTimeout();
        String message = new String(payload, StandardCharsets.UTF_8);
        if (topic.endsWith(TOPIC_STATS)) {
            String baseTopic = topic.replace(TOPIC_STATS, "");
            Map<String, Object> messageParams = Helper.decodeStatsJson(message);
            String vendorString = "Unknown";
            @Nullable
            Object vendor = messageParams.get(FIELD_BRIDGE_TYPE);
            if (vendor instanceof Integer) {
                vendorString = vendor.equals(0) ? "Ulanzi" : "Generic";
            }
            String firmwareString = "Unknown";
            @Nullable
            Object firmware = messageParams.get(FIELD_BRIDGE_FIRMWARE);
            if (firmware instanceof String) {
                firmwareString = (String) firmware;
            }
            String hardwareUidString = "Unknown";
            @Nullable
            Object hardwareUid = messageParams.get(FIELD_BRIDGE_UID);
            if (hardwareUid instanceof String) {
                hardwareUidString = (String) hardwareUid;
            }
            logger.trace("Publishing an Awtrix Clock with ID :{}", hardwareUidString);
            publishClock(connectionBridge, baseTopic, vendorString, firmwareString, hardwareUidString);
        }
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic) {
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected MQTTTopicDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    void publishClock(ThingUID connectionBridgeUid, String baseTopic, String vendor, String firmware,
            String hardwareUid) {
        String name = baseTopic.replace(TOPIC_BASE + "/", "");
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_BRIDGE, connectionBridgeUid, hardwareUid))
                .withBridge(connectionBridgeUid).withProperty(PROP_VENDOR, vendor).withProperty(PROP_FIRMWARE, firmware)
                .withProperty(PROP_UNIQUEID, hardwareUid).withProperty(PROP_BASETOPIC, baseTopic)
                .withRepresentationProperty(PROP_UNIQUEID).withLabel("Awtrix Clock " + name).build());
    }
}
