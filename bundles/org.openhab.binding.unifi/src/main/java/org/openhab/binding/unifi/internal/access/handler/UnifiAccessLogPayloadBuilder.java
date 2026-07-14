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
package org.openhab.binding.unifi.internal.access.handler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.access.dto.Notification.InsightLogsAddData;
import org.openhab.binding.unifi.internal.access.dto.Notification.LogsAddData;

import com.google.gson.Gson;

/**
 * Utility class that builds compact JSON payloads for log and insight notification triggers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessLogPayloadBuilder {

    /**
     * Builds a compact JSON payload from an {@link InsightLogsAddData} notification.
     *
     * @param gson the Gson instance used for serialization
     * @param data the insight log data
     * @return JSON string containing the non-null fields
     */
    public static String buildInsightPayload(Gson gson, InsightLogsAddData data) {
        String cameraId = data.metadata != null && data.metadata.cameraCapture != null
                && !data.metadata.cameraCapture.isEmpty() ? data.metadata.cameraCapture.get(0).alternateId : null;
        Map<String, Object> insight = new LinkedHashMap<>();
        putIfNonNull(insight, "logKey", data.logKey);
        putIfNonNull(insight, "eventType", data.eventType);
        putIfNonNull(insight, "message", data.message);
        putIfNonNull(insight, "published", data.published);
        putIfNonNull(insight, "result", data.result);
        String actorName = (data.metadata != null && data.metadata.actor != null) ? data.metadata.actor.displayName
                : null;
        putIfNonNull(insight, "actorName", actorName);
        String doorId = (data.metadata != null && data.metadata.door != null) ? data.metadata.door.id : null;
        putIfNonNull(insight, "doorId", doorId);
        String doorName = (data.metadata != null && data.metadata.door != null) ? data.metadata.door.displayName : null;
        putIfNonNull(insight, "doorName", doorName);
        String deviceId = (data.metadata != null && data.metadata.device != null && !data.metadata.device.isEmpty()
                && data.metadata.device.get(0) != null) ? data.metadata.device.get(0).id : null;
        putIfNonNull(insight, "deviceId", deviceId);
        putIfNonNull(insight, "cameraId", cameraId);
        return gson.toJson(insight);
    }

    /**
     * Builds a compact JSON payload from a {@link LogsAddData} notification.
     * Assumes {@code data} and {@code data.source} are non-null.
     *
     * @param gson the Gson instance used for serialization
     * @param data the log data (must have a non-null source)
     * @return JSON string containing the non-null fields
     */
    public static String buildLogPayload(Gson gson, LogsAddData data) {
        Map<String, Object> logMap = new LinkedHashMap<>();
        if (data.source.event != null) {
            putIfNonNull(logMap, "type", data.source.event.type);
            putIfNonNull(logMap, "displayMessage", data.source.event.displayMessage);
            putIfNonNull(logMap, "result", data.source.event.result);
            putIfNonNull(logMap, "published", data.source.event.published);
            putIfNonNull(logMap, "logKey", data.source.event.logKey);
            putIfNonNull(logMap, "logCategory", data.source.event.logCategory);
        }
        if (data.source.actor != null) {
            putIfNonNull(logMap, "actorName", data.source.actor.displayName);
        }
        if (data.source.authentication != null) {
            putIfNonNull(logMap, "credentialProvider", data.source.authentication.credentialProvider);
        }
        putIfNonNull(logMap, "doorName", targetDisplayName(data, "door"));
        putIfNonNull(logMap, "userStatus", targetDisplayName(data, "user_status"));
        return gson.toJson(logMap);
    }

    /**
     * Builds a compact JSON payload for an access attempt (success or failure) from a {@link LogsAddData} notification.
     *
     * @param gson the Gson instance used for serialization
     * @param data the log data (must have a non-null source)
     * @return JSON string containing the non-null fields
     */
    public static String buildAccessAttemptPayload(Gson gson, LogsAddData data) {
        Map<String, Object> accessMap = new LinkedHashMap<>();
        if (data.source.actor != null) {
            putIfNonNull(accessMap, "actorName", data.source.actor.displayName);
        }
        if (data.source.authentication != null) {
            putIfNonNull(accessMap, "credentialProvider", data.source.authentication.credentialProvider);
        }
        if (data.source.event != null) {
            putIfNonNull(accessMap, "message", data.source.event.displayMessage);
            putIfNonNull(accessMap, "result", data.source.event.result);
            putIfNonNull(accessMap, "published", data.source.event.published);
        }
        putIfNonNull(accessMap, "doorName", targetDisplayName(data, "door"));
        putIfNonNull(accessMap, "userStatus", targetDisplayName(data, "user_status"));
        return gson.toJson(accessMap);
    }

    /**
     * Extracts the {@code displayName} of the first {@code _source.target} entry matching the given
     * {@code type} (e.g. {@code "door"} or {@code "user_status"}). The UniFi Access log event carries
     * these as typed references in the target array rather than as dedicated fields.
     *
     * @param data the log data
     * @param type the target {@code type} to look for
     * @return the matching non-empty display name, or {@code null} if absent
     */
    private static @Nullable String targetDisplayName(LogsAddData data, String type) {
        if (data.source.target == null) {
            return null;
        }
        return data.source.target.stream().filter(t -> type.equalsIgnoreCase(t.type)).map(t -> t.displayName)
                .filter(v -> v != null && !v.isEmpty()).findFirst().orElse(null);
    }

    private static void putIfNonNull(Map<String, Object> map, String key, @Nullable Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
