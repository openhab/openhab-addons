/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homie.internal.discovery;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.discovery.AbstractMQTTDiscovery;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.homie.generic.internal.MqttBindingConstants;
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
 * The {@link Homie300Discovery} is responsible for discovering device nodes that follow the
 * Homie 3.x convention (https://github.com/homieiot/convention).
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.mqtthomie")
@NonNullByDefault
public class Homie300Discovery extends AbstractMQTTDiscovery {
    private final Logger logger = LoggerFactory.getLogger(Homie300Discovery.class);

    protected final MQTTTopicDiscoveryService discoveryService;

    @Activate
    public Homie300Discovery(@Reference MQTTTopicDiscoveryService discoveryService) {
        super(Collections.singleton(MqttBindingConstants.HOMIE300_MQTT_THING), 3, true, "+/+/$homie");
        this.discoveryService = discoveryService;
    }

    @Override
    protected MQTTTopicDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    /**
     * @param topic A topic like "homie/mydevice/$homie"
     * @return Returns the "mydevice" part of the example
     */
    public static @Nullable String extractDeviceID(String topic) {
        String[] strings = topic.split("/");
        if (strings.length > 2) {
            return strings[1];
        }
        return null;
    }

    /**
     * Returns true if the version is something like "3.x" or "4.x".
     */
    public static boolean checkVersion(byte[] payload) {
        return payload.length > 0 && (payload[0] == '3' || payload[0] == '4');
    }

    @Override
    public void receivedMessage(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
            byte[] payload) {
        resetTimeout();

        if (!checkVersion(payload)) {
            logger.trace("Found homie device. But version {} is out of range.",
                    new String(payload, StandardCharsets.UTF_8));
            return;
        }
        final String deviceID = extractDeviceID(topic);
        if (deviceID == null) {
            logger.trace("Found homie device. But deviceID {} is invalid.", deviceID);
            return;
        }

        publishDevice(connectionBridge, connection, deviceID, topic, deviceID);
    }

    void publishDevice(ThingUID connectionBridge, MqttBrokerConnection connection, String deviceID, String topic,
            String name) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("deviceid", deviceID);
        properties.put("basetopic", topic.substring(0, topic.indexOf("/")));

        thingDiscovered(DiscoveryResultBuilder
                .create(new ThingUID(MqttBindingConstants.HOMIE300_MQTT_THING, connectionBridge, deviceID))
                .withBridge(connectionBridge).withProperties(properties).withRepresentationProperty("deviceid")
                .withLabel(name).build());
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic) {
        String deviceID = extractDeviceID(topic);
        if (deviceID == null) {
            return;
        }
        thingRemoved(new ThingUID(MqttBindingConstants.HOMIE300_MQTT_THING, connectionBridge, deviceID));
    }
}
