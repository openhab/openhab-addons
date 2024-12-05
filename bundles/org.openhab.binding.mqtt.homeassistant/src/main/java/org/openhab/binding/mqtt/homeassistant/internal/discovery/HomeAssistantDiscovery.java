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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.discovery.AbstractMQTTDiscovery;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ThingType;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
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
@Component(service = DiscoveryService.class, configurationPid = "discovery.mqttha", property = Constants.SERVICE_PID
        + "=discovery.mqttha")
@ConfigurableService(category = "system", label = "Home Assistant Discovery", description_uri = "binding:mqtt.homeassistant")
@NonNullByDefault
public class HomeAssistantDiscovery extends AbstractMQTTDiscovery {
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantDiscovery.class);
    private HomeAssistantConfiguration configuration;
    protected final Map<String, Set<HaID>> componentsPerThingID = new HashMap<>();
    protected final Map<String, ThingUID> thingIDPerTopic = new HashMap<>();
    protected final Map<String, DiscoveryResult> results = new HashMap<>();
    protected final Map<String, DiscoveryResult> allResults = new HashMap<>();

    private @Nullable ScheduledFuture<?> future;
    private final Gson gson;

    static final String BASE_TOPIC = "homeassistant";
    static final String BIRTH_TOPIC = "homeassistant/status";
    static final String ONLINE_STATUS = "online";

    @NonNullByDefault({})
    protected MqttChannelTypeProvider typeProvider;

    @NonNullByDefault({})
    protected MQTTTopicDiscoveryService mqttTopicDiscovery;

    @Activate
    public HomeAssistantDiscovery(@Nullable Map<String, Object> properties) {
        super(null, 3, true, BASE_TOPIC + "/#");
        this.gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory()).create();
        configuration = (new Configuration(properties)).as(HomeAssistantConfiguration.class);
    }

    @Reference
    public void setMQTTTopicDiscoveryService(MQTTTopicDiscoveryService service) {
        mqttTopicDiscovery = service;
    }

    public void unsetMQTTTopicDiscoveryService(@Nullable MQTTTopicDiscoveryService service) {
        mqttTopicDiscovery.unsubscribe(this);
        this.mqttTopicDiscovery = null;
    }

    @Modified
    protected void modified(@Nullable Map<String, Object> properties) {
        configuration = (new Configuration(properties)).as(HomeAssistantConfiguration.class);
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
        return typeProvider.getThingTypes(null).stream().map(ThingType::getUID).collect(Collectors.toSet());
    }

    @Override
    public void receivedMessage(ThingUID bridgeUID, MqttBrokerConnection connection, String topic, byte[] payload) {
        resetTimeout();

        // For HomeAssistant we need to subscribe to a wildcard topic, because topics can either be:
        // homeassistant/<component>/<node_id>/<object_id>/config OR
        // homeassistant/<component>/<object_id>/config.
        // We check for the last part to filter all non-config topics out.
        if (!topic.endsWith("/config")) {
            return;
        }

        resetPublishTimer();

        // We will of course find multiple of the same unique Thing IDs, for each different component another one.
        // Therefore the components are assembled into a list and given to the DiscoveryResult label for the user to
        // easily recognize object capabilities.
        HaID haID = new HaID(topic);

        try {
            AbstractChannelConfiguration config = AbstractChannelConfiguration
                    .fromString(new String(payload, StandardCharsets.UTF_8), gson);

            final String thingID = config.getThingId(haID.objectID);
            final ThingUID thingUID = new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, bridgeUID, thingID);

            synchronized (results) {
                thingIDPerTopic.put(topic, thingUID);

                Map<String, Object> properties = new HashMap<>();
                properties = config.appendToProperties(properties);
                properties.put("deviceId", thingID);
                properties.put("newStyleChannels", "true");

                buildResult(thingID, thingUID, config.getThingName(), haID, properties, bridgeUID);
            }
        } catch (ConfigurationException e) {
            logger.warn("HomeAssistant discover error: invalid configuration of thing {} component {}: {}",
                    haID.objectID, haID.component, e.getMessage());
        } catch (Exception e) {
            logger.warn("HomeAssistant discover error: {}", e.getMessage());
        }
    }

    @Override
    protected void startScan() {
        super.startScan();
        triggerDeviceDiscovery();
    }

    @Override
    protected void startBackgroundDiscovery() {
        super.startBackgroundDiscovery();
        triggerDeviceDiscovery();
    }

    private void triggerDeviceDiscovery() {
        if (!configuration.status) {
            return;
        }
        // https://www.home-assistant.io/integrations/mqtt/#use-the-birth-and-will-messages-to-trigger-discovery
        getDiscoveryService().publish(BIRTH_TOPIC, ONLINE_STATUS.getBytes(), 1, false);
    }

    private void resetPublishTimer() {
        // Reset the found-component timer.
        // We will collect components for the thing label description for another 2 seconds.
        final ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(false);
        }
        this.future = scheduler.schedule(this::publishResults, 2, TimeUnit.SECONDS);
    }

    private void buildResult(String thingID, ThingUID thingUID, String thingName, HaID haID,
            Map<String, Object> properties, ThingUID bridgeUID) {
        // We need to keep track of already found component topics for a specific thing
        final List<HaID> components;
        {
            Set<HaID> componentsUnordered = componentsPerThingID.computeIfAbsent(thingID, key -> new HashSet<>());

            // Invariant. For compiler, computeIfAbsent above returns always
            // non-null
            Objects.requireNonNull(componentsUnordered);
            componentsUnordered.add(haID);

            components = componentsUnordered.stream().collect(Collectors.toList());
            // We sort the components for consistent jsondb serialization order of 'topics' thing property
            // Sorting key is HaID::toString, i.e. using the full topic string
            components.sort(Comparator.comparing(HaID::toString));
        }

        final List<String> topics = components.stream().map(HaID::toShortTopic).collect(Collectors.toList());

        HandlerConfiguration handlerConfig = new HandlerConfiguration(haID.baseTopic, topics);
        properties = handlerConfig.appendToProperties(properties);

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty("deviceId").withBridge(bridgeUID).withLabel(thingName).build();
        // Because we need the new properties map with the updated "components" list
        results.put(thingUID.toString(), result);
        allResults.put(thingUID.toString(), result);
    }

    protected void publishResults() {
        Collection<DiscoveryResult> localResults;

        synchronized (results) {
            localResults = new ArrayList<>(results.values());
            results.clear();
        }
        for (DiscoveryResult result : localResults) {
            thingDiscovered(result);
        }
    }

    @Override
    public void topicVanished(ThingUID bridgeUID, MqttBrokerConnection connection, String topic) {
        if (!topic.endsWith("/config")) {
            return;
        }
        synchronized (results) {
            ThingUID thingUID = thingIDPerTopic.remove(topic);
            if (thingUID != null) {
                final String thingID = thingUID.getId();

                HaID haID = new HaID(topic);

                Set<HaID> components = componentsPerThingID.getOrDefault(thingID, Collections.emptySet());
                components.remove(haID);
                if (components.isEmpty()) {
                    allResults.remove(thingUID.toString());
                    results.remove(thingUID.toString());
                    thingRemoved(thingUID);
                } else {
                    resetPublishTimer();

                    DiscoveryResult existingThing = allResults.get(thingUID.toString());
                    if (existingThing == null) {
                        logger.warn("Could not find discovery result for removed component {}; this is a bug",
                                thingUID);
                        return;
                    }
                    Map<String, Object> properties = new HashMap<>(existingThing.getProperties());
                    buildResult(thingID, thingUID, existingThing.getLabel(), haID, properties, bridgeUID);
                }
            }
        }
    }
}
