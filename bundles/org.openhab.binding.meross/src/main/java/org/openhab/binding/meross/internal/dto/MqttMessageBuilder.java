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
package org.openhab.binding.meross.internal.dto;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MD5Util;

import com.google.gson.Gson;

/**
 * The {@link MqttMessageBuilder} class is responsible for building MQTT
 * messages.
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Subscribe to messages asynchronously
 */
@NonNullByDefault
public class MqttMessageBuilder {
    public static @Nullable String brokerAddress;
    public static @Nullable String userId;
    public static @Nullable String clientId;
    public static @Nullable String key;

    /**
     * @param method The method
     * @param namespace The namespace
     * @param payload The payload
     * @return the message
     */
    public static byte[] buildMqttMessage(String method, String namespace, @Nullable String destinationDeviceUUID,
            Map<String, Object> payload) {
        int timestamp = Math.round(Instant.now().getEpochSecond());
        String randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String messageId = MD5Util.getMD5String(randomString.toLowerCase());
        String signatureToHash = "%s%s%d".formatted(messageId, key, timestamp);
        String signature = MD5Util.getMD5String(signatureToHash).toLowerCase();
        Map<String, @Nullable Object> headerMap = new HashMap<>();
        Map<String, Object> dataMap = new HashMap<>();
        headerMap.put("from", buildClientResponseTopic());
        headerMap.put("messageId", messageId);
        headerMap.put("method", method);
        headerMap.put("namespace", namespace);
        headerMap.put("payloadVersion", 1);
        headerMap.put("sign", signature);
        headerMap.put("timestamp", timestamp);
        headerMap.put("triggerSrc", "Android");
        headerMap.put("uuid", destinationDeviceUUID);
        dataMap.put("header", headerMap);
        dataMap.put("payload", payload);
        String jsonString = new Gson().toJson(dataMap);
        return StandardCharsets.UTF_8.encode(jsonString).array();
    }

    /**
     * In general, the API subscribes to this topic in order to update its state as events happen on the physical
     * device.
     *
     * @return The client user topic
     */
    public static String buildClientUserTopic() {
        return "/app/" + MqttMessageBuilder.userId + "/subscribe";
    }

    public static String buildAppId() {
        String randomString = "API" + UUID.randomUUID();
        String encodedString = StandardCharsets.UTF_8.encode(randomString).toString();
        return MD5Util.getMD5String(encodedString);
    }

    /**
     * API command.
     * It is the topic to which the Meross API subscribes. It is used by the app to receive the response to commands
     * sent to the appliance
     *
     * @return The response topic
     */
    public static String buildClientResponseTopic() {
        return "/app/" + MqttMessageBuilder.userId + "-" + buildAppId() + "/subscribe";
    }

    public static String buildClientId() {
        return "app:" + buildAppId();
    }

    /**
     * API command.
     *
     * @param deviceUUID The device UUID
     * @return The topic to be published
     */

    public static String buildDeviceRequestTopic(String deviceUUID) {
        return "/appliance/" + deviceUUID + "/subscribe";
    }

    public static void setUserId(String userId) {
        MqttMessageBuilder.userId = userId;
    }

    public static void setClientId(String clientId) {
        MqttMessageBuilder.clientId = clientId;
    }

    public static void setBrokerAddress(String brokerAddress) {
        MqttMessageBuilder.brokerAddress = brokerAddress;
    }

    public static void setKey(String key) {
        MqttMessageBuilder.key = key;
    }
}
