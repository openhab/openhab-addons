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
package org.openhab.binding.dreame.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameMowerHeartbeat;
import org.openhab.binding.dreame.internal.model.DreameMowerPose;
import org.openhab.binding.dreame.internal.model.DreameMowerTask;
import org.openhab.binding.dreame.internal.model.DreameMowerTaskStatus;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;

import com.google.gson.JsonParser;

/**
 * Tests MQTT events and proprietary mower telemetry frames.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameMqttMessageParserTest {

    @Test
    void mqttPropertyChangeMapsByServiceAndPropertyId() throws DreameCloudException {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"did":"2.1","siid":2,"piid":1,"value":1},
                  {"did":"3.1","siid":3,"piid":1,"value":76},
                  {"did":"99.1","siid":99,"piid":1,"value":"ignored"}
                ]}}
                """;

        DreameStatus status = new DreameMqttMessageParser().parse(payload,
                List.of(DreameProperty.STATE, DreameProperty.BATTERY_LEVEL));

        assertEquals(1, status.integer(DreameProperty.STATE, -1));
        assertEquals(76, status.integer(DreameProperty.BATTERY_LEVEL, -1));
        assertEquals(2, status.properties().size());
    }

    @Test
    void mqttParserKeepsMovaDndStatusAndTimeWindow() throws DreameCloudException {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":2,"piid":51,"value":{"start":1320,"end":480,"value":1}}
                ]}}
                """;

        DreameStatus status = new DreameMqttMessageParser().parse(payload, List.of(DreameProperty.DND_STATUS));

        assertEquals(JsonParser.parseString("{\"start\":1320,\"end\":480,\"value\":1}"),
                status.value(DreameProperty.DND_STATUS));
    }

    @Test
    void mqttParserIgnoresNonPropertyMessages() throws DreameCloudException {
        DreameStatus status = new DreameMqttMessageParser().parse("{\"data\":{\"method\":\"event\"}}",
                List.of(DreameProperty.STATE));

        assertTrue(status.properties().isEmpty());
    }

    @Test
    void mqttParserRecognizesMissionCompletionEvent() throws DreameCloudException {
        String payload = """
                {"data":{"method":"event_occured","params":{"siid":4,"eiid":1,"arguments":[
                  {"piid":2,"value":6},{"piid":3,"value":1094}
                ]}}}
                """;

        DreameStatus status = new DreameMqttMessageParser().parse(payload, List.of());

        assertTrue(status.missionCompleted());
        assertTrue(status.hasUpdates());
    }

    @Test
    void mqttParserIgnoresOtherEvents() throws DreameCloudException {
        DreameStatus status = new DreameMqttMessageParser()
                .parse("{\"data\":{\"method\":\"event_occured\",\"params\":{\"siid\":1,\"eiid\":1}}}", List.of());

        assertFalse(status.missionCompleted());
        assertFalse(status.hasUpdates());
    }

    @Test
    void mqttParserRecognizesMapChangeNotification() throws DreameCloudException {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"did":"123","siid":1,"piid":50,"value":{}}
                ]}}
                """;

        DreameStatus status = new DreameMqttMessageParser().parse(payload, List.of());

        assertTrue(status.mapChanged());
        assertTrue(status.hasUpdates());
    }

    @Test
    void mqttParserDecodesMowerHeartbeatBatteryAndChargingState() throws DreameCloudException {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":1,"piid":1,"value":[206,0,0,0,0,0,0,33,0,0,128,100,225,255,0,0,128,187,127,206]}
                ]}}
                """;

        DreameStatus status = new DreameMqttMessageParser().parse(payload,
                List.of(DreameProperty.BATTERY_LEVEL, DreameProperty.CHARGING_STATUS));

        assertEquals(100, status.integer(DreameProperty.BATTERY_LEVEL, -1));
        assertFalse(status.contains(DreameProperty.CHARGING_STATUS));
        DreameMowerHeartbeat heartbeat = status.mowerHeartbeat();
        assertNotNull(heartbeat);
        assertEquals(0, heartbeat.locationState());
        assertEquals(0, heartbeat.dockingState());
        assertEquals(-128, heartbeat.bleRssi());
        assertEquals(-69, heartbeat.wifiRssi());
        assertEquals(127, heartbeat.lteRssi());
    }

    @Test
    void mqttParserDecodesChargingBitAndRejectsInvalidHeartbeatFrame() throws DreameCloudException {
        String charging = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":1,"piid":1,"value":[206,0,0,0,0,0,0,0,0,0,0,206,0,0,0,0,0,0,0,206]}
                ]}}
                """;
        String invalid = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":1,"piid":1,"value":[0,0,0,0,0,0,0,0,0,0,0,80,0,0,0,0,0,0,0,206]}
                ]}}
                """;

        DreameStatus chargingStatus = new DreameMqttMessageParser().parse(charging,
                List.of(DreameProperty.BATTERY_LEVEL, DreameProperty.CHARGING_STATUS));
        DreameStatus invalidStatus = new DreameMqttMessageParser().parse(invalid,
                List.of(DreameProperty.BATTERY_LEVEL, DreameProperty.CHARGING_STATUS));

        assertEquals(78, chargingStatus.integer(DreameProperty.BATTERY_LEVEL, -1));
        assertEquals(1, chargingStatus.integer(DreameProperty.CHARGING_STATUS, -1));
        assertTrue(invalidStatus.properties().isEmpty());
    }

    @Test
    void mqttParserDecodesMowerPose() throws DreameCloudException {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":1,"piid":4,"value":[206,204,255,63,255,255,190,49,0,0,255,127,0,128,74,252,4,0,15,0,4,0,1,2,25,4,96,59,0,58,6,0,206]}
                ]}}
                """;

        DreameStatus status = new DreameMqttMessageParser().parse(payload, List.of());
        DreameMowerPose pose = status.mowerPose();
        DreameMowerTask task = status.mowerTask();

        assertNotNull(pose);
        assertNotNull(task);
        assertTrue(status.hasUpdates());
        assertEquals(-520, pose.x());
        assertEquals(-130, pose.y());
        assertEquals(268.24, pose.heading());
        assertEquals(1, task.regionId());
        assertEquals(2, task.taskId());
        assertEquals(10.49, task.progress());
        assertEquals(152.0, task.plannedArea());
        assertEquals(15.94, task.mowedArea());
    }

    @Test
    void mqttParserDecodesShortMowerPoseUsedWhileDocking() throws DreameCloudException {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":1,"piid":4,"value":[206,250,251,31,3,0,123,206]}
                ]}}
                """;

        DreameStatus status = new DreameMqttMessageParser().parse(payload, List.of());
        DreameMowerPose pose = status.mowerPose();

        assertNotNull(pose);
        assertEquals(-10300, pose.x());
        assertEquals(490, pose.y());
        assertEquals(173.65, pose.heading());
    }

    @Test
    void mqttParserDecodesMowerTaskStatus() throws DreameCloudException {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":2,"piid":50,"value":{"d":{"area_id":[],"exe":true,"o":100,"region_id":[1,2],"status":true,"time":4375},"t":"TASK"}}
                ]}}
                """;

        DreameStatus status = new DreameMqttMessageParser().parse(payload, List.of());
        DreameMowerTaskStatus task = status.mowerTaskStatus();

        assertNotNull(task);
        assertTrue(status.hasUpdates());
        assertTrue(task.executable());
        assertEquals(100, task.operation());
        assertEquals(4375L, task.time());
        assertEquals(List.of(1, 2), task.regionIds());
    }

    @Test
    void mqttParserAcceptsTaskStatusWithoutOptionalFields() throws DreameCloudException {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":2,"piid":50,"value":{"d":{"exe":true,"o":6,"status":true},"t":"TASK"}}
                ]}}
                """;

        DreameMowerTaskStatus task = new DreameMqttMessageParser().parse(payload, List.of()).mowerTaskStatus();

        assertNotNull(task);
        assertEquals(6, task.operation());
        assertNull(task.time());
        assertTrue(task.regionIds().isEmpty());
    }

    @Test
    void mqttParserDecodesMowerTaskActivity() throws DreameCloudException {
        String activePayload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":2,"piid":56,"value":{"status":[[1,0]]}}
                ]}}
                """;
        String inactivePayload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":2,"piid":56,"value":{"status":[[1,4]]}}
                ]}}
                """;

        DreameStatus active = new DreameMqttMessageParser().parse(activePayload, List.of());
        DreameStatus inactive = new DreameMqttMessageParser().parse(inactivePayload, List.of());

        assertEquals(Boolean.TRUE, active.mowerTaskActive());
        assertTrue(active.hasUpdates());
        assertEquals(Boolean.FALSE, inactive.mowerTaskActive());
        assertTrue(inactive.hasUpdates());
    }

    @Test
    void mqttParserIgnoresUnknownMowerTaskActivity() throws DreameCloudException {
        String payload = """
                {"data":{"method":"properties_changed","params":[
                  {"siid":2,"piid":56,"value":{"status":[[1,2]]}}
                ]}}
                """;

        DreameStatus status = new DreameMqttMessageParser().parse(payload, List.of());

        assertNull(status.mowerTaskActive());
        assertFalse(status.hasUpdates());
    }
}
