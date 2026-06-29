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
package org.openhab.binding.rachio.internal.api.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * {@link RachioApiGsonDTO} maps some API results to a Java object (using GSon).
 *
 * @author Markus Michels - Initial contribution
 */
public class RachioApiGsonDTO {
    public static class RachioCloudPersonId {
        public String id = ""; // "id":"xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx"
    }

    public static class RachioCloudStatus {
        public long createDate = -1; // "createDate":1494626927000,
        public String id = ""; // "id":"xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx",
        public String username = ""; // "username":"openhab",
        public String fullName = ""; // "fullName":"openHAB user",
        public String email = ""; // "email":info@openhab.info",
        public ArrayList<RachioCloudDevice> devices = new ArrayList<>(); // "devices":[]
        public boolean deleted = false; // "deleted":false
    }

    public static class RachioApiWebHookEntry {
        public long createDate = -1;
        public long lastUpdateDate = -1;
        public String id = "";
        public String url = "";
        public String externalId = "";
        public RachioApiWebHookResourceId resourceId = new RachioApiWebHookResourceId();
        public ArrayList<String> eventTypes = new ArrayList<>();
    }

    public static class RachioApiWebHookList {
        public ArrayList<RachioApiWebHookEntry> webhooks = new ArrayList<>();
    }

    public static class RachioApiLegacyWebHookEventType {
        public String id = "";
        public String type = "";
        public String description = "";
    }

    public static class RachioApiWebHookResourceId {
        public String irrigationControllerId = "";
        public String valveId = "";
        public String programId = "";
        public String lightingControllerId = "";
        public String lightingZoneId = "";
        public String lightingSceneId = "";
        public String lightingProgramId = "";

        public String getResourceId(RachioWebhookResourceType resourceType) {
            switch (resourceType) {
                case IRRIGATION_CONTROLLER:
                    return irrigationControllerId;
                case VALVE:
                    return valveId;
                case PROGRAM:
                    return programId;
                case LIGHTING_CONTROLLER:
                    return lightingControllerId;
                case LIGHTING_ZONE:
                    return lightingZoneId;
                case LIGHTING_SCENE:
                    return lightingSceneId;
                case LIGHTING_PROGRAM:
                    return lightingProgramId;
                case UNKNOWN:
                default:
                    return "";
            }
        }
    }

    public static class RachioApiWebhookEventTypesResponse {
        public ArrayList<RachioApiWebhookEventTypeGroup> eventTypes = new ArrayList<>();

        public static RachioApiWebhookEventTypesResponse fromJson(String json) {
            RachioApiWebhookEventTypesResponse response = new RachioApiWebhookEventTypesResponse();
            JsonElement root = JsonParser.parseString(json);
            if (root.isJsonArray()) {
                response.eventTypes.addAll(parseGroups(root.getAsJsonArray()));
            } else if (root.isJsonObject()) {
                JsonObject object = root.getAsJsonObject();
                for (String arrayName : List.of("eventTypes", "items", "data", "results")) {
                    JsonElement arrayElement = object.get(arrayName);
                    if (arrayElement != null && arrayElement.isJsonArray()) {
                        response.eventTypes.addAll(parseGroups(arrayElement.getAsJsonArray()));
                    }
                }
            }
            return response;
        }

        public Map<RachioWebhookResourceType, Set<String>> toResourceEventTypeMap() {
            Map<RachioWebhookResourceType, Set<String>> eventTypesByResourceType = new LinkedHashMap<>();
            for (RachioApiWebhookEventTypeGroup group : eventTypes) {
                RachioWebhookResourceType resourceType = RachioWebhookResourceType.fromApiValue(group.resourceType);
                if (resourceType == RachioWebhookResourceType.UNKNOWN && group.eventTypes.isEmpty()) {
                    continue;
                }
                Set<String> resourceEventTypes = eventTypesByResourceType.get(resourceType);
                if (resourceEventTypes == null) {
                    resourceEventTypes = new LinkedHashSet<>();
                    eventTypesByResourceType.put(resourceType, resourceEventTypes);
                }
                resourceEventTypes.addAll(group.eventTypes);
            }
            return eventTypesByResourceType;
        }

