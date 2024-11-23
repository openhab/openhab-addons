/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.espmilighthub.internal.discovery;

import static org.openhab.binding.mqtt.MqttBindingConstants.BINDING_ID;
import static org.openhab.binding.mqtt.espmilighthub.internal.EspMilightHubBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.discovery.AbstractMQTTDiscovery;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EspMilightHubDiscoveryService} is responsible for finding globes
 * and setting them up for the handlers.
 *
 * @author Matthew Skinner - Initial contribution
 */

@Component(service = DiscoveryService.class, configurationPid = "discovery.mqttespmilighthub")
@NonNullByDefault
public class EspMilightHubDiscoveryService extends AbstractMQTTDiscovery {
    protected final MQTTTopicDiscoveryService discoveryService;

    @Activate
    public EspMilightHubDiscoveryService(@Reference MQTTTopicDiscoveryService discoveryService) {
        super(SUPPORTED_THING_TYPES, 3, true, STATES_BASE_TOPIC + "#");
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
        if (topic.startsWith(STATES_BASE_TOPIC)) {
            String cutTopic = topic.replace(STATES_BASE_TOPIC, "");
            int index = cutTopic.indexOf("/");
            if (index != -1) // -1 means "not found"
            {
                String remoteCode = (cutTopic.substring(0, index)); // Store the remote code for use later
                cutTopic = topic.replace(STATES_BASE_TOPIC + remoteCode + "/", "");
                index = cutTopic.indexOf("/");
                if (index != -1) {
                    String globeType = (cutTopic.substring(0, index));
                    String remoteGroupID = (cutTopic.substring(index + 1, index + 2));
                    // openHAB's framework has better code for handling groups then the firmware does
                    if (!"0".equals(remoteGroupID)) {// Users can manually add group 0 things if they wish
                        publishDevice(connectionBridge, connection, topic, remoteCode, globeType, remoteGroupID);
                    }
                }
            }
        }
    }

    void publishDevice(ThingUID connectionBridge, MqttBrokerConnection connection, String topic, String remoteCode,
            String globeType, String remoteGroupID) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("deviceid", remoteCode + remoteGroupID);
        properties.put("basetopic", STATES_BASE_TOPIC + remoteCode + "/" + globeType + "/" + remoteGroupID);
        thingDiscovered(DiscoveryResultBuilder
                .create(new ThingUID(new ThingTypeUID(BINDING_ID, globeType), connectionBridge,
                        remoteCode + remoteGroupID))
                .withProperties(properties).withRepresentationProperty("deviceid").withBridge(connectionBridge)
                .withLabel("Milight " + globeType).build());
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic) {
    }
}
