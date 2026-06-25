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

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioEventProperty;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioZoneStatus;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudNetworkSettings;

import com.google.gson.annotations.SerializedName;

/**
 * {@link RachioEventGsonDTO} maps the API result into a Java object (using GSon).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioEventGsonDTO {
    private static final Set<String> LEGACY_NOTIFICATION_TYPES = Set.of("DELTA", "DEVICE_DELTA", "DEVICE_STATUS",
            "RAIN_DELAY", "RAIN_SENSOR_DETECTION", "SCHEDULE_DELTA", "SCHEDULE_STATUS", "WEATHER_INTELLIGENCE",
            "WATER_BUDGET", "ZONE_DELTA", "ZONE_STATUS");
    private static final Map<String, String> LEGACY_NOTIFICATION_TYPE_IDS = Map.of(WHE_DEVICE_STATUS, "DEVICE_STATUS",
            WHE_RAIN_DELAY, "RAIN_DELAY", WEATHER_INTELLIGENCE, "WEATHER_INTELLIGENCE", WHE_WATER_BUDGET,
            "WATER_BUDGET", WHE_SCHEDULE_STATUS, "SCHEDULE_STATUS", WHE_ZONE_STATUS, "ZONE_STATUS",
            WHE_RAIN_SENSOR_DETECTION, "RAIN_SENSOR_DETECTION", WHE_ZONE_DELTA, "ZONE_DELTA", WHE_DELTA, "DELTA");

    public String eventId = "";
    public String resourceId = "";
    public String resourceType = "";
    public String externalId = "";
    public String routingId = "";
    public String connectId = "";
    public String correlationId = "";
    public String scheduleId = "";
    public String deviceId = "";
    public String zoneId = "";
    public String id = "";

    public String timeZone = "";
    public String timestamp = "";
    public String timeForSummary = "";
    public String startTime = "";
    public String endTime = "";

    public long eventDate = -1;
    public long createDate = -1;
    public long lastUpdateDate = -1;
    public int sequence = -1;
    public String status = ""; // COLD_REBOOT: "status" :
                               // "coldReboot",

    /*
     * type : DEVICE_STATUS
     *
     * Subtype:
     *
     * OFFLINE
     * ONLINE
     * OFFLINE_NOTIFICATION
     * COLD_REBOOT
     * SLEEP_MODE_ON
     * SLEEP_MODE_OFF
     * BROWNOUT_VALVE
     * RAIN_SENSOR_DETECTION_ON
     * RAIN_SENSOR_DETECTION_OFF
     * RAIN_DELAY_ON
     * RAIN_DELAY_OFF
     *
     * Type : SCHEDULE_STATUS
     *
     * Subtype:
     *
     * SCHEDULE_STARTED
     * SCHEDULE_STOPPED
     * SCHEDULE_COMPLETED
     * WEATHER_INTELLIGENCE_NO_SKIP
     * WEATHER_INTELLIGENCE_SKIP
     * WEATHER_INTELLIGENCE_CLIMATE_SKIP
     * WEATHER_INTELLIGENCE_FREEZE
     *
     * Type : ZONE_STATUS
     *
     * Subtype:
     *
     * ZONE_STARTED
     * ZONE_STOPPED
     * ZONE_COMPLETED
     * ZONE_CYCLING
     * ZONE_CYCLING_COMPLETED
     *
     * Type : DEVICE_DELTA
     * Subtype : DEVICE_DELTA
     *
     * Type : ZONE_DELTA
     * Subtype : ZONE_DELTA
     *
     * Type : SCHEDULE_DELTA
     * Subtype : SCHEDULE_DELTA
     */
    public String type = "";
    @SerializedName(value = "subType", alternate = { "subtype", "sub_type", "eventSubType", "eventSubtype" })
    public String subType = "";
    public String eventType = "";
    public String category = "";
    public String topic = "";
    public String action = "";
    public String summary = "";
    public String description = "";
    public String title = "";
    public String pushTitle = "";

    public String icon = "";
    public String iconUrl = "";

    // ZONE_STATUS
    public @Nullable Integer zoneNumber = 0;
    public String zoneName = "";
    public Integer zoneCurrent = 0;
    public String zoneRunState = "";
    public Integer duration = 0;
    public Integer durationInMinutes = 0;
    public Integer flowVolume = 0;
    public @Nullable RachioZoneStatus zoneRunStatus;
    public @Nullable RachioWebhookPayload payload;

    // SCHEDULE_STATUS
    public String scheduleName = "";
    public String scheduleType = "";

    // COLD_REBOOT
    public String deviceName = ""; // "deviceName" : "My
                                   // Rachio",
    public @Nullable RachioCloudNetworkSettings network; // "network" : {}
    String pin = "";

    public RachioApiResult apiResult = new RachioApiResult();

    // public JsonArray eventDatas;
    public @Nullable HashMap<String, String> eventParms;
    public @Nullable HashMap<String, RachioEventProperty> deltaProperties;

    public RachioEventGsonDTO() {
    }

    public void normalize() {
        if (isLegacyEvent() || resourceType.isEmpty()) {
            return;
        }

        if ("IRRIGATION_CONTROLLER".equals(resourceType)) {
            deviceId = resourceId;
        }

        RachioWebhookPayload eventPayload = payload;
        if ("VALVE".equals(resourceType) && resourceId.isBlank() && eventPayload != null
                && !eventPayload.valveId.isBlank()) {
            resourceId = eventPayload.valveId;
        }
        if ("PROGRAM".equals(resourceType) && resourceId.isBlank() && eventPayload != null
                && !eventPayload.programId.isBlank()) {
            resourceId = eventPayload.programId;
        }

        if (timestamp.isEmpty()) {
            timestamp = createDate > 0 ? Long.toString(createDate) : "";
        }

        if (summary.isBlank()) {
            summary = eventType;
        }
        if (eventPayload != null) {
            startTime = firstNonBlank(eventPayload.startTime, startTime);
            endTime = firstNonBlank(eventPayload.endTime, endTime);
            int payloadDuration = eventPayload.getDurationSeconds();
            if (payloadDuration > 0) {
                duration = payloadDuration;
            }
            durationInMinutes = duration / 60;
        }

        switch (eventType) {
            case EVENT_DEVICE_ZONE_RUN_STARTED:
                normalizeZoneStatus(eventPayload, "ZONE_STARTED");
                break;
            case EVENT_DEVICE_ZONE_RUN_STOPPED:
                normalizeZoneStatus(eventPayload, "ZONE_STOPPED");
                break;
            case EVENT_DEVICE_ZONE_RUN_COMPLETED:
                normalizeZoneStatus(eventPayload, "ZONE_COMPLETED");
                break;
            case EVENT_DEVICE_ZONE_RUN_PAUSED:
                normalizeZoneStatus(eventPayload, "ZONE_CYCLING");
                break;
            case EVENT_SCHEDULE_STARTED:
                normalizeScheduleStatus(eventPayload, "SCHEDULE_STARTED");
                break;
            case EVENT_SCHEDULE_STOPPED:
                normalizeScheduleStatus(eventPayload, "SCHEDULE_STOPPED");
                break;
            case EVENT_SCHEDULE_COMPLETED:
                normalizeScheduleStatus(eventPayload, "SCHEDULE_COMPLETED");
                break;
            case EVENT_RAIN_SKIP:
            case EVENT_CLIMATE_SKIP:
            case EVENT_FREEZE_SKIP:
            case EVENT_WIND_SKIP:
                normalizeScheduleStatus(eventPayload, "WEATHER_INTELLIGENCE_SKIP");
                break;
            case EVENT_NO_SKIP:
                normalizeScheduleStatus(eventPayload, "WEATHER_INTELLIGENCE_NO_SKIP");
                break;
            case EVENT_RAIN_SENSOR_DETECTION_ON:
                type = "DEVICE_STATUS";
                subType = "RAIN_SENSOR_DETECTION_ON";
                category = "DEVICE";
                break;
            case EVENT_RAIN_SENSOR_DETECTION_OFF:
                type = "DEVICE_STATUS";
                subType = "RAIN_SENSOR_DETECTION_OFF";
                category = "DEVICE";
                break;
            case EVENT_RAIN_DELAY_ON:
                type = "DEVICE_STATUS";
                subType = "RAIN_DELAY_ON";
                category = "DEVICE";
                break;
            case EVENT_RAIN_DELAY_OFF:
                type = "DEVICE_STATUS";
                subType = "RAIN_DELAY_OFF";
                category = "DEVICE";
                break;
            default:
                type = eventType;
                subType = eventType;
                category = resourceType;
                break;
        }
    }

    public boolean isLegacyNotificationEvent() {
        return !isBlank(externalId) && !isBlank(deviceId) && isLegacyNotificationTypeRecognized();
    }

    public boolean hasStrongModernWebhookMarkers() {
        return !isBlank(eventId) || payload != null;
    }

    public boolean hasWeakModernWebhookHints() {
        return !isBlank(resourceId) || !isBlank(timestamp) || !isBlank(eventType) || !isBlank(resourceType);
    }

    public boolean hasModernWebhookMarkers() {
        return hasStrongModernWebhookMarkers() || hasWeakModernWebhookHints();
    }

    public void normalizeLegacyNotificationEvent() {
        String normalizedType = getLegacyNotificationType(type);
        if (normalizedType != null) {
            type = normalizedType;
        }
    }

    public boolean isLegacyNotificationTypeRecognized() {
        return getLegacyNotificationType(type) != null;
    }

    public String getLegacyNotificationTypeForLogging() {
        String normalizedType = getLegacyNotificationType(type);
        return normalizedType != null ? normalizedType : "unrecognized";
    }

    public int getZoneNumberForWebhookHandling() {
        RachioZoneStatus runStatus = zoneRunStatus;
        Integer runZoneNumber = runStatus != null ? runStatus.zoneNumber : null;
        if (runZoneNumber != null && runZoneNumber > 0) {
            return runZoneNumber;
        }
        Integer eventZoneNumber = zoneNumber;
        if (eventZoneNumber != null && eventZoneNumber > 0) {
            return eventZoneNumber;
        }
        return 0;
    }

    public String getZoneRunStateForWebhookHandling() {
        RachioZoneStatus runStatus = zoneRunStatus;
        if (runStatus != null && !isBlank(runStatus.state)) {
            return normalizeZoneRunState(runStatus.state);
        }
        if (!isBlank(zoneRunState)) {
            return normalizeZoneRunState(zoneRunState);
        }
        return normalizeZoneRunState(subType);
    }

    private String normalizeZoneRunState(String candidate) {
        String normalizedCandidate = candidate.trim().toUpperCase(Locale.ROOT);
        return switch (normalizedCandidate) {
            case "STARTED" -> "ZONE_STARTED";
            case "STOPPED" -> "ZONE_STOPPED";
            case "COMPLETED" -> "ZONE_COMPLETED";
            case "PAUSED", "CYCLING" -> "ZONE_CYCLING";
            case "RESUMED", "CYCLING_COMPLETED" -> "ZONE_CYCLING_COMPLETED";
            default -> normalizedCandidate;
        };
    }

    /**
     * @return rain delay seconds remaining from webhook timing data, or -1 when the event payload does not carry enough
     *         information to calculate it
     */
    public int getRainDelaySecondsRemaining() {
        int secondsUntilEnd = getSecondsUntil(endTime);
        if (secondsUntilEnd >= 0) {
            return secondsUntilEnd;
        }

        RachioWebhookPayload eventPayload = payload;
        if (eventPayload != null && !eventPayload.durationSeconds.isEmpty()) {
            return Math.max(0, eventPayload.getDurationSeconds());
        }
        return -1;
    }

    private static int getSecondsUntil(String timestamp) {
        if (timestamp.isEmpty()) {
            return -1;
        }
        try {
            long remainingMillis = Instant.parse(timestamp).toEpochMilli() - System.currentTimeMillis();
            return remainingMillis <= 0 ? 0 : (int) Math.min(Integer.MAX_VALUE, (remainingMillis + 999) / 1000);
        } catch (DateTimeParseException e) {
            return -1;
        }
    }

    private boolean isLegacyEvent() {
        return !type.isEmpty() || !subType.isEmpty() || !deviceId.isEmpty();
    }

    private static @Nullable String getLegacyNotificationType(@Nullable String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }
        String normalizedCandidate = candidate.trim().toUpperCase(Locale.ROOT);
        String mappedType = LEGACY_NOTIFICATION_TYPE_IDS.get(normalizedCandidate);
        if (mappedType != null) {
            return mappedType;
        }
        return LEGACY_NOTIFICATION_TYPES.contains(normalizedCandidate) ? normalizedCandidate : null;
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }

    private void normalizeZoneStatus(@Nullable RachioWebhookPayload eventPayload, String state) {
        type = "ZONE_STATUS";
        subType = state;
        category = "ZONE";
        if (eventPayload != null) {
            int payloadZoneNumber = eventPayload.getZoneNumber();
            if (payloadZoneNumber > 0) {
                zoneNumber = payloadZoneNumber;
            }
            zoneId = firstNonBlank(eventPayload.zoneId, zoneId);
            zoneName = firstNonBlank(eventPayload.zoneName, zoneName);
            flowVolume = eventPayload.getFlowVolumeGallons();
        }
        zoneRunState = state;

        RachioZoneStatus status = new RachioZoneStatus();
        status.duration = duration;
        status.zoneNumber = zoneNumber;
        status.state = state;
        status.scheduleType = eventPayload != null ? eventPayload.runType : "";
        status.startTime = startTime;
        status.endTime = endTime;
        zoneRunStatus = status;
    }

    private void normalizeScheduleStatus(@Nullable RachioWebhookPayload eventPayload, String status) {
        type = "SCHEDULE_STATUS";
        subType = status;
        category = "SCHEDULE";
        if (eventPayload != null) {
            scheduleId = firstNonBlank(eventPayload.scheduleId, scheduleId);
            scheduleName = firstNonBlank(eventPayload.scheduleName, scheduleName);
            scheduleType = firstNonBlank(eventPayload.runType, scheduleType);
            if (!eventPayload.plannedRunStartTime.isEmpty()) {
                startTime = eventPayload.plannedRunStartTime;
            }
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    public static class RachioWebhookPayload {
        public String durationSeconds = "";
        public String endTime = "";
        public String flowVolumeG = "";
        public String plannedRunStartTime = "";
        public String runType = "";
        public String scheduleId = "";
        public String scheduleName = "";
        public String startTime = "";
        @SerializedName(value = "zoneId", alternate = { "zone_id" })
        public String zoneId = "";
        public String zoneName = "";
        public String zoneNumber = "";
        public String endReason = "";
        public String programId = "";
        public String valveId = "";
        public @Nullable Boolean flowDetected;

        public int getDurationSeconds() {
            return parseInt(durationSeconds);
        }

        public int getFlowVolumeGallons() {
            return parseInt(flowVolumeG);
        }

        public int getZoneNumber() {
            return parseInt(zoneNumber);
        }

        public boolean getFlowDetected() {
            Boolean flowDetected = this.flowDetected;
            return flowDetected != null && flowDetected.booleanValue();
        }

        public boolean hasFlowDetected() {
            return flowDetected != null;
        }

        private static int parseInt(String value) {
            try {
                return value.isEmpty() ? 0 : Double.valueOf(value).intValue();
            } catch (RuntimeException e) {
                return 0;
            }
        }
    }
}