        private static List<RachioApiWebhookEventTypeGroup> parseGroups(JsonArray entries) {
            List<RachioApiWebhookEventTypeGroup> groups = new ArrayList<>();
            RachioApiWebhookEventTypeGroup flatGroup = new RachioApiWebhookEventTypeGroup();
            for (JsonElement entry : entries) {
                if (entry == null) {
                    continue;
                }
                if (entry.isJsonPrimitive()) {
                    flatGroup.eventTypes.add(entry.getAsString());
                } else if (entry.isJsonObject()) {
                    RachioApiWebhookEventTypeGroup group = parseGroup(entry.getAsJsonObject());
                    if (!group.eventTypes.isEmpty()) {
                        groups.add(group);
                    } else {
                        String eventType = readEventType(entry.getAsJsonObject());
                        if (!eventType.isBlank()) {
                            flatGroup.eventTypes.add(eventType);
                        }
                    }
                }
            }
            if (!flatGroup.eventTypes.isEmpty()) {
                groups.add(flatGroup);
            }
            return groups;
        }

        private static RachioApiWebhookEventTypeGroup parseGroup(JsonObject object) {
            RachioApiWebhookEventTypeGroup group = new RachioApiWebhookEventTypeGroup();
            group.resourceType = readString(object, "resourceType");
            JsonElement eventTypes = object.get("eventTypes");
            if (eventTypes != null && eventTypes.isJsonArray()) {
                for (JsonElement entry : eventTypes.getAsJsonArray()) {
                    if (entry != null && entry.isJsonPrimitive()) {
                        group.eventTypes.add(entry.getAsString());
                    }
                }
            }
            return group;
        }
    }

    public static class RachioApiWebhookEventTypeGroup {
        public String resourceType = "";
        public ArrayList<String> eventTypes = new ArrayList<>();
    }

    public static class RachioEventProperty {
        public String propertyName = "";
        public String oldValue = "";
        public String newValue = "";
    }

    public static class RachioZoneStatus {
        public Integer duration = 0;
        public String scheduleType = "";
        public @Nullable Integer zoneNumber = 0;
        public String executionType = "";
        public String state = "";
        public String startTime = "";
        public String endTime = "";
        // public Integer corId = 0; // currently unused
        // public Integer seqId = 0; // currently unused
        // public Integer ix = 0; // currently unused
    }

    public static class RachioCloudDelta {
        // V3: ZONE_DELTA / SCHEDULE_DELTA
        public String routingId = ""; // "routingId" : "d3beb3ab-b85a-49fe-a45d-37c4d95ea9a8",
        public String icon = ""; // "icon" : "NO_ICON",
        public String action = ""; // "action" : "UPDATED",
        public String zoneId = ""; // "zoneId" : "e49c8b55-a553-4733-b1cf-0e402b97db49",
        public String externalId = ""; // "externalId" : "cc765dfb-d095-4ceb-8062-b9d88dcce911",
        public String subType = ""; // "subType" : "ZONE_DELTA",
        public String id = ""; // "id" : "e9d4fa9f-1619-37c4-b457-3845620643d2",
        public String type = ""; // "type" : "DELTA",
        public String category = ""; // "category" : "DEVICE",
        public String deviceId = ""; // "deviceId" : "d3beb3ab-b85a-49fe-a45d-37c4d95ea9a8",
        public String timestamp = ""; // "timestamp" : "2018-04-09T23:17:14.365Z"
    }

    private static String readEventType(JsonObject object) {
        String eventType = readString(object, "eventType");
        if (!eventType.isBlank()) {
            return eventType;
        }
        eventType = readString(object, "name");
        if (!eventType.isBlank()) {
            return eventType;
        }
        return readString(object, "type");
    }

    private static String readString(JsonObject object, String memberName) {
        JsonElement element = object.get(memberName);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }
}
