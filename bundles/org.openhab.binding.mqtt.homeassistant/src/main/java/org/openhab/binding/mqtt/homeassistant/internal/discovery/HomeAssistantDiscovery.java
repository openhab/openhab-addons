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
package org.openhab.binding.mqtt.homeassistant.internal.discovery;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.discovery.AbstractMQTTDiscovery;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ThingType;
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
@Component(service = DiscoveryService.class, configurationPid = "discovery.mqttha")
@NonNullByDefault
public class HomeAssistantDiscovery extends AbstractMQTTDiscovery {
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantDiscovery.class);
    protected final Map<String, Set<HaID>> componentsPerThingID = new TreeMap<>();
    protected final Map<String, ThingUID> thingIDPerTopic = new TreeMap<>();
    protected final Map<String, DiscoveryResult> results = new ConcurrentHashMap<>();

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

    @NonNullByDefault({})
    protected MqttChannelTypeProvider typeProvider;

    @NonNullByDefault({})
    protected MQTTTopicDiscoveryService mqttTopicDiscovery;

    public HomeAssistantDiscovery() {
        super(null, 3, true, BASE_TOPIC + "/#");
        this.gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory()).create();
    }

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

    @Reference
    protected void setTypeProvider(MqttChannelTypeProvider provider) {
        this.typeProvider = provider;
    }

    protected void unsetTypeProvider(MqttChannelTypeProvider provider) {
        this.typeProvider = null;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return typeProvider.getThingTypeUIDs();
    }

    /**
     * Summarize components such as {Switch, Switch, Sensor} into string "Sensor, 2x Switch"
     *
     * @param componentNames stream of component names
     * @return summary string of component names and their counts
     */
    static String getComponentNamesSummary(Stream<String> componentNames) {
        StringBuilder summary = new StringBuilder();
        Collector<String, ?, Long> countingCollector = Collectors.counting();
        Map<String, Long> componentCounts = componentNames
                .collect(Collectors.groupingBy(Function.identity(), countingCollector));
        componentCounts.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String componentName = entry.getKey();
            long count = entry.getValue();
            if (summary.length() > 0) {
                // not the first entry, so let's add the separating comma
                summary.append(", ");
            }
            if (count > 1) {
                summary.append(count);
                summary.append("x ");
            }
            summary.append(componentName);
        });
        return summary.toString();
    }

    @Override
    public void receivedMessage(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
            byte[] payload) {
        resetTimeout();

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
        this.future = scheduler.schedule(this::publishResults, 2, TimeUnit.SECONDS);

        // We will of course find multiple of the same unique Thing IDs, for each different component another one.
        // Therefore the components are assembled into a list and given to the DiscoveryResult label for the user to
        // easily recognize object capabilities.
        HaID haID = new HaID(topic);

        try {
            AbstractChannelConfiguration config = AbstractChannelConfiguration
                    .fromString(new String(payload, StandardCharsets.UTF_8), gson);

            final String thingID = config.getThingId(haID.objectID);

            final ThingTypeUID typeID = new ThingTypeUID(MqttBindingConstants.BINDING_ID,
                    MqttBindingConstants.HOMEASSISTANT_MQTT_THING.getId() + "_" + thingID);

            final ThingUID thingUID = new ThingUID(typeID, connectionBridge, thingID);

            thingIDPerTopic.put(topic, thingUID);

            // We need to keep track of already found component topics for a specific thing
            final List<HaID> components;
            {
                Set<HaID> componentsUnordered = componentsPerThingID.computeIfAbsent(thingID,
                        key -> ConcurrentHashMap.newKeySet());

                // Invariant. For compiler, computeIfAbsent above returns always
                // non-null
                Objects.requireNonNull(componentsUnordered);
                componentsUnordered.add(haID);

                components = componentsUnordered.stream().collect(Collectors.toList());
                // We sort the components for consistent jsondb serialization order of 'topics' thing property
                // Sorting key is HaID::toString, i.e. using the full topic string
                components.sort(Comparator.comparing(HaID::toString));
            }

            final String componentNames = getComponentNamesSummary(
                    components.stream().map(id -> id.component).map(c -> HA_COMP_TO_NAME.getOrDefault(c, c)));

            final List<String> topics = components.stream().map(HaID::toShortTopic).collect(Collectors.toList());

            Map<String, Object> properties = new HashMap<>();
            HandlerConfiguration handlerConfig = new HandlerConfiguration(haID.baseTopic, topics);
            properties = handlerConfig.appendToProperties(properties);
            properties = config.appendToProperties(properties);
            properties.put("deviceId", thingID);

            // Because we need the new properties map with the updated "components" list
            results.put(thingUID.getAsString(),
                    DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withRepresentationProperty("deviceId").withBridge(connectionBridge)
                            .withLabel(config.getThingName() + " (" + componentNames + ")").build());
        } catch (ConfigurationException e) {
            logger.warn("HomeAssistant discover error: invalid configuration of thing {} component {}: {}",
                    haID.objectID, haID.component, e.getMessage());
        } catch (Exception e) {
            logger.warn("HomeAssistant discover error: {}", e.getMessage());
        }
    }

    protected void publishResults() {
        Collection<DiscoveryResult> localResults;

        localResults = new ArrayList<>(results.values());
        results.clear();
        componentsPerThingID.clear();
        for (DiscoveryResult result : localResults) {
            final ThingTypeUID typeID = result.getThingTypeUID();
            ThingType type = typeProvider.derive(typeID, MqttBindingConstants.HOMEASSISTANT_MQTT_THING).build();
            typeProvider.setThingTypeIfAbsent(typeID, type);

            thingDiscovered(result);
        }
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic) {
        if (!topic.endsWith("/config")) {
            return;
        }
        if (thingIDPerTopic.containsKey(topic)) {
            ThingUID thingUID = thingIDPerTopic.remove(topic);
            if (thingUID != null) {
                final String thingID = thingUID.getId();

                HaID haID = new HaID(topic);

                Set<HaID> components = componentsPerThingID.getOrDefault(thingID, Collections.emptySet());
                components.remove(haID);
                if (components.isEmpty()) {
                    thingRemoved(thingUID);
                }
            }
        }
    }
}
