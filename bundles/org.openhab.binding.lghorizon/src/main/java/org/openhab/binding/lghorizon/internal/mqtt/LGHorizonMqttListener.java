/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lghorizon.internal.mqtt;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * Callback for events coming from {@link LGHorizonMqttClient}.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public interface LGHorizonMqttListener {

    /**
     * Called for every JSON message received on any subscribed topic.
     *
     * @param topic the MQTT topic the message arrived on
     * @param payload the parsed JSON payload
     */
    void onMessage(String topic, JsonObject payload);

    /**
     * Called once the client has (re)connected and subscriptions have been (re)established.
     */
    void onConnected();

    /**
     * Called when the connection is lost. The client will attempt to reconnect on its own (fetching a fresh MQTT token
     * first); this is informational so the bridge handler can update its thing status.
     */
    void onConnectionLost(Throwable cause);
}
