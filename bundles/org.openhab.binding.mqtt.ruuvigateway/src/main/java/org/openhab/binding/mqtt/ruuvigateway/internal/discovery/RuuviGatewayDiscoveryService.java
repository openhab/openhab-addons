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

package org.openhab.binding.mqtt.ruuvigateway.internal.discovery;

import static org.openhab.binding.mqtt.ruuvigateway.internal.RuuviGatewayBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.discovery.AbstractMQTTDiscovery;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.ruuvigateway.internal.RuuviGatewayBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link RuuviGatewayDiscoveryService} is responsible for finding Ruuvi Tag Sensors
 * and setting them up for the handlers.
 *
 * @author Matthew Skinner - Initial contribution
 * @author Sami Salonen - Adaptation to Ruuvi Gateway
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.mqttruuvigateway")
@NonNullByDefault
public class RuuviGatewayDiscoveryService extends AbstractMQTTDiscovery {
    protected final MQTTTopicDiscoveryService discoveryService;

    private static final Predicate<String> HEX_PATTERN_CHECKER = Pattern
            .compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$").asMatchPredicate();

    @Activate
    public RuuviGatewayDiscoveryService(@Reference MQTTTopicDiscoveryService discoveryService) {
        super(SUPPORTED_THING_TYPES_UIDS, 3, true, BASE_TOPIC + "#");
        this.discoveryService = discoveryService;
    }

    @Override
    protected MQTTTopicDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    @Override
    public void receivedMessage(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
            byte[] payload) {
        resetTimeout();
        if (topic.startsWith(BASE_TOPIC)) {
            String cutTopic = topic.replace(BASE_TOPIC, "");
            int index = cutTopic.lastIndexOf("/");
            if (index != -1) // -1 means "not found"
            {
                String tagMacAddress = cutTopic.substring(index + 1);
                if (looksLikeMac(tagMacAddress)) {
                    publishDevice(connectionBridge, connection, topic, tagMacAddress);
                }
            }
        }
    }

    void publishDevice(ThingUID connectionBridge, MqttBrokerConnection connection, String topic, String tagMacAddress) {
        Map<String, Object> properties = new HashMap<>();
        String thingID = tagMacAddress.toLowerCase().replaceAll("[:-]", "");
        String normalizedTagID = normalizedTagID(tagMacAddress);
        properties.put(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TOPIC, topic);
        properties.put(RuuviGatewayBindingConstants.PROPERTY_TAG_ID, normalizedTagID);
        properties.put(Thing.PROPERTY_VENDOR, "Ruuvi Innovations Ltd (Oy)");

        // Discovered things are identified with their topic name, in case of having pathological case
        // where we find multiple tags with same mac address (e.g. ruuvi/gw1/mac1 and ruuvi/gw2/mac1)
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_BEACON, connectionBridge, thingID))
                .withProperties(properties)
                .withRepresentationProperty(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TOPIC)
                .withBridge(connectionBridge).withLabel("MQTT Ruuvi Tag " + normalizedTagID).build());
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic) {
    }

    private boolean looksLikeMac(String topic) {
        return HEX_PATTERN_CHECKER.test(topic);
    }

    private static String normalizedTagID(String mac) {
        String nondelimited = mac.toUpperCase().replaceAll("[:-]", "");
        assert nondelimited.length() == 12; // Invariant: method to be used only with valid Ruuvi MACs
        return nondelimited.subSequence(0, 2) + ":" + nondelimited.subSequence(2, 4) + ":"
                + nondelimited.subSequence(4, 6) + ":" + nondelimited.subSequence(6, 8) + ":"
                + nondelimited.subSequence(8, 10) + ":" + nondelimited.subSequence(10, 12);
    }
}
