/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;

/**
 * Test class for {@link LxControlIRoomControllerV2}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlIRoomControllerV2Test extends LxControlTest {
    private static final String ACTIVE_MODE_CHANNEL = "/ Active Mode";
    private static final String OPERATING_MODE_CHANNEL = "/ Operating Mode";
    private static final String PREPARE_STATE_CHANNEL = "/ Prepare State";
    private static final String OPEN_WINDOW_CHANNEL = "/ Open Window";
    private static final String TEMP_ACTUAL_CHANNEL = "/ Current Temperature";
    private static final String TEMP_TARGET_CHANNEL = "/ Target Temperature";
    private static final String COMFORT_TEMPERATURE_CHANNEL = "/ Comfort Temperature";
    private static final String COMFORT_TEMPERATURE_OFFSET_CHANNEL = "/ Comfort Temperature Offset";
    private static final String COMFORT_TOLERANCE_CHANNEL = "/ Comfort Tolerance";
    private static final String ABSENT_MIN_OFFSET_CHANNEL = "/ Absent Min Offset";
    private static final String ABSENT_MAX_OFFSET_CHANNEL = "/ Absent Max Offset";
    private static final String FROST_PROTECT_TEMPERATURE_CHANNEL = "/ Frost Protect Temperature";
    private static final String HEAT_PROTECT_TEMPERATURE_CHANNEL = "/ Heat Protect Temperature";

    @BeforeEach
    public void setup() {
        setupControl("14328f8a-21c9-7c0d-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Intelligent Room Controller");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlIRoomControllerV2.class, 1, 0, 13, 13, 17);
    }

    @Test
    public void testChannels() {
        Set<String> tempTags = new HashSet<>();
        tempTags.add("CurrentTemperature");
        testChannel("Number", ACTIVE_MODE_CHANNEL);
        testChannel("Number", OPERATING_MODE_CHANNEL);
        testChannel("Number", PREPARE_STATE_CHANNEL);
        testChannel("Switch", OPEN_WINDOW_CHANNEL);
        testChannel("Number", TEMP_ACTUAL_CHANNEL, null, null, null, "%.1f°", true, null, tempTags);
        testChannel("Number", TEMP_TARGET_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", COMFORT_TEMPERATURE_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", COMFORT_TEMPERATURE_OFFSET_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", COMFORT_TOLERANCE_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", ABSENT_MIN_OFFSET_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", ABSENT_MAX_OFFSET_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", FROST_PROTECT_TEMPERATURE_CHANNEL, null, null, null, "%.1f°", true, null, tempTags);
        testChannel("Number", HEAT_PROTECT_TEMPERATURE_CHANNEL, null, null, null, "%.1f°", true, null, tempTags);
    }

    @Test
    public void testModeStateChanges() {
        for (int i = 0; i <= 3; i++) {
            changeLoxoneState("activemode", Double.valueOf(i));
            testChannelState(ACTIVE_MODE_CHANNEL, new DecimalType(i));
        }
        for (int i = 0; i <= 5; i++) {
            changeLoxoneState("operatingmode", Double.valueOf(i));
            testChannelState(OPERATING_MODE_CHANNEL, new DecimalType(i));
        }
        for (int i = -1; i <= 1; i++) {
            changeLoxoneState("preparestate", Double.valueOf(i));
            testChannelState(PREPARE_STATE_CHANNEL, new DecimalType(i));
        }
    }

    @Test
    public void testWindowStateChanges() {
        for (int i = 0; i < 100; i++) {
            changeLoxoneState("openwindow", 0.0);
            testChannelState(OPEN_WINDOW_CHANNEL, OnOffType.OFF);
            changeLoxoneState("openwindow", 1.0);
            testChannelState(OPEN_WINDOW_CHANNEL, OnOffType.ON);
        }
    }

    @Test
    public void testTemperatureStateChanges() {
        for (Double i = -50.0; i < 50.0; i += 0.3) {
            changeLoxoneState("tempactual", i);
            changeLoxoneState("temptarget", i + 0.01);
            changeLoxoneState("comforttemperature", i + 0.02);
            changeLoxoneState("comforttemperatureoffset", i + 0.03);
            changeLoxoneState("comforttolerance", i + 0.04);
            changeLoxoneState("absentminoffset", i + 0.05);
            changeLoxoneState("absentmaxoffset", i + 0.06);
            changeLoxoneState("frostprotecttemperature", i + 0.07);
            changeLoxoneState("heatprotecttemperature", i + 0.08);
            testChannelState(TEMP_ACTUAL_CHANNEL, new DecimalType(i));
            testChannelState(TEMP_TARGET_CHANNEL, new DecimalType(i + 0.01));
            testChannelState(COMFORT_TEMPERATURE_CHANNEL, new DecimalType(i + 0.02));
            testChannelState(COMFORT_TEMPERATURE_OFFSET_CHANNEL, new DecimalType(i + 0.03));
            testChannelState(COMFORT_TOLERANCE_CHANNEL, new DecimalType(i + 0.04));
            testChannelState(ABSENT_MIN_OFFSET_CHANNEL, new DecimalType(i + 0.05));
            testChannelState(ABSENT_MAX_OFFSET_CHANNEL, new DecimalType(i + 0.06));
            testChannelState(FROST_PROTECT_TEMPERATURE_CHANNEL, new DecimalType(i + 0.07));
            testChannelState(HEAT_PROTECT_TEMPERATURE_CHANNEL, new DecimalType(i + 0.08));
        }
    }

    @Test
    public void testModeCommands() {
        testAction(null);
        for (int i = 0; i <= 5; i++) {
            executeCommand(OPERATING_MODE_CHANNEL, new DecimalType(i));
            testAction("setOperatingMode/" + i);
        }
        testAction(null);
    }

    @Test
    public void testTemperatureCommands() {
        testAction(null);
        for (Double t = -50.0; t < 50.0; t += 0.03) {
            DecimalType a = new DecimalType(t);
            executeCommand(TEMP_TARGET_CHANNEL, a);
            testAction("setManualTemperature/" + t.toString());
            executeCommand(COMFORT_TEMPERATURE_CHANNEL, a);
            testAction("setComfortTemperature/" + t.toString());
            executeCommand(COMFORT_TEMPERATURE_OFFSET_CHANNEL, a);
            testAction("setComfortModeTemp/" + t.toString());
            executeCommand(COMFORT_TOLERANCE_CHANNEL, a);
            testAction("setComfortTolerance/" + t.toString());
            executeCommand(ABSENT_MAX_OFFSET_CHANNEL, a);
            testAction("setAbsentMaxTemperature/" + t.toString());
            executeCommand(ABSENT_MIN_OFFSET_CHANNEL, a);
            testAction("setAbsentMinTemperature/" + t.toString());
        }
        testAction(null);
    }
}
