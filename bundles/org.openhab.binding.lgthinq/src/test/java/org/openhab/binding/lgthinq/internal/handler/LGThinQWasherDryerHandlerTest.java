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
package org.openhab.binding.lgthinq.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lgthinq.handler.JsonUtils;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for the remote start payload of the {@link LGThinQWasherDryerHandler}.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
class LGThinQWasherDryerHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private @NonNullByDefault({}) WasherDryerCapability cap;
    private @NonNullByDefault({}) Map<String, Object> cmdTemplate;

    @BeforeEach
    void setUp() throws JsonProcessingException, LGThinqException {
        cap = CapabilityFactory.getInstance().create(mapper.readTree(JsonUtils.loadJson("thinq-washer-v2-cap.json")),
                WasherDryerCapability.class);
        CommandDefinition cmd = Objects.requireNonNull(cap.getCommandsDefinition().get(cap.getCommandRemoteStart()));
        cmdTemplate = cmd.getData();
    }

    private WasherDryerSnapshot snapshot(String course) throws JsonProcessingException {
        Map<String, Object> raw = mapper.readValue("""
                {"courseFL24inchBaseTitan": "%s", "smartCourseFL24inchBaseTitan": "NOT_SELECTED",
                 "temp": "TEMP_60", "spin": "SPIN_1400", "rinse": "RINSE_NORMAL", "steam": "STEAM_ON",
                 "turboWash": null, "reserveTimeHour": 0.0, "reserveTimeMinute": 0.0}
                """.formatted(course), new TypeReference<HashMap<String, Object>>() {
        });
        WasherDryerSnapshot shot = new WasherDryerSnapshot();
        shot.setCourse(course);
        shot.setSmartCourse("NOT_SELECTED");
        shot.setRawData(raw);
        return shot;
    }

    @Test
    void keepsMachineSettingsForCurrentCourse() throws JsonProcessingException {
        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, snapshot("COTTON"),
                "COTTON", Map.of());

        assertEquals("COTTON", data.get("courseFL24inchBaseTitan"));
        assertEquals("TEMP_60", data.get("temp"));
        assertEquals("SPIN_1400", data.get("spin"));
        assertEquals("RINSE_NORMAL", data.get("rinse"));
        assertEquals("STEAM_ON", data.get("steam"));
        assertEquals("Course", data.get("courseType"));
        assertFalse(data.containsValue(null));
    }

    @Test
    void usesCourseDefaultsForOtherCourse() throws JsonProcessingException {
        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, snapshot("COTTON"),
                "EASYCARE", Map.of());

        assertEquals("EASYCARE", data.get("courseFL24inchBaseTitan"));
        assertEquals("TEMP_40", data.get("temp"));
        assertEquals("STEAM_OFF", data.get("steam"));
        // numeric course defaults must not replace the snapshot value with null
        assertEquals(0.0, data.get("reserveTimeHour"));
        assertFalse(data.containsValue(null));
    }

    @Test
    void selectedFeaturesOverrideSnapshot() throws JsonProcessingException {
        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, snapshot("COTTON"),
                "COTTON", Map.of("temp", "TEMP_30", "spin", "SPIN_800"));

        assertEquals("TEMP_30", data.get("temp"));
        assertEquals("SPIN_800", data.get("spin"));
        assertEquals("RINSE_NORMAL", data.get("rinse"));
    }

    @Test
    void templateValuesUsedWhenMissingInSnapshot() throws JsonProcessingException {
        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, snapshot("COTTON"),
                "COTTON", Map.of());

        assertEquals("TURBOWASH_OFF", data.get("turboWash"));
        assertEquals("LOADITEM_OFF", data.get("loadItemWasher"));
    }
}
