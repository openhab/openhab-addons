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
package org.openhab.binding.mqtt.discovery;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;

/**
 * Represents a MQTT subscription for one specific topic. This is an immutable class.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class TopicSubscribe implements MqttMessageSubscriber {
    final MqttBrokerConnection connection;
    final ThingUID thing;
    final String topic;
    final MQTTTopicDiscoveryParticipant topicDiscoveredListener;

    /**
     * Creates a {@link TopicSubscribe} object.
     *
     * @param connection The broker connection
     * @param topic The topic
     * @param topicDiscoveredListener A listener
     * @param thing A thing, used as an argument to the listener callback.
     */
    public TopicSubscribe(MqttBrokerConnection connection, String topic,
            MQTTTopicDiscoveryParticipant topicDiscoveredListener, ThingUID thing) {
        this.connection = connection;
        this.thing = thing;
        this.topic = topic;
        this.topicDiscoveredListener = topicDiscoveredListener;
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        if (payload.length > 0) {
            topicDiscoveredListener.receivedMessage(thing, connection, topic, payload);
        } else {
            topicDiscoveredListener.topicVanished(thing, connection, topic);
        }
    }

    /**
     * Subscribe to the topic
     *
     * @return Completes with true if successful. Completes with false if not connected yet. Exceptionally otherwise.
     */
    public CompletableFuture<Boolean> start() {
        return connection.subscribe(topic, this);
    }

    /**
     * Unsubscribes from the topic
     *
     * @return Completes with true if successful. Exceptionally otherwise.
     */
    public CompletableFuture<Boolean> stop() {
        return connection.unsubscribe(topic, this);
    }
}
