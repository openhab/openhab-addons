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
    private @Nullable String userId;
    private volatile @Nullable String appId;
    private @Nullable String key;

    /**
     * @param method The method
     * @param namespace The namespace
     * @param payload The payload
     * @return the message
     */
    public byte[] buildMqttMessage(String method, String namespace, @Nullable String destinationDeviceUUID,
            Map<String, Object> payload) {
        long timestamp = Instant.now().getEpochSecond();
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
        return jsonString.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * In general, the API subscribes to this topic in order to update its state as events happen on the physical
     * device.
     *
     * @return The client user topic
     */
    public String buildClientUserTopic() {
        return "/app/" + userId + "/subscribe";
    }

    public synchronized String getAppId() {
        String appId = this.appId;
        if (appId == null) {
            String randomString = "API" + UUID.randomUUID();
            this.appId = appId = MD5Util.getMD5String(randomString);
        }
        return appId;
    }

    /**
     * API command.
     * It is the topic to which the Meross API subscribes. It is used by the app to receive the response to commands
     * sent to the appliance
     *
     * @return The response topic
     */
    public String buildClientResponseTopic() {
        return "/app/" + userId + "-" + getAppId() + "/subscribe";
    }

    public String getClientId() {
        return "app:" + getAppId();
    }

    /**
     * API command.
     *
     * @param deviceUUID The device UUID
     * @return The topic to be published
     */

    public String buildDeviceRequestTopic(String deviceUUID) {
        return "/appliance/" + deviceUUID + "/subscribe";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
