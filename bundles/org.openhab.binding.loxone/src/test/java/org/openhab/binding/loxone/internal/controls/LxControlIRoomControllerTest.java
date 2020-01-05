/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link LxControlIRoomController}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
@NonNullByDefault
public class LxControlIRoomControllerTest extends LxControlTest {
    private static final String TEMP_ACTUAL_CHANNEL = " / Current Temperature";
    private static final String TEMP_TARGET_CHANNEL = " / Target Temperature";
    private static final String MODE_CHANNEL = " / Mode";
    private static final String SERVICE_MODE_CHANNEL = " / Service Mode";
    private static final String MANUAL_MODE_CHANNEL = " / Manual Mode";
    private static final String OPEN_WINDOW_CHANNEL = " / Open Window";
    private static final String OUTPUTS_DISABLED_CHANNEL = " / Outputs Disabled";
    private static final String OVERRIDE_TIME_CHANNEL = " / Override Time";
    private static final String OVERRIDE_TOTAL_CHANNEL = " / Override Total Time";
    private static final String MANUAL_OVERRIDE_TEMP_CHANNEL = " / Manual Override Temperature";
    private static final String MANUAL_OVERRIDE_TIME_CHANNEL = " / Manual Override Time";
    private static final String OVERRIDE_CHANNEL = " / Override";
    private static final String CURRENT_HEATING_TYPE_CHANNEL = " / Current Heating Temperature";
    private static final String CURRENT_COOLING_TYPE_CHANNEL = " / Current Cooling Temperature";
    private static final String TEMPERATURE_ECONOMY_CHANNEL = " / Temperature / Economy";
    private static final String TEMPERATURE_COMFORT_HEATING_CHANNEL = " / Temperature / Comfort Heating";
    private static final String TEMPERATURE_COMFORT_COOLING_CHANNEL = " / Temperature / Comfort Cooling";
    private static final String TEMPERATURE_EMPTY_HOUSE_CHANNEL = " / Temperature / Empty House";
    private static final String TEMPERATURE_HEAT_PROTECTION_CHANNEL = " / Temperature / Heat Protection";
    private static final String TEMPERATURE_INCREASED_HEAT_CHANNEL = " / Temperature / Increased Heat";
    private static final String TEMPERATURE_PARTY_CHANNEL = " / Temperature / Party";
    private static final String TEMPERATURE_MANUAL_CHANNEL = " / Temperature / Manual";

