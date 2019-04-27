/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.discovery;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.discovery.AbstractMQTTDiscovery;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homeassistant.internal.BaseChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link HomeAssistantDiscovery} is responsible for discovering device nodes that follow the
 * Home Assistant MQTT discovery convention (https://www.home-assistant.io/docs/mqtt/discovery/).
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.mqttha")
@NonNullByDefault
public class HomeAssistantDiscovery extends AbstractMQTTDiscovery {
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantDiscovery.class);
    protected final Map<String, Set<String>> componentsPerThingID = new TreeMap<>();
    private @Nullable ScheduledFuture<?> future;
    private final Gson gson;

    public static final Map<String, String> HA_COMP_TO_NAME = new TreeMap<>();
    {
        HA_COMP_TO_NAME.put("alarm_control_panel", "Alarm Control Panel");
        HA_COMP_TO_NAME.put("binary_sensor", "Sensor");
        HA_COMP_TO_NAME.put("camera", "Camera");
        HA_COMP_TO_NAME.put("cover", "Blind");
        HA_COMP_TO_NAME.put("fan", "Fan");
        HA_COMP_TO_NAME.put("climate", "Climate Control");
        HA_COMP_TO_NAME.put("light", "Light");
        HA_COMP_TO_NAME.put("lock", "Lock");
        HA_COMP_TO_NAME.put("sensor", "Sensor");
        HA_COMP_TO_NAME.put("switch", "Switch");
    }

    static final String BASE_TOPIC = "homeassistant";

    public HomeAssistantDiscovery() {
        super(Stream.of(MqttBindingConstants.HOMEASSISTANT_MQTT_THING).collect(Collectors.toSet()), 3, true,
                BASE_TOPIC + "/#");
        this.gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory()).create();
    }

    @NonNullByDefault({})
    protected MQTTTopicDiscoveryService mqttTopicDiscovery;

    @Reference
    public void setMQTTTopicDiscoveryService(MQTTTopicDiscoveryService service) {
        mqttTopicDiscovery = service;
    }

    public void unsetMQTTTopicDiscoveryService(@Nullable MQTTTopicDiscoveryService service) {
        mqttTopicDiscovery.unsubscribe(this);
        this.mqttTopicDiscovery = null;
    }

    @Override
    protected MQTTTopicDiscoveryService getDiscoveryService() {
        return mqttTopicDiscovery;
    }

    /**
     * @param topic A topic like "homeassistant/binary_sensor/garden/config"
     * @return Returns the "mydevice" part of the example
     */
    public static HaID determineTopicParts(String topic) {
        return new HaID(topic);
    }

    /**
     * Returns true if the version is something like "3.x". We accept
     * version 3 up to but not including version 4 of the homie spec.
     */
    public static boolean checkVersion(String versionString) {
        String[] strings = versionString.split("\\.");
        if (strings.length < 2) {
            return false;
        }
        return strings[0].equals("3");
    }

    @Override
    public void receivedMessage(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
            byte[] payload) {
        // For HomeAssistant we need to subscribe to a wildcard topic, because topics can either be:
        // homeassistant/<component>/<node_id>/<object_id>/config OR
        // homeassistant/<component>/<object_id>/config.
        // We check for the last part to filter all non-config topics out.
        if (!topic.endsWith("/config")) {
            return;
        }

        // We will of course find multiple of the same unique Thing IDs, for each different component another one.
        // Therefore the components are assembled into a list and given to the DiscoveryResult label for the user to
        // easily recognize object capabilities.
        HaID topicParts = determineTopicParts(topic);
        final String thingID = topicParts.objectID;
        final ThingUID thingUID = new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, connectionBridge,
                thingID);

        // Reset the found-component timer.
        // We will collect components for the thing label description for another 2 seconds.
        final ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(false);
        }
        this.future = scheduler.schedule(componentsPerThingID::clear, 2, TimeUnit.SECONDS);

        // We need to keep track of already found component topics for a specific object_id/node_id
        Set<String> components = componentsPerThingID.getOrDefault(thingID, new HashSet<>());
        if (components.contains(topicParts.component)) {
            logger.trace("Discovered an already known component {}", topicParts.component);
            return; // If we already know about this object component, ignore the discovered topic.
        }
        components.add(topicParts.component);
        componentsPerThingID.put(thingID, components);

        final String componentNames = components.stream().map(c -> HA_COMP_TO_NAME.getOrDefault(c, c))
                .collect(Collectors.joining(","));

        BaseChannelConfiguration config = BaseChannelConfiguration
                .fromString(new String(payload, StandardCharsets.UTF_8), gson);

        Map<String, Object> properties = new HashMap<>();
        HandlerConfiguration handlerConfig = topicParts.toHandlerConfiguration();
        properties = handlerConfig.appendToProperties(properties);
        properties = config.appendToProperties(properties);
        // First remove an already discovered thing with the same ID
        thingRemoved(thingUID);
        // Because we need the new properties map with the updated "components" list
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty("objectid").withBridge(connectionBridge)
                .withLabel(config.name + " (" + componentNames + ")").build());
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic) {
        if (!topic.endsWith("/config")) {
            return;
        }
        final String thingID = determineTopicParts(topic).objectID;
        componentsPerThingID.remove(thingID);
        thingRemoved(new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, connectionBridge, thingID));
    }

}
