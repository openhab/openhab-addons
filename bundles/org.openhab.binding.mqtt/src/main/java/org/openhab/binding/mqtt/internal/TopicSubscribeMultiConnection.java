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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryParticipant;
import org.openhab.binding.mqtt.discovery.TopicSubscribe;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this for MQTT topic subscriptions on all available broker connections.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class TopicSubscribeMultiConnection {
    private final Logger logger = LoggerFactory.getLogger(TopicSubscribeMultiConnection.class);
    protected final Map<ThingUID, TopicSubscribe> observedBrokerHandlers = new HashMap<>();
    protected final MQTTTopicDiscoveryParticipant messageReceivedListener;
    protected final String topic;

    /**
     * Creates a topic subscription object.
     *
     * @param messageReceivedListener A callback to get notified of results.
     * @param topic A topic, most likely with a wildcard like this: "house/+/main-light" to match
     *            "house/room1/main-light", "house/room2/main-light" etc.
     */
    public TopicSubscribeMultiConnection(MQTTTopicDiscoveryParticipant messageReceivedListener, String topic) {
        this.messageReceivedListener = messageReceivedListener;
        this.topic = topic;
    }

    /**
     * Add thing if it is a bridge and has a handler that inherits from {@link AbstractBrokerHandler}.
     */
    public void add(AbstractBrokerHandler handler) {
        final ThingUID bridgeUid = handler.getThing().getUID();

        handler.getConnectionAsync().thenAccept(connection -> {
            final TopicSubscribe o = new TopicSubscribe(connection, topic, messageReceivedListener, bridgeUid);
            observedBrokerHandlers.put(bridgeUid, o);
            o.start().exceptionally(e -> {
                logger.warn("Failed to MQTT subscribe for {} on topic {}", bridgeUid, topic);
                return false;
            }).thenRun(() -> {
                logger.trace("Found suitable bridge {} for listing to topic {}", bridgeUid, topic);
            });
        });
    }

    /**
     * Removes the thing from observed connections, if it exists in there, and stops any MQTT subscriptions.
     */
    @SuppressWarnings("null")
    public void remove(AbstractBrokerHandler handler) {
        final TopicSubscribe observedBrokerHandler = observedBrokerHandlers.remove(handler.getThing().getUID());
        if (observedBrokerHandler != null) {
            observedBrokerHandler.stop();
        }
    }

    /**
     * Unsubscribes from the topic on all connections.
     *
     * @return Completes with true if successful. Exceptionally otherwise.
     */
    public CompletableFuture<Boolean> stop() {
        return observedBrokerHandlers.values().stream().map(v -> v.stop())
                .reduce(CompletableFuture.completedFuture(true), (a, v) -> a.thenCompose(b -> v));
    }
}
