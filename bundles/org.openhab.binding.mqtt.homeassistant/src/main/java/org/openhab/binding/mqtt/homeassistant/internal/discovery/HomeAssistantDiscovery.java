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
package org.openhab.binding.mqtt.homeassistant.internal.discovery;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantPythonBridge;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractComponentConfiguration;
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
    protected final Map<String, DiscoveryResult> allResults = new HashMap<>();
    private Set<ThingUID> dirtyResults = new HashSet<>();
    private final Object discoveryStateLock = new Object();

    private @Nullable ScheduledFuture<?> future;
    private final HomeAssistantPythonBridge python;

    static final String BASE_TOPIC = "homeassistant";
    static final String BIRTH_TOPIC = "homeassistant/status";
    static final String ONLINE_STATUS = "online";
    private volatile long lastEventTime = 0;
    private final static long DISCOVERY_TIMEOUT_MS = 2000;

    @NonNullByDefault({})
    protected MqttChannelTypeProvider typeProvider;

    @NonNullByDefault({})
    protected MQTTTopicDiscoveryService mqttTopicDiscovery;

    @Activate
    public HomeAssistantDiscovery(@Nullable Map<String, Object> properties,
            @Reference HomeAssistantPythonBridge python) {
        super(null, 3, true, BASE_TOPIC + "/#");
        configuration = (new Configuration(properties)).as(HomeAssistantConfiguration.class);
        this.python = python;
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
            AbstractComponentConfiguration config = AbstractComponentConfiguration.create(python, haID.component,
                    new String(payload, StandardCharsets.UTF_8));

            final String thingID = config.getThingId(haID.objectID);
            final ThingUID thingUID = new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, bridgeUID, thingID);

            // Build properties and DiscoveryResult outside the lock
            Map<String, Object> properties = new HashMap<>();
            properties = config.appendToProperties(properties);
            properties.put("deviceId", thingID);

            DiscoveryResult result = buildResult(thingID, thingUID, config.getThingName(), haID, properties, bridgeUID);

            // Now only mutate shared state under the lock
            synchronized (discoveryStateLock) {
                thingIDPerTopic.put(topic, thingUID);
                applyResult(thingID, haID, result);
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
        lastEventTime = System.currentTimeMillis();
        if (future == null || future.isDone()) {
            future = scheduler.schedule(this::checkAndPublish, DISCOVERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void checkAndPublish() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastEventTime;

        if (elapsed >= DISCOVERY_TIMEOUT_MS) {
            publishResults(); // process the accumulated results
            future = null; // allow new scheduling
        } else {
            // reschedule only for the remaining time
            future = scheduler.schedule(this::checkAndPublish, DISCOVERY_TIMEOUT_MS - elapsed, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Builds a {@link DiscoveryResult} for a given Thing. Responsibilities of this method:
     * <ul>
     * <li>Maintain a consistent, ordered set of components ({@link HaID}) per Thing ID.
     * A {@link TreeSet} is used to guarantee stable ordering of components.</li>
     * <li>Convert components into a list of "short topics", which are passed into the
     * {@link HandlerConfiguration} for handler initialization.</li>
     * <li>Assemble a new {@link DiscoveryResult} with the given properties, label,
     * and bridge reference.</li>
     * </ul>
     * Ordering note: this method is the only place where the ordered list of components
     * and topics is constructed. Callers (such as {@code applyResult}) must treat the
     * returned {@link DiscoveryResult} as authoritative for ordering.
     *
     * @param thingID the stable ID of the discovered Thing
     * @param thingUID the unique identifier for the Thing
     * @param thingName human-readable label for the Thing
     * @param haID the Home Assistant component identifier for this discovery event
     * @param properties current Thing properties, to be extended with handler configuration
     * @param bridgeUID UID of the bridge Thing (MQTT broker)
     * @return a complete and ordered {@link DiscoveryResult} representing the Thing
     */
    private DiscoveryResult buildResult(String thingID, ThingUID thingUID, String thingName, HaID haID,
            Map<String, Object> properties, ThingUID bridgeUID) {
        // Use a TreeSet to keep components sorted automatically
        Set<HaID> componentsSet = componentsPerThingID.computeIfAbsent(thingID,
                key -> new TreeSet<>(Comparator.comparing(HaID::toString)));

        componentsSet.add(haID);

        // Convert components to short topics
        List<String> topics = componentsSet.stream().map(HaID::toShortTopic).toList();

        // Append handler configuration
        HandlerConfiguration handlerConfig = new HandlerConfiguration(haID.baseTopic, topics);
        properties = handlerConfig.appendToProperties(properties);

        return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withRepresentationProperty("deviceId")
                .withBridge(bridgeUID).withLabel(thingName).build();
    }

    /**
     * Stores the newly built DiscoveryResult and marks it dirty.
     */
    private void applyResult(String thingID, HaID haID, DiscoveryResult result) {
        allResults.put(result.getThingUID().toString(), result);
        dirtyResults.add(result.getThingUID());
    }

    protected void publishResults() {
        Set<ThingUID> toPublish;
        synchronized (discoveryStateLock) {
            toPublish = dirtyResults;
            dirtyResults = new HashSet<>();
        }
        for (ThingUID uid : toPublish) {
            DiscoveryResult result = allResults.get(uid.toString());
            if (result != null) {
                thingDiscovered(result);
            }
        }
    }

    @Override
    public void topicVanished(ThingUID bridgeUID, MqttBrokerConnection connection, String topic) {
        if (!topic.endsWith("/config")) {
            return;
        }
        ThingUID thingUID;
        HaID haID = new HaID(topic);
        String thingID;

        // Step 1: remove the topic mapping (under lock)
        synchronized (discoveryStateLock) {
            thingUID = thingIDPerTopic.remove(topic);
        }
        if (thingUID == null) {
            return;
        }

        thingID = thingUID.getId();

        // Step 2: decide what to do about components (under lock)
        boolean removedLastComponent;
        DiscoveryResult existingThing;
        synchronized (discoveryStateLock) {
            Set<HaID> components = componentsPerThingID.getOrDefault(thingID, Collections.emptySet());
            components.remove(haID);
            removedLastComponent = components.isEmpty();
            existingThing = allResults.get(thingUID.toString());

            if (removedLastComponent) {
                componentsPerThingID.remove(thingID);
                allResults.remove(thingUID.toString());
                dirtyResults.remove(thingUID);
                thingRemoved(thingUID);
            }
        }

        if (removedLastComponent) {
            return;
        }

        // Step 3: heavy work outside lock
        if (existingThing == null) {
            logger.warn("Could not find discovery result for removed component {}; this is a bug", thingUID);
            return;
        }

        resetPublishTimer();
        Map<String, Object> properties = new HashMap<>(existingThing.getProperties());
        DiscoveryResult result = buildResult(thingID, thingUID, existingThing.getLabel(), haID, properties, bridgeUID);

        // Step 4: commit new result under lock
        synchronized (discoveryStateLock) {
            applyResult(thingID, haID, result);
        }
    }
}
