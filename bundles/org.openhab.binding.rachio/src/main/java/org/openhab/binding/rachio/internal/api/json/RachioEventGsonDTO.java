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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioEventProperty;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioZoneStatus;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudNetworkSettings;

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
    public Integer zoneNumber = 0;
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

        if (eventPayload == null) {
            return;
        }

        summary = eventType;
        startTime = eventPayload.startTime;
        endTime = eventPayload.endTime;
        duration = eventPayload.getDurationSeconds();
        durationInMinutes = duration / 60;

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
        return eventType.isBlank() && resourceType.isBlank() && !externalId.isBlank() && !deviceId.isBlank()
                && !subType.isBlank() && LEGACY_NOTIFICATION_TYPES.contains(type);
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

    private void normalizeZoneStatus(RachioWebhookPayload eventPayload, String state) {
        type = "ZONE_STATUS";
        subType = state;
        category = "ZONE";
        zoneNumber = eventPayload.getZoneNumber();
        zoneName = eventPayload.zoneName;
        zoneRunState = state;
        flowVolume = eventPayload.getFlowVolumeGallons();

        RachioZoneStatus status = new RachioZoneStatus();
        status.duration = duration;
        status.zoneNumber = zoneNumber;
        status.state = state;
        status.scheduleType = eventPayload.runType;
        status.startTime = startTime;
        status.endTime = endTime;
        zoneRunStatus = status;
    }

    private void normalizeScheduleStatus(RachioWebhookPayload eventPayload, String status) {
        type = "SCHEDULE_STATUS";
        subType = status;
        category = "SCHEDULE";
        scheduleId = eventPayload.scheduleId;
        scheduleName = eventPayload.scheduleName;
        scheduleType = eventPayload.runType;
        if (!eventPayload.plannedRunStartTime.isEmpty()) {
            startTime = eventPayload.plannedRunStartTime;
        }
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
