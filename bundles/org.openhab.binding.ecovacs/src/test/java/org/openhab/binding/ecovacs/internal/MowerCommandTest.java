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
package org.openhab.binding.ecovacs.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.ecovacs.internal.api.commands.GetCuttingHeightCommand;
import org.openhab.binding.ecovacs.internal.api.commands.MowerCleanCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetCuttingHeightCommand;
import org.openhab.binding.ecovacs.internal.api.commands.SetSafeProtectCommand;
import org.openhab.binding.ecovacs.internal.api.commands.StartEdgeCutCommand;
import org.openhab.binding.ecovacs.internal.api.commands.ZoneMowingCommand;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Tests for mower command JSON serialization.
 *
 * @author Stefan Höhn - Initial contribution
 */
class MowerCommandTest {

    private final Gson gson = new Gson();

    @Test
    void testMowerCleanCommandName() {
        MowerCleanCommand cmd = new MowerCleanCommand("start");
        assertEquals("clean", cmd.getName(ProtocolVersion.JSON_V2));
        assertEquals("clean", cmd.getName(ProtocolVersion.JSON));
    }

    @Test
    void testMowerCleanStartPayload() {
        MowerCleanCommand cmd = new MowerCleanCommand("start");
        JsonElement payload = cmd.getJsonPayload(ProtocolVersion.JSON_V2, gson);
        JsonObject header = payload.getAsJsonObject().get("header").getAsJsonObject();
        assertEquals("0.0.22", header.get("ver").getAsString());
        JsonObject body = payload.getAsJsonObject().get("body").getAsJsonObject();
        JsonObject data = body.get("data").getAsJsonObject();
        assertEquals("start", data.get("act").getAsString());
        JsonObject content = data.get("content").getAsJsonObject();
        assertEquals("auto", content.get("type").getAsString());
    }

    @Test
    void testMowerCleanPausePayload() {
        MowerCleanCommand cmd = new MowerCleanCommand("pause");
        JsonElement payload = cmd.getJsonPayload(ProtocolVersion.JSON_V2, gson);
        JsonObject body = payload.getAsJsonObject().get("body").getAsJsonObject();
        JsonObject data = body.get("data").getAsJsonObject();
        assertEquals("pause", data.get("act").getAsString());
        JsonObject content = data.get("content").getAsJsonObject();
        assertEquals("auto", content.get("type").getAsString());
    }

    @Test
    void testMowerCleanHeaderVersion() {
        MowerCleanCommand cmd = new MowerCleanCommand("stop");
        JsonElement payload = cmd.getJsonPayload(ProtocolVersion.JSON_V2, gson);
        JsonObject header = payload.getAsJsonObject().get("header").getAsJsonObject();
        assertEquals("0.0.22", header.get("ver").getAsString());
    }

    @Test
    void testStartEdgeCutCommandPayload() {
        StartEdgeCutCommand cmd = new StartEdgeCutCommand();
        JsonElement payload = cmd.getJsonPayload(ProtocolVersion.JSON_V2, gson);
        JsonObject header = payload.getAsJsonObject().get("header").getAsJsonObject();
        assertEquals("0.0.22", header.get("ver").getAsString());
        JsonObject body = payload.getAsJsonObject().get("body").getAsJsonObject();
        JsonObject data = body.get("data").getAsJsonObject();
        assertEquals("start", data.get("act").getAsString());
        JsonObject content = data.get("content").getAsJsonObject();
        assertEquals("border", content.get("type").getAsString());
    }

    @Test
    void testZoneMowingCommandPayload() {
        ZoneMowingCommand cmd = new ZoneMowingCommand("1;2;3");
        JsonElement payload = cmd.getJsonPayload(ProtocolVersion.JSON_V2, gson);
        JsonObject header = payload.getAsJsonObject().get("header").getAsJsonObject();
        assertEquals("0.0.22", header.get("ver").getAsString());
        JsonObject body = payload.getAsJsonObject().get("body").getAsJsonObject();
        JsonObject data = body.get("data").getAsJsonObject();
        assertEquals("start", data.get("act").getAsString());
        JsonObject content = data.get("content").getAsJsonObject();
        assertEquals("spotArea", content.get("type").getAsString());
        assertEquals("1;2;3", content.get("value").getAsString());
    }

    @Test
    void testSetCuttingHeightCommandName() {
        SetCuttingHeightCommand cmd = new SetCuttingHeightCommand(50);
        assertEquals("setCutHeight", cmd.getName(ProtocolVersion.JSON_V2));
    }

    @Test
    void testSetCuttingHeightCommandPayload() {
        SetCuttingHeightCommand cmd = new SetCuttingHeightCommand(50);
        JsonElement payload = cmd.getJsonPayload(ProtocolVersion.JSON_V2, gson);
        JsonObject body = payload.getAsJsonObject().get("body").getAsJsonObject();
        JsonObject data = body.get("data").getAsJsonObject();
        assertEquals(5, data.get("level").getAsInt());
    }

    @Test
    void testGetCuttingHeightCommandName() {
        GetCuttingHeightCommand cmd = new GetCuttingHeightCommand();
        assertEquals("getCutHeight", cmd.getName(ProtocolVersion.JSON_V2));
    }

    @Test
    void testSetSafeProtectCommandPayload() {
        SetSafeProtectCommand cmdOn = new SetSafeProtectCommand(true);
        JsonElement payloadOn = cmdOn.getJsonPayload(ProtocolVersion.JSON_V2, gson);
        JsonObject dataOn = payloadOn.getAsJsonObject().get("body").getAsJsonObject().get("data").getAsJsonObject();
        assertEquals(1, dataOn.get("enable").getAsInt());

        SetSafeProtectCommand cmdOff = new SetSafeProtectCommand(false);
        JsonElement payloadOff = cmdOff.getJsonPayload(ProtocolVersion.JSON_V2, gson);
        JsonObject dataOff = payloadOff.getAsJsonObject().get("body").getAsJsonObject().get("data").getAsJsonObject();
        assertEquals(0, dataOff.get("enable").getAsInt());
    }

    @Test
    void testMowerCleanCommandThrowsForXml() {
        MowerCleanCommand cmd = new MowerCleanCommand("start");
        assertThrows(IllegalStateException.class, () -> cmd.getName(ProtocolVersion.XML));
    }
}
