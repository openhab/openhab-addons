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
package org.openhab.binding.mqtt.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.MqttBindingConstants;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryParticipant;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.binding.mqtt.handler.BrokerHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MqttBrokerHandlerFactory} is responsible for creating things and thing
 * handlers. It keeps reference to all handlers and implements the {@link MQTTTopicDiscoveryService} service
 * interface, so service consumers can subscribe to a topic on all available broker connections.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class,
        MQTTTopicDiscoveryService.class }, configurationPid = "MqttBrokerHandlerFactory")
public class MqttBrokerHandlerFactory extends BaseThingHandlerFactory implements MQTTTopicDiscoveryService {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(MqttBindingConstants.BRIDGE_TYPE_BROKER).collect(Collectors.toSet());

    /**
     * This Map provides a lookup between a Topic string (key) and a Set of MQTTTopicDiscoveryParticipants (value),
     * where the Set itself is a list of participants which are subscribed to the respective Topic.
     */
    protected final Map<String, Set<MQTTTopicDiscoveryParticipant>> discoveryTopics = new ConcurrentHashMap<>();

    /**
     * This Set contains a list of all the Broker handlers that have been created by this factory
     */
    protected final Set<AbstractBrokerHandler> handlers = Collections
            .synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Add the given broker handler to the list of known handlers. And then iterate over all topics and their respective
     * list of listeners, and register the respective new listener and topic with the given new broker handler.
     */
    protected void createdHandler(AbstractBrokerHandler handler) {
        handlers.add(handler);
        discoveryTopics.forEach((topic, listeners) -> {
            listeners.forEach(listener -> {
                handler.registerDiscoveryListener(listener, topic);
            });
        });
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (!(thing instanceof Bridge)) {
            throw new IllegalStateException("A bridge type is expected");
        }
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        final AbstractBrokerHandler handler;
        if (thingTypeUID.equals(MqttBindingConstants.BRIDGE_TYPE_BROKER)) {
            handler = new BrokerHandler((Bridge) thing);
        } else {
            throw new IllegalStateException("Not supported " + thingTypeUID.toString());
        }
        createdHandler(handler);
        return handler;
    }

    /**
     * This factory also implements {@link MQTTTopicDiscoveryService} so consumers can subscribe to
     * a MQTT topic that is registered on all available broker connections.
     *
     * Checks each topic, and if the listener is not already in the listener list for that topic, adds itself from that
     * list, and registers itself and the respective topic with all the known brokers.
     */
    @Override
    @SuppressWarnings("null")
    public void subscribe(MQTTTopicDiscoveryParticipant listener, String topic) {
        Set<MQTTTopicDiscoveryParticipant> listeners = discoveryTopics.computeIfAbsent(topic,
                t -> ConcurrentHashMap.newKeySet());
        if (listeners.add(listener)) {
            handlers.forEach(broker -> broker.registerDiscoveryListener(listener, topic));
        }
    }

    /**
     * This factory also implements {@link MQTTTopicDiscoveryService} so consumers can unsubscribe from
     * a MQTT topic that is registered on all available broker connections.
     *
     * Checks each topic, and if the listener is in the listener list for that topic, removes itself from that list, and
     * unregisters itself and the respective topic from all the known brokers.
     */
    @Override
    public void unsubscribe(MQTTTopicDiscoveryParticipant listener) {
        discoveryTopics.forEach((topic, listeners) -> {
            if (listeners.remove(listener)) {
                handlers.forEach(broker -> broker.unregisterDiscoveryListener(listener, topic));
            }
        });
    }

    @Override
    public void publish(String topic, byte[] payload, int qos, boolean retain) {
        handlers.forEach(handler -> {
            handler.getConnectionAsync().thenAccept(connection -> {
                connection.publish(topic, payload, qos, retain);
            });
        });
    }
}
