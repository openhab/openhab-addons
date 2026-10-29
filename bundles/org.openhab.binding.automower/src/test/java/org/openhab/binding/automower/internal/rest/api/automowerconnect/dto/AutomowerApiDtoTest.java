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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class AutomowerApiDtoTest {
    private final Gson gson = new Gson();

    @Test
    void mowerResponseDeserializesOpenApiAggregate() {
        String json = """
                {
                    "data": {
                        "type": "mowers",
                        "id": "mower-1",
                        "attributes": {
                            "system": {
                                "name": "Automower",
                                "model": "435X AWD",
                                "serialNumber": "ABC123"
                            },
                            "battery": {
                                "batteryPercent": 87
                            },
                            "capabilities": {
                                "canConfirmError": true,
                                "headlights": true,
                                "position": true,
                                "stayOutZones": true,
                                "workAreas": true
                            },
                            "mower": {
                                "mode": "MAIN_AREA",
                                "activity": "MOWING",
                                "inactiveReason": "NONE",
                                "state": "IN_OPERATION",
                                "workAreaId": 17746,
                                "errorCode": 0,
                                "errorCodeTimestamp": 1720000000,
                                "isErrorConfirmable": false
                            },
                            "calendar": {
                                "tasks": [
                                    {
                                        "start": 480,
                                        "duration": 389,
                                        "monday": true,
                                        "tuesday": false,
                                        "wednesday": true,
                                        "thursday": false,
                                        "friday": true,
                                        "saturday": false,
                                        "sunday": false,
                                        "workAreaId": 17746
                                    }
                                ]
                            },
                            "planner": {
                                "nextStartTimestamp": 1720000000,
                                "restrictedReason": "NONE",
                                "override": {
                                    "action": "NOT_ACTIVE"
                                },
                                "externalReason": 0
                            },
                            "metadata": {
                                "connected": true,
                                "statusTimestamp": 1720000000
                            },
                            "positions": [
                                {
                                    "latitude": 49.4886,
                                    "longitude": 11.4403
                                }
                            ],
                            "settings": {
                                "cuttingHeight": 5,
                                "headlight": {
                                    "mode": "EVENING_ONLY"
                                }
                            },
                            "statistics": {
                                "cuttingBladeUsageTime": 100,
                                "downTime": 200,
                                "numberOfChargingCycles": 3,
                                "numberOfCollisions": 4,
                                "totalChargingTime": 500,
                                "totalCuttingTime": 600,
                                "totalDrivenDistance": 700,
                                "totalRunningTime": 800,
                                "totalSearchingTime": 900,
                                "upTime": 1000
                            },
                            "stayOutZones": {
                                "dirty": false,
                                "zones": [
                                    {
                                        "id": "zone-1",
                                        "name": "Flower bed",
                                        "enabled": true
                                    }
                                ]
                            },
                            "workAreas": [
                                {
                                    "workAreaId": 17746,
                                    "name": "Autumn",
                                    "type": "RANDOM",
                                    "cuttingHeight": 5,
                                    "enabled": true,
                                    "schedulable": true,
                                    "useGlobalCuttingHeight": false,
                                    "lastTimeAbandoned": 1720000000,
                                    "progress": 25,
                                    "lastTimeCompleted": 1720000100,
                                    "orientation": 45,
                                    "orientationShift": 10
                                }
                            ]
                        }
                    }
                }
                """;

        MowerResult result = gson.fromJson(json, MowerResult.class);
        assertNotNull(result);
        Mower mower = result.getData();
        assertNotNull(mower);

        assertEquals("mowers", mower.getType());
        assertEquals("mower-1", mower.getId());
        assertEquals("435X AWD", mower.getAttributes().getSystem().getModel());
        assertEquals(87, mower.getAttributes().getBattery().getBatteryPercent());
        assertTrue(mower.getAttributes().getCapabilities().hasWorkAreas());
        assertEquals(Activity.MOWING, mower.getAttributes().getMower().getActivity());
        assertEquals(480, mower.getAttributes().getCalendar().getTasks().get(0).getStart());
        assertEquals(Action.NOT_ACTIVE, mower.getAttributes().getPlanner().getOverride().getAction());
        assertTrue(mower.getAttributes().getMetadata().isConnected());
        assertEquals(49.4886, mower.getAttributes().getLastPosition().getLatitude());
        assertEquals(HeadlightMode.EVENING_ONLY, mower.getAttributes().getSettings().getHeadlight().getHeadlightMode());
        assertEquals(700, mower.getAttributes().getStatistics().getTotalDriveDistance());
        assertEquals("zone-1", mower.getAttributes().getStayOutZones().getZones().get(0).getId());
        assertEquals(Byte.valueOf((byte) 25), mower.getAttributes().getWorkAreas().get(0).getProgress());
    }

    @Test
    void calendarRequestSerializesOpenApiTaskFields() {
        CalendarTask task = new CalendarTask();
        task.setStart((short) 480);
        task.setDuration((short) 389);
        task.setMonday(true);
        task.setWednesday(true);
        task.setFriday(true);
        task.setWorkAreaId(17746L);

        Calendar calendar = new Calendar();
        calendar.setTasks(List.of(task));
        MowerCalendar mowerCalendar = new MowerCalendar();
        mowerCalendar.setType("calendar");
        mowerCalendar.setAttributes(calendar);
        MowerCalendardRequest request = new MowerCalendardRequest();
        request.setData(mowerCalendar);

        JsonObject json = JsonParser.parseString(gson.toJson(request)).getAsJsonObject();
        JsonObject data = json.getAsJsonObject("data");
        JsonObject serializedTask = data.getAsJsonObject("attributes").getAsJsonArray("tasks").get(0).getAsJsonObject();

        assertEquals("calendar", data.get("type").getAsString());
        assertEquals(480, serializedTask.get("start").getAsInt());
        assertEquals(389, serializedTask.get("duration").getAsInt());
        assertTrue(serializedTask.get("monday").getAsBoolean());
        assertTrue(serializedTask.get("wednesday").getAsBoolean());
        assertTrue(serializedTask.get("friday").getAsBoolean());
        assertEquals(17746L, serializedTask.get("workAreaId").getAsLong());
    }

    @Test
    void settingsRequestSerializesOpenApiFields() {
        Settings settings = new Settings();
        settings.setCuttingHeight((byte) 7);
        Headlight headlight = new Headlight();
        headlight.setHeadlightMode(HeadlightMode.ALWAYS_ON);
        settings.setHeadlight(headlight);

        MowerSettings mowerSettings = new MowerSettings();
        mowerSettings.setType("settings");
        mowerSettings.setAttributes(settings);
        MowerSettingsRequest request = new MowerSettingsRequest();
        request.setData(mowerSettings);

        JsonObject json = JsonParser.parseString(gson.toJson(request)).getAsJsonObject();
        JsonObject data = json.getAsJsonObject("data");
        JsonObject attributes = data.getAsJsonObject("attributes");

        assertEquals("settings", data.get("type").getAsString());
        assertEquals(7, attributes.get("cuttingHeight").getAsInt());
        assertEquals("ALWAYS_ON", attributes.getAsJsonObject("headlight").get("mode").getAsString());
    }

    @Test
    void stayOutZonePatchSerializesOnlyEnableAttribute() {
        MowerStayOutZoneAttributes attributes = new MowerStayOutZoneAttributes();
        attributes.setEnable(false);
        MowerStayOutZone zone = new MowerStayOutZone();
        zone.setType("stayOutZone");
        zone.setId("zone-1");
        zone.setAttributes(attributes);
        MowerStayOutZoneRequest request = new MowerStayOutZoneRequest();
        request.setData(zone);

        JsonObject json = JsonParser.parseString(gson.toJson(request)).getAsJsonObject();
        JsonObject data = json.getAsJsonObject("data");
        JsonObject serializedAttributes = data.getAsJsonObject("attributes");

        assertEquals("stayOutZone", data.get("type").getAsString());
        assertEquals("zone-1", data.get("id").getAsString());
        assertFalse(serializedAttributes.get("enable").getAsBoolean());
        assertEquals(1, serializedAttributes.size());
    }

    @Test
    void commandRequestSerializesSupportedVariants() {
        for (String commandType : List.of("Start", "StartInWorkArea", "ResumeSchedule", "Pause", "Park",
                "ParkUntilNextSchedule", "ParkUntilFurtherNotice")) {
            MowerCommand command = new MowerCommand();
            command.setType(commandType);
            MowerCommandRequest request = new MowerCommandRequest();
            request.setData(command);

            JsonObject data = JsonParser.parseString(gson.toJson(request)).getAsJsonObject().getAsJsonObject("data");
            assertEquals(commandType, data.get("type").getAsString());
            assertFalse(data.has("attributes"));
        }

        MowerCommandAttributes attributes = new MowerCommandAttributes();
        attributes.setDuration(30L);
        attributes.setWorkAreaId(17746L);
        MowerCommand command = new MowerCommand();
        command.setType("StartInWorkArea");
        command.setAttributes(attributes);
        MowerCommandRequest request = new MowerCommandRequest();
        request.setData(command);

        JsonObject serializedAttributes = JsonParser.parseString(gson.toJson(request)).getAsJsonObject()
                .getAsJsonObject("data").getAsJsonObject("attributes");
        assertEquals(30L, serializedAttributes.get("duration").getAsLong());
        assertEquals(17746L, serializedAttributes.get("workAreaId").getAsLong());

        MowerCommandAttributes parkAttributes = new MowerCommandAttributes();
        parkAttributes.setDuration(30L);
        parkAttributes.setExternalReason(200001L);
        MowerCommand parkCommand = new MowerCommand();
        parkCommand.setType("Park");
        parkCommand.setAttributes(parkAttributes);
        MowerCommandRequest parkRequest = new MowerCommandRequest();
        parkRequest.setData(parkCommand);

        JsonObject parkSerializedAttributes = JsonParser.parseString(gson.toJson(parkRequest)).getAsJsonObject()
                .getAsJsonObject("data").getAsJsonObject("attributes");
        assertEquals(30L, parkSerializedAttributes.get("duration").getAsLong());
        assertEquals(200001L, parkSerializedAttributes.get("externalReason").getAsLong());
    }

    @Test
    void workAreaPatchContainsOnlyChangedAttributes() {
        MowerWorkAreaAttributes attributes = new MowerWorkAreaAttributes();
        attributes.setName("Autumn");
        attributes.setOrientation(45);
        attributes.setOrientationShift(10);

        JsonObject json = JsonParser.parseString(gson.toJson(attributes)).getAsJsonObject();

        assertEquals("Autumn", json.get("name").getAsString());
        assertEquals(45, json.get("orientation").getAsInt());
        assertEquals(10, json.get("orientationShift").getAsInt());
        assertFalse(json.has("enable"));
        assertFalse(json.has("cuttingHeight"));
    }

    @Test
    void commandAttributesSerializeExternalReason() {
        MowerCommandAttributes attributes = new MowerCommandAttributes();
        attributes.setDuration(1500L);
        attributes.setExternalReason(200001L);

        JsonObject json = JsonParser.parseString(gson.toJson(attributes)).getAsJsonObject();

        assertEquals(1500L, json.get("duration").getAsLong());
        assertEquals(200001L, json.get("externalReason").getAsLong());
        assertFalse(json.has("workAreaId"));
    }

    @Test
    void workAreaResponseDeserializesNewFields() {
        String json = """
                {
                  "workAreaId": 17746,
                  "name": "Autumn",
                  "type": "RANDOM",
                  "schedulable": true,
                  "useGlobalCuttingHeight": false,
                  "lastTimeAbandoned": 1720000000,
                  "orientation": 45,
                  "orientationShift": 10
                }
                """;

        WorkArea workArea = gson.fromJson(json, WorkArea.class);
        assertNotNull(workArea);

        assertEquals(17746L, workArea.getWorkAreaId());
        assertEquals("Autumn", workArea.getName());
        assertEquals("RANDOM", workArea.getType());
        assertTrue(workArea.isSchedulable());
        assertFalse(workArea.isUseGlobalCuttingHeight());
        assertEquals(1720000000L, workArea.getLastTimeAbandoned());
        assertEquals(45, workArea.getOrientation());
        assertEquals(10, workArea.getOrientationShift());
    }
}
