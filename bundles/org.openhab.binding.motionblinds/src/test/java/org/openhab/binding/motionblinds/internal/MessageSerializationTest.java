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
package org.openhab.binding.motionblinds.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.motionblinds.internal.dto.DeviceListEntry;
import org.openhab.binding.motionblinds.internal.dto.DeviceStatus;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsMessage;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsRequest;
import org.openhab.core.config.discovery.DiscoveryResult;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Tests (de)serialization with messages captured from a Wi-Fi blind motor (tokens replaced).
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class MessageSerializationTest {

    private static final Gson GSON = new Gson();

    static final String GET_DEVICE_LIST_ACK = """
            {"msgType":"GetDeviceListAck","mac":"244cab4e06b4","deviceType":"22000002","ProtocolVersion":"0.9",\
            "token":"5Yx9TSv3kUq7Hn2a","data":[{"mac":"244cab4e06b4","deviceType":"22000002"}]}""";
    static final String READ_DEVICE_ACK = """
            {"msgType":"ReadDeviceAck","mac":"244cab4e06b4","deviceType":"22000002","msgID":"20260928175033366",\
            "data":{"currentPosition":98,"currentAngle":180,"operation":2,"RSSI":-78}}""";
    static final String REPORT_MOVING = """
            {"msgType":"Report","mac":"244cab4e06b4","deviceType":"22000002","msgID":"20260928182944898",\
            "data":{"currentPosition":90,"currentAngle":180,"RSSI":-78}}""";
    static final String HEARTBEAT = """
            {"msgType":"Heartbeat","mac":"244cab4e062c","deviceType":"22000002","token":"Qw3rTy7uIoP1aSdF",\
            "data":{"currentAngle":180,"currentPosition":98,"RSSI":-84}}""";
    static final String ACCESS_TOKEN_ERROR = """
            {"msgType":"WriteDeviceAck","mac":"244cab4e06b4","deviceType":"22000002",\
            "actionResult":"AccessToken error","token":"Zx8cVb7nMl6kJh5g"}""";

    @Test
    public void parseGetDeviceListAck() {
        MotionBlindsMessage message = parse(GET_DEVICE_LIST_ACK);
        assertEquals("GetDeviceListAck", message.msgType);
        assertEquals("244cab4e06b4", message.mac);
        assertEquals("22000002", message.deviceType);
        assertEquals("0.9", message.protocolVersion);
        assertEquals("5Yx9TSv3kUq7Hn2a", message.token);
        JsonElement data = Objects.requireNonNull(message.data);
        DeviceListEntry[] entries = Objects.requireNonNull(GSON.fromJson(data, DeviceListEntry[].class));
        assertEquals(1, entries.length);
        assertEquals("244cab4e06b4", entries[0].mac);
    }

    @Test
    public void parseStatusMessages() {
        DeviceStatus status = parseStatus(READ_DEVICE_ACK);
        assertEquals(98, status.currentPosition);
        assertEquals(180, status.currentAngle);
        assertEquals(2, status.operation);
        assertEquals(-78, status.rssi);

        DeviceStatus moving = parseStatus(REPORT_MOVING);
        assertEquals(90, moving.currentPosition);
        assertNull(moving.operation);

        assertEquals("Qw3rTy7uIoP1aSdF", parse(HEARTBEAT).token);
    }

    @Test
    public void parseActionResult() {
        MotionBlindsMessage message = parse(ACCESS_TOKEN_ERROR);
        assertEquals("AccessToken error", message.actionResult);
        assertNull(message.data);
    }

    @Test
    public void serializeRequests() {
        JsonObject list = GSON.toJsonTree(new MotionBlindsRequest("GetDeviceList")).getAsJsonObject();
        assertEquals(2, list.size());
        assertEquals("GetDeviceList", list.get("msgType").getAsString());
        assertTrue(list.get("msgID").getAsString().matches("\\d{17}"));

        JsonObject write = GSON.toJsonTree(new MotionBlindsRequest("WriteDevice", "244cab4e06b4", "22000002",
                "4D098FF506E81A40DB823D3F60333214", Map.of("targetPosition", 50))).getAsJsonObject();
        assertEquals("4D098FF506E81A40DB823D3F60333214", write.get("AccessToken").getAsString());
        assertEquals("244cab4e06b4", write.get("mac").getAsString());
        assertEquals(50, write.getAsJsonObject("data").get("targetPosition").getAsInt());

        JsonObject read = GSON.toJsonTree(new MotionBlindsRequest("ReadDevice", "244cab4e06b4", "22000002",
                "4D098FF506E81A40DB823D3F60333214", null)).getAsJsonObject();
        assertFalse(read.has("data"));
    }

    @Test
    public void hideTokens() {
        String hidden = MotionBlindsCommunicationManager.hideTokens(HEARTBEAT);
        assertFalse(hidden.contains("Qw3rTy7uIoP1aSdF"));
        assertTrue(hidden.contains("\"token\":\"***\""));
    }

    @Test
    public void normalizeMac() {
        assertEquals("244cab4e06b4", MotionBlindsCommunicationManager.normalizeMac("24:4C:AB:4E:06:B4"));
    }

    @Test
    public void discoveryResult() {
        DiscoveryResult result = Objects.requireNonNull(
                MotionBlindsDiscoveryService.toDiscoveryResult(parse(GET_DEVICE_LIST_ACK), "192.168.0.21"));
        assertEquals("motionblinds:wifi-motor:244cab4e06b4", result.getThingUID().getAsString());
        assertEquals("244cab4e06b4", result.getProperties().get("macAddress"));
        assertNull(result.getProperties().get("ipAddress"));
        assertEquals("macAddress", result.getRepresentationProperty());

        MotionBlindsMessage hub = parse(GET_DEVICE_LIST_ACK.replace("\"deviceType\":\"22000002\",\"Proto",
                "\"deviceType\":\"02000002\",\"Proto"));
        assertNull(MotionBlindsDiscoveryService.toDiscoveryResult(hub, "192.168.0.30"));
    }

    /**
     * Gson results are nullable; {@code requireNonNull} establishes non-null values for the null analysis, which
     * does not treat JUnit assertions as null checks.
     */
    private static MotionBlindsMessage parse(String json) {
        return Objects.requireNonNull(GSON.fromJson(json, MotionBlindsMessage.class));
    }

    private static DeviceStatus parseStatus(String json) {
        return Objects.requireNonNull(GSON.fromJson(parse(json).data, DeviceStatus.class));
    }
}
