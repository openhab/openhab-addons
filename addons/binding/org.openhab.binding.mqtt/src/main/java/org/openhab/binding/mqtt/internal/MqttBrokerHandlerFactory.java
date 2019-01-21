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
package org.openhab.binding.mqtt.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.openhab.binding.mqtt.MqttBindingConstants;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryParticipant;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.binding.mqtt.handler.BrokerHandler;
import org.openhab.binding.mqtt.handler.SystemBrokerHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
            .of(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER, MqttBindingConstants.BRIDGE_TYPE_BROKER)
            .collect(Collectors.toSet());
    protected final Map<MQTTTopicDiscoveryParticipant, TopicSubscribeMultiConnection> subscriber = Collections
            .synchronizedMap(new WeakHashMap<>());
    protected final Set<AbstractBrokerHandler> handlers = Collections
            .synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @NonNullByDefault({})
    private MqttService mqttService;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Reference
    public void setMqttService(MqttService service) {
        mqttService = service;
    }

    public void unsetMqttService(MqttService service) {
        mqttService = null;
    }

    /**
     * Remove the given broker connection from all listeners.
     */
    @Override
    protected void removeHandler(@NonNull ThingHandler thingHandler) {
        handlers.remove(thingHandler);
        subscriber.forEach((receiver, multiConnection) -> multiConnection.remove((AbstractBrokerHandler) thingHandler));
    }

    /**
     * Add the given broker connection to all listeners.
     */
    protected void createdHandler(AbstractBrokerHandler handler) {
        handlers.add(handler);
        subscriber.forEach((receiver, multiConnection) -> multiConnection.add(handler));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (mqttService == null) {
            throw new IllegalStateException("MqttService must be bound, before ThingHandlers can be created");
        }
        if (!(thing instanceof Bridge)) {
            throw new IllegalStateException("A bridge type is expected");
        }
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        final AbstractBrokerHandler handler;
        if (thingTypeUID.equals(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER)) {
            handler = new SystemBrokerHandler((Bridge) thing, mqttService);
        } else if (thingTypeUID.equals(MqttBindingConstants.BRIDGE_TYPE_BROKER)) {
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
     */
    @Override
    public void subscribe(MQTTTopicDiscoveryParticipant listener, String topic) {
        if (subscriber.containsKey(listener)) {
            return;
        }
        final TopicSubscribeMultiConnection multiSubscriber = new TopicSubscribeMultiConnection(listener, topic);
        handlers.forEach(multiSubscriber::add);
        subscriber.put(listener, multiSubscriber);
    }

    /**
     * Unsubscribe a listener from all available broker connections.
     */
    @Override
    @SuppressWarnings("null")
    public void unsubscribe(MQTTTopicDiscoveryParticipant listener) {
        TopicSubscribeMultiConnection multiSubscriber = subscriber.remove(listener);
        if (multiSubscriber != null) {
            multiSubscriber.stop();
        }
    }
}