    @Before
    public void setup() {
        setupControl("127e7abc-01df-0afc-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fb99a98-02df-46f1-ffff403fb0c34b9e", "Intelligent Room Controller V1");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlIRoomController.class, 1, 2, 22, 26, 23);
    }

    @Test
    public void testChannels() {
        Set<String> tempTags = new HashSet<>();
        tempTags.add("CurrentTemperature");
        testChannel("Number", TEMP_ACTUAL_CHANNEL, null, null, null, "%.1f°", true, null, tempTags);
        testChannel("Number", TEMP_TARGET_CHANNEL, null, null, null, "%.1f°", true, null, tempTags);
        testChannel("Number", MODE_CHANNEL);
        testChannel("Number", SERVICE_MODE_CHANNEL);
        testChannel("String", MANUAL_MODE_CHANNEL);
        testChannel("Switch", OPEN_WINDOW_CHANNEL);
        testChannel("Switch", OUTPUTS_DISABLED_CHANNEL);
        testChannel("Number", OVERRIDE_TIME_CHANNEL);
        testChannel("Number", OVERRIDE_TOTAL_CHANNEL);
        testChannel("Number", MANUAL_OVERRIDE_TEMP_CHANNEL);
        testChannel("Number", MANUAL_OVERRIDE_TIME_CHANNEL);
        testChannel("Switch", OVERRIDE_CHANNEL);
        testChannel("String", CURRENT_HEATING_TYPE_CHANNEL);
        testChannel("String", CURRENT_COOLING_TYPE_CHANNEL);
        testChannel("Number", TEMPERATURE_ECONOMY_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", TEMPERATURE_COMFORT_HEATING_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", TEMPERATURE_COMFORT_COOLING_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", TEMPERATURE_EMPTY_HOUSE_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", TEMPERATURE_HEAT_PROTECTION_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", TEMPERATURE_INCREASED_HEAT_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", TEMPERATURE_PARTY_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
        testChannel("Number", TEMPERATURE_MANUAL_CHANNEL, null, null, null, "%.1f°", false, null, tempTags);
    }

    @Test
    public void testActualTargetTemperatureStateChanges() {
        for (Double i = -50.0; i < 50.0; i += 0.3) {
            changeLoxoneState("tempactual", i);
            changeLoxoneState("temptarget", i + 0.01);
            testChannelState(TEMP_ACTUAL_CHANNEL, new DecimalType(i));
            testChannelState(TEMP_TARGET_CHANNEL, new DecimalType(i + 0.01));
        }
    }

    @Test
    public void testModeStateChanges() {
        for (int i = 0; i <= 6; i++) {
            changeLoxoneState("mode", Double.valueOf(i));
            testChannelState(MODE_CHANNEL, new DecimalType(i));
        }
        for (int i = 0; i <= 4; i++) {
            changeLoxoneState("servicemode", Double.valueOf(i));
            testChannelState(SERVICE_MODE_CHANNEL, new DecimalType(i));
        }
        changeLoxoneState("manualmode", 0.0);
        testChannelState(MANUAL_MODE_CHANNEL, new StringType("Off"));
        changeLoxoneState("manualmode", 1.0);
        testChannelState(MANUAL_MODE_CHANNEL, new StringType("Comfort overriding"));
        changeLoxoneState("manualmode", 2.0);
        testChannelState(MANUAL_MODE_CHANNEL, new StringType("Economy overriding"));
        changeLoxoneState("manualmode", 3.0);
        testChannelState(MANUAL_MODE_CHANNEL, new StringType("Timer overriding"));
        changeLoxoneState("manualmode", 4.0);
        testChannelState(MANUAL_MODE_CHANNEL, new StringType("Movement/presence"));
        changeLoxoneState("manualmode", 5.0);
        testChannelState(MANUAL_MODE_CHANNEL, UnDefType.UNDEF);
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
    public void testOutputsDisabledStateChanges() {
        for (int i = 0; i < 100; i++) {
            changeLoxoneState("stop", 0.0);
            testChannelState(OUTPUTS_DISABLED_CHANNEL, OnOffType.OFF);
            changeLoxoneState("stop", 1.0);
            testChannelState(OUTPUTS_DISABLED_CHANNEL, OnOffType.ON);
        }
    }

    @Test
    public void testOverrideStateChanges() {
        changeLoxoneState("override", 0.0);
        changeLoxoneState("overridetotal", 0.0);
        testChannelState(OVERRIDE_CHANNEL, OnOffType.OFF);
        for (Double i = 100.0; i >= 0.0; i -= 0.5) {
            changeLoxoneState("override", i);
            changeLoxoneState("overridetotal", i + 0.01);
            if (i == 0.0) {
                testChannelState(OVERRIDE_CHANNEL, OnOffType.OFF);
            } else {
                testChannelState(OVERRIDE_CHANNEL, OnOffType.ON);
            }
            testChannelState(OVERRIDE_TIME_CHANNEL, new DecimalType(i));
            testChannelState(OVERRIDE_TOTAL_CHANNEL, new DecimalType(i + 0.01));
        }
    }

    @Test
    public void testHeatingCoolingTypeStateChanges() {
        for (int n = 0; n <= 100; n++) {
            Double i = new Double(n % 7);
            Double j = new Double((n + 3) % 7);
            changeLoxoneState("currheattempix", i);
            changeLoxoneState("currcooltempix", j);
            String label = getTemperatureLabel(i.intValue());
            assertNotNull(label);
            testChannelState(CURRENT_HEATING_TYPE_CHANNEL, new StringType(label));
            label = getTemperatureLabel(j.intValue());
            assertNotNull(label);
            testChannelState(CURRENT_COOLING_TYPE_CHANNEL, new StringType(label));
        }
    }

    @Test
    public void testTemperaturesStateChanges() {
        for (Double i = -50.0; i < 50.0; i += 0.3) {
            changeLoxoneState("temperatures-0", i);
            changeLoxoneState("temperatures-1", i + 0.01);
            changeLoxoneState("temperatures-2", i + 0.02);
            changeLoxoneState("temperatures-3", i + 0.03);
            changeLoxoneState("temperatures-4", i + 0.04);
            changeLoxoneState("temperatures-5", i + 0.05);
            changeLoxoneState("temperatures-6", i + 0.06);
            testChannelState(TEMPERATURE_ECONOMY_CHANNEL, new DecimalType(i));
            testChannelState(TEMPERATURE_COMFORT_HEATING_CHANNEL, new DecimalType(i + 0.01));
            testChannelState(TEMPERATURE_COMFORT_COOLING_CHANNEL, new DecimalType(i + 0.02));
            testChannelState(TEMPERATURE_EMPTY_HOUSE_CHANNEL, new DecimalType(i + 0.03));
            testChannelState(TEMPERATURE_HEAT_PROTECTION_CHANNEL, new DecimalType(i + 0.04));
            testChannelState(TEMPERATURE_INCREASED_HEAT_CHANNEL, new DecimalType(i + 0.05));
            testChannelState(TEMPERATURE_PARTY_CHANNEL, new DecimalType(i + 0.06));
        }
    }

    @Test
    public void testModeCommands() {
        testAction(null);
        for (int i = 0; i <= 10; i++) {
            executeCommand(MODE_CHANNEL, new DecimalType(0.0));
            testAction("mode/0");
            executeCommand(MODE_CHANNEL, new DecimalType(1.0));
            testAction("mode/3");
            executeCommand(MODE_CHANNEL, new DecimalType(2.0));
            testAction("mode/4");
            executeCommand(MODE_CHANNEL, new DecimalType(3.0));
            testAction("mode/3");
            executeCommand(MODE_CHANNEL, new DecimalType(4.0));
            testAction("mode/4");
            executeCommand(MODE_CHANNEL, new DecimalType(5.0));
            testAction("mode/5");
            executeCommand(MODE_CHANNEL, new DecimalType(6.0));
            testAction("mode/6");
        }
        testAction(null);
    }

    @Test
    public void testServiceModeCommands() {
        testAction(null);
        for (int j = 0; j <= 10; j++) {
            for (int i = 0; i <= 4; i++) {
                executeCommand(SERVICE_MODE_CHANNEL, new DecimalType(i));
                testAction("service/" + i);
            }
        }
        testAction(null);
    }

    @Test
    public void testManualOverrideCommands() {
        testAction(null);

        executeCommand(MANUAL_OVERRIDE_TEMP_CHANNEL, new DecimalType(10.0));
        testAction(null);
        testChannelState(MANUAL_OVERRIDE_TEMP_CHANNEL, UnDefType.UNDEF);

        executeCommand(MANUAL_OVERRIDE_TIME_CHANNEL, new DecimalType(1.0));
        testAction(null);
        testChannelState(MANUAL_OVERRIDE_TIME_CHANNEL, new DecimalType(1.0));

        for (int i = 0; i <= 7; i++) {
            executeCommand(MANUAL_OVERRIDE_TEMP_CHANNEL, new DecimalType(i));
            testAction(null);
            testChannelState(MANUAL_OVERRIDE_TEMP_CHANNEL, new DecimalType(i));
            int time = 10 + i * 2;
            executeCommand(MANUAL_OVERRIDE_TIME_CHANNEL, new DecimalType(time));
            testAction(null);
            testChannelState(MANUAL_OVERRIDE_TIME_CHANNEL, new DecimalType(time));
            executeCommand(OVERRIDE_CHANNEL, OnOffType.ON);
            testAction("starttimer/" + i + "/" + time);
            executeCommand(OVERRIDE_CHANNEL, OnOffType.OFF);
            testAction("stoptimer");
            executeCommand(MANUAL_OVERRIDE_TIME_CHANNEL, DecimalType.ZERO);
            testAction(null);
            executeCommand(OVERRIDE_CHANNEL, OnOffType.ON);
            testAction(null);
            executeCommand(OVERRIDE_CHANNEL, OnOffType.OFF);
            testAction("stoptimer");
        }
        testAction(null);
    }

    @Test
    public void testSetTempCommands() {
        testAction(null);
        for (Double i = -50.0; i <= 50.0; i += 0.3) {
            executeCommand(TEMPERATURE_ECONOMY_CHANNEL, new DecimalType(i));
            testAction("settemp/0/" + i);
            executeCommand(TEMPERATURE_COMFORT_HEATING_CHANNEL, new DecimalType(i + 0.01));
            testAction("settemp/1/" + (i + 0.01));
            executeCommand(TEMPERATURE_COMFORT_COOLING_CHANNEL, new DecimalType(i + 0.02));
            testAction("settemp/2/" + (i + 0.02));
            executeCommand(TEMPERATURE_EMPTY_HOUSE_CHANNEL, new DecimalType(i + 0.03));
            testAction("settemp/3/" + (i + 0.03));
            executeCommand(TEMPERATURE_HEAT_PROTECTION_CHANNEL, new DecimalType(i + 0.04));
            testAction("settemp/4/" + (i + 0.04));
            executeCommand(TEMPERATURE_INCREASED_HEAT_CHANNEL, new DecimalType(i + 0.05));
            testAction("settemp/5/" + (i + 0.05));
            executeCommand(TEMPERATURE_PARTY_CHANNEL, new DecimalType(i + 0.06));
            testAction("settemp/6/" + (i + 0.06));
            executeCommand(TEMPERATURE_MANUAL_CHANNEL, new DecimalType(i + 0.07));
            testAction("settemp/7/" + (i + 0.07));
            testChannelState(TEMPERATURE_MANUAL_CHANNEL, new DecimalType(i + 0.07));
        }
        testAction(null);
    }

    static @Nullable String getTemperatureLabel(int index) {
        switch (index) {
            case 0:
                return "Economy";
            case 1:
                return "Comfort Heating";
            case 2:
                return "Comfort Cooling";
            case 3:
                return "Empty House";
            case 4:
                return "Heat Protection";
            case 5:
                return "Increased Heat";
            case 6:
                return "Party";
            case 7:
                return "Manual";
            default:
                return null;
        }
    }
}
