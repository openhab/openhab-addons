/**
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
package org.openhab.binding.ferroamp.internal.api;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ferroamp.internal.FerroampBindingConstants;
//import org.openhab.binding.ferroamp.internal.config.ChannelMapping;
//import org.openhab.binding.ferroamp.internal.handler.FerroampHandler;
//import org.openhab.binding.ferroamp.internal.handler.StateHelper;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
//import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FerroampMqttCommunication} is responsible for communication with Ferroamp-system's Mqtt-broker.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class FerroampMqttCommunication implements MqttMessageSubscriber {

    private @Nullable FerroAmpUpdateListener ferroAmpUpdateListeners;

    private final static Logger logger = LoggerFactory.getLogger(FerroampMqttCommunication.class);
    private final MqttBrokerConnection ferroampConnection;

    public FerroampMqttCommunication(String username, String password, String host, int port) {
        super();
        ferroampConnection = new MqttBrokerConnection(host, port, false, false, username);
        ferroampConnection.setCredentials(username, password);

    }

    public void start() {
        ferroampConnection.start();
        ferroampConnection.subscribe(FerroampBindingConstants.EHUB_TOPIC, this);
        ferroampConnection.subscribe(FerroampBindingConstants.SSO_TOPIC, this);
        ferroampConnection.subscribe(FerroampBindingConstants.ESO_TOPIC, this);
        ferroampConnection.subscribe(FerroampBindingConstants.ESM_TOPIC, this);

    }

    public void registerFerroAmpUpdateListener(FerroAmpUpdateListener listener) {
        ferroAmpUpdateListeners = listener;
    }

    public void unregisterFerroAmpUpdateListener() {
        ferroAmpUpdateListeners = null;
    }

    // Handles request topic
    public void sendPublishedTopic(String payload) {
        ferroampConnection.publish(FerroampBindingConstants.REQUEST_TOPIC, payload.getBytes(), 1, false);
    }

    // Capture actual Json-topic message
    @Override
    public void processMessage(String topic, byte[] payload) {
        DataType type = DataType.UNKNOWN;
        Map<String, @Nullable String> keyValueMap = new HashMap<>();
        String message = new String(payload, StandardCharsets.UTF_8);

        if (FerroampBindingConstants.EHUB_TOPIC.equals(topic)) {
            keyValueMap = extractKeyValuePairs(message, null);
            type = DataType.EHUB;
        } else if (FerroampBindingConstants.SSO_TOPIC.equals(topic)) {
            keyValueMap = processIncomingJsonMessageSso(new String(payload, StandardCharsets.UTF_8));
            type = DataType.SSO;
        } else if (FerroampBindingConstants.ESO_TOPIC.equals(topic)) {
            keyValueMap = extractKeyValuePairs(message, null);
            type = DataType.ESO;
        } else if (FerroampBindingConstants.ESM_TOPIC.equals(topic)) {
            keyValueMap = extractKeyValuePairs(message, null);
            type = DataType.ESM;
        } else {
            logger.warn("Received message on unknown topic: {}", topic);
        }
        FerroAmpUpdateListener ferroAmpUpdateListeners = this.ferroAmpUpdateListeners;
        if (ferroAmpUpdateListeners != null && type != DataType.UNKNOWN) {
            ferroAmpUpdateListeners.onFerroAmpUpdate(type, keyValueMap);
        }
    }

    // Prepare actual Json-topic Ehub-message and update values for channels
    private Map<String, @Nullable String> extractKeyValuePairs(String json, @Nullable Integer deviceIndex) {
        Map<String, @Nullable String> result = new ConcurrentHashMap<>();
        JsonArray arr;
        JsonObject obj;
        try {
            arr = JsonParser.parseString(json).getAsJsonArray();
            if (deviceIndex != null && deviceIndex > -1 && deviceIndex < arr.size()) {
                obj = arr.get(deviceIndex).getAsJsonObject();
            } else {
                obj = JsonParser.parseString(json).getAsJsonObject();
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Failed to parse JSON: {}", e.getMessage());
            return Collections.emptyMap();
        } catch (IllegalStateException e) {
            logger.debug("Failed to parse JSON: {}", e.getMessage());
            obj = JsonParser.parseString(json).getAsJsonObject();
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Device index {} out of bounds for JSON array: {}", deviceIndex, e.getMessage());
            return Collections.emptyMap();
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonObject valueObj = entry.getValue().getAsJsonObject();
            // Prefer "val" if present
            if (valueObj.has("val")) {
                result.put(key, valueObj.get("val").getAsString());
            }

            // Handle phase keys (L1, L2, L3)
            for (String phase : new String[] { "L1", "L2", "L3" }) {
                if (valueObj.has(phase)) {
                    result.put(key + "." + phase, valueObj.get(phase).getAsString());
                }
            }

            // Handle "pos" and "neg"
            if (valueObj.has("pos")) {
                result.put(key + ".pos", valueObj.get("pos").getAsString());
            }
            if (valueObj.has("neg")) {
                result.put(key + ".neg", valueObj.get("neg").getAsString());
            }
        }

        return result;
    }

    // Prepare actual Json-topic Sso-messages and update values for channels
    private Map<String, @Nullable String> processIncomingJsonMessageSso(String messageJsonSso) {

        messageJsonSso = "[" + messageJsonSso + "]";

        JsonArray ssoArray = JsonParser.parseString(messageJsonSso).getAsJsonArray();
        Map<String, @Nullable String> keyValueMap = new HashMap<>();
        for (int ssoIndex = 0; ssoIndex < ssoArray.size(); ssoIndex++) {
            Map<String, @Nullable String> extracted = extractKeyValuePairs(messageJsonSso, ssoIndex);
            for (Map.Entry<String, @Nullable String> entry : extracted.entrySet()) {
                keyValueMap.put(ssoIndex + "-" + entry.getKey(), entry.getValue());
            }
        }
        return keyValueMap;
    }

    public void stop() {
        ferroampConnection.unsubscribe(FerroampBindingConstants.EHUB_TOPIC, this);
        ferroampConnection.unsubscribe(FerroampBindingConstants.SSO_TOPIC, this);
        ferroampConnection.unsubscribe(FerroampBindingConstants.ESO_TOPIC, this);
        ferroampConnection.unsubscribe(FerroampBindingConstants.ESM_TOPIC, this);
        ferroampConnection.stop();
    }

    public void dispose() {
        stop();
    }

    public boolean isConnected() {
        return !MqttConnectionState.DISCONNECTED.equals(ferroampConnection.connectionState());
    }
}