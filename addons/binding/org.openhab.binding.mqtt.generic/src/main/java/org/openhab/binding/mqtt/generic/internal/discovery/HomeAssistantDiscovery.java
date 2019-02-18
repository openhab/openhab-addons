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
package org.openhab.binding.mqtt.generic.internal.discovery;

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
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.generic.internal.convention.homeassistant.AbstractConfiguration;
import org.openhab.binding.mqtt.generic.internal.convention.homeassistant.HaID;
import org.openhab.binding.mqtt.generic.internal.generic.MqttTypeProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link HomeAssistantDiscovery} is responsible for discovering device nodes that follow the
 * Home Assistant MQTT discovery convention (https://www.home-assistant.io/docs/mqtt/discovery/).
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.mqttha")
@NonNullByDefault
public class HomeAssistantDiscovery extends AbstractMQTTDiscovery {

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantDiscovery.class);
    protected final Map<String, Set<HaID>> componentsPerThingID = new TreeMap<>();
    private @Nullable ScheduledFuture<?> future;

    public static final Map<String, String> HA_COMP_TO_NAME = new TreeMap<String, String>();
    {
        HA_COMP_TO_NAME.put("alarm_control_panel", "Alarm Control Panel");
        HA_COMP_TO_NAME.put("binary_sensor", "Sensor");
        HA_COMP_TO_NAME.put("camera", "Camera");
        HA_COMP_TO_NAME.put("cover", "Blind");
        HA_COMP_TO_NAME.put("device_tracker", "Device Tracker");
        HA_COMP_TO_NAME.put("fan", "Fan");
        HA_COMP_TO_NAME.put("climate", "Climate Control");
        HA_COMP_TO_NAME.put("light", "Light");
        HA_COMP_TO_NAME.put("lock", "Lock");
        HA_COMP_TO_NAME.put("sensor", "Sensor");
        HA_COMP_TO_NAME.put("switch", "Switch");
        HA_COMP_TO_NAME.put("vacuum", "Vaccum");
    }

    static final String BASE_TOPIC = "homeassistant";

    public HomeAssistantDiscovery() {
        super(Stream.of(MqttBindingConstants.HOMEASSISTANT_MQTT_THING).collect(Collectors.toSet()), 3, true,
                BASE_TOPIC + "/#");
    }

    @NonNullByDefault({})
    protected MQTTTopicDiscoveryService mqttTopicDiscovery;

    @NonNullByDefault({})
    protected MqttTypeProvider typeProvider;

    @Reference
    public void setMQTTTopicDiscoveryService(MQTTTopicDiscoveryService service) {
        mqttTopicDiscovery = service;
    }

    public void unsetMQTTTopicDiscoveryService(@Nullable MQTTTopicDiscoveryService service) {
        mqttTopicDiscovery.unsubscribe(this);
        this.mqttTopicDiscovery = null;
    }

    @Reference
    protected void setTypeProvider(MqttTypeProvider provider) {
        this.typeProvider = provider;
    }

    protected void unsetTypeProvider(MqttTypeProvider provider) {
        this.typeProvider = null;
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

        // Reset the found-component timer.
        // We will collect components for the thing label description for another 2 seconds.
        final ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(false);
        }
        this.future = scheduler.schedule(componentsPerThingID::clear, 2, TimeUnit.SECONDS);

        AbstractConfiguration config = new Gson().fromJson(new String(payload, StandardCharsets.UTF_8),
                AbstractConfiguration.class);

        // We will of course find multiple of the same unique Thing IDs, for each different component another one.
        // Therefore the components are assembled into a list and given to the DiscoveryResult label for the user to
        // easily recognize object capabilities.
        // We use an own type for each discovered device, as it will have a unique group configuration.

        HaID topicParts = determineTopicParts(topic);
        final String thingID = config.getId(topicParts.getThingID());
        final ThingTypeUID typeID = new ThingTypeUID(MqttBindingConstants.BINDING_ID,
                MqttBindingConstants.HOMEASSISTANT_MQTT_THING.getId() + "_" + thingID);

        ThingType type = typeProvider.derive(typeID, MqttBindingConstants.HOMEASSISTANT_MQTT_THING).build();
        typeProvider.setThingTypeIfAbsent(typeID, type);

        final ThingUID thingUID = new ThingUID(typeID, connectionBridge, thingID);

        // We need to keep track of already found component topics for a specific object_id/node_id
        Set<HaID> components = componentsPerThingID.computeIfAbsent(thingID, thing -> new HashSet<>());
        components.add(topicParts);

        final String componentNames = components.stream().map(id -> id.component)
                .map(c -> HA_COMP_TO_NAME.getOrDefault(c, c)).collect(Collectors.joining(", "));

        Map<String, Object> properties = new HashMap<>();
        properties.put("basetopic", BASE_TOPIC);
        properties.put("objectid", components.stream().map(id -> id.objectID).distinct().collect(Collectors.toList()));
        properties.put("thingid", thingID);
        config.addDeviceProperties(properties);

        // First remove an already discovered thing with the same ID
        thingRemoved(thingUID);
        // Because we need the new properties map with the updated "components" list
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty("thingid").withBridge(connectionBridge)
                .withLabel(config.getThingName() + " (" + componentNames + ")").build());
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic) {
        if (!topic.endsWith("/config")) {
            return;
        }
        // TODO:...
        final String thingID = determineTopicParts(topic).getThingID();
        componentsPerThingID.remove(thingID);
        thingRemoved(new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, connectionBridge, thingID));
    }
}
