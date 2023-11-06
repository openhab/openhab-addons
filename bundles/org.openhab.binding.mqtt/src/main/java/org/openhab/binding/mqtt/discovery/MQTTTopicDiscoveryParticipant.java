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
package org.openhab.binding.mqtt.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ThingUID;

/**
 * Implement this interface to get notified of received values and vanished topics.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface MQTTTopicDiscoveryParticipant {
    /**
     * Called whenever a message on the subscribed topic got published or a retained message was received.
     *
     * @param thingUID The MQTT thing UID of the Thing that established/created the given broker connection.
     * @param connection The broker connection
     * @param topic The topic
     * @param payload The topic payload
     */
    void receivedMessage(ThingUID thingUID, MqttBrokerConnection connection, String topic, byte[] payload);

    /**
     * A MQTT topic vanished.
     *
     * @param thingUID The MQTT thing UID of the Thing that established/created the given broker connection.
     * @param connection The broker connection
     * @param topic The topic
     */
    void topicVanished(ThingUID thingUID, MqttBrokerConnection connection, String topic);
}
