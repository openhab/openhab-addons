/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

/**
 * Test class for (@link LxControlSauna} - version with no door sensor and no vaporizer
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlSaunaTest extends LxControlTest {
    private static final String ACTIVE_CHANNEL = " / Active";
    private static final String POWER_CHANNEL = " / Power";
    private static final String TEMP_ACTUAL_CHANNEL = " / Temperature / Actual";
    private static final String TEMP_BENCH_CHANNEL = " / Temperature / Bench";
    private static final String TEMP_TARGET_CHANNEL = " / Temperature / Target";
    private static final String FAN_CHANNEL = " / Fan";
    private static final String DRYING_CHANNEL = " / Drying";
    static final String DOOR_CLOSED_CHANNEL = " / Door Closed";
    private static final String ERROR_CODE_CHANNEL = " / Error Code";
    static final String VAPOR_POWER_CHANNEL = " / Evaporator / Power";
    private static final String TIMER_CURRENT_CHANNEL = " / Timer / Current";
    private static final String TIMER_TRIGGER_CHANNEL = " / Timer / Trigger";
    private static final String TIMER_TOTAL_CHANNEL = " / Timer / Total";
    static final String OUT_OF_WATER_CHANNEL = " / Evaporator / Out Of Water";
    static final String ACTUAL_HUMIDITY_CHANNEL = " / Evaporator / Humidity / Actual";
    static final String TARGET_HUMIDITY_CHANNEL = " / Evaporator / Humidity / Target";
    static final String EVAPORATOR_MODE_CHANNEL = " / Evaporator / Mode";
    private static final String NEXT_STATE_CHANNEL = " / Next State";

    @BeforeEach
    public void setup() {
        setupControl("17452951-02ae-1b6e-ffff266cf17271db", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Sauna Controller No Vaporizer No Door Sensor");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlSauna.class, 3, 0, 12, 12, 14);
    }

    @Test
    public void testChannels() {
        testChannel("Switch", ACTIVE_CHANNEL);
        testChannel("Number", POWER_CHANNEL);
        testChannel("Number", TEMP_ACTUAL_CHANNEL);
        testChannel("Number", TEMP_BENCH_CHANNEL);
        testChannel("Number", TEMP_TARGET_CHANNEL);
        testChannel("Switch", FAN_CHANNEL);
        testChannel("Switch", DRYING_CHANNEL);
        testChannel("Number", ERROR_CODE_CHANNEL);
        testChannel("Number", TIMER_CURRENT_CHANNEL);
        testChannel("Switch", TIMER_TRIGGER_CHANNEL);
        testChannel("Number", TIMER_TOTAL_CHANNEL);
        testChannel("Switch", NEXT_STATE_CHANNEL);
    }

    @Test
    public void testActiveChannel() {
        for (int i = 0; i < 5; i++) {
            changeLoxoneState("active", 0.0);
            testChannelState(ACTIVE_CHANNEL, OnOffType.OFF);
            changeLoxoneState("active", 1.0);
            testChannelState(ACTIVE_CHANNEL, OnOffType.ON);
        }
        for (int i = 0; i < 5; i++) {
            executeCommand(ACTIVE_CHANNEL, OnOffType.ON);
            testAction("on");
            executeCommand(ACTIVE_CHANNEL, DecimalType.ZERO);
            testAction(null);
            executeCommand(ACTIVE_CHANNEL, OnOffType.OFF);
            testAction("off");
            executeCommand(ACTIVE_CHANNEL, StringType.EMPTY);
            testAction(null);
        }
    }

    @Test
    public void testPowerChannel() {
        for (Double i = 0.0; i <= 100.0; i += 1.0) {
            changeLoxoneState("power", i);
            testChannelState(POWER_CHANNEL, new PercentType(i.intValue()));
        }
        changeLoxoneState("power", -1.0);
        testChannelState(POWER_CHANNEL, UnDefType.UNDEF);
        changeLoxoneState("power", 100.1);
        testChannelState(POWER_CHANNEL, UnDefType.UNDEF);
    }

    @Test
    public void testTempActualBenchChannels() {
        for (Double i = -20.0; i <= 150.0; i += 0.37) {
            changeLoxoneState("tempactual", i);
            testChannelState(TEMP_ACTUAL_CHANNEL, new DecimalType(i));
            changeLoxoneState("tempbench", i * 1.1);
            testChannelState(TEMP_BENCH_CHANNEL, new DecimalType(i * 1.1));
            changeLoxoneState("temptarget", i * 1.2);
            testChannelState(TEMP_TARGET_CHANNEL, new DecimalType(i * 1.2));
        }
    }

    @Test
    public void testTempTargetSetCommand() {
        for (Double i = 0.0; i <= 150.0; i += 0.37) {
            executeCommand(TEMP_TARGET_CHANNEL, new DecimalType(i));
            testAction("temp/" + i.toString());
        }
    }

    @Test
    public void testFanChannel() {
        for (int i = 0; i < 5; i++) {
            changeLoxoneState("fan", 0.0);
            testChannelState(FAN_CHANNEL, OnOffType.OFF);
            changeLoxoneState("fan", 1.0);
            testChannelState(FAN_CHANNEL, OnOffType.ON);
        }
        for (int i = 0; i < 5; i++) {
            executeCommand(FAN_CHANNEL, OnOffType.ON);
            testAction("fanon");
            executeCommand(FAN_CHANNEL, DecimalType.ZERO);
            testAction(null);
            executeCommand(FAN_CHANNEL, OnOffType.OFF);
            testAction("fanoff");
            executeCommand(FAN_CHANNEL, StringType.EMPTY);
            testAction(null);
        }
    }

    @Test
    public void testDryingChannel() {
        for (int i = 0; i < 5; i++) {
            changeLoxoneState("drying", 0.0);
            testChannelState(DRYING_CHANNEL, OnOffType.OFF);
            changeLoxoneState("drying", 1.0);
            testChannelState(DRYING_CHANNEL, OnOffType.ON);
        }
    }

    @Test
    public void testDoorClosedChannel() {
        testNoChannel(DOOR_CLOSED_CHANNEL);
    }

    @Test
    public void testErrorCodeChannel() {
        for (Double i = 0.0; i < 10.0; i += 1.0) {
            changeLoxoneState("saunaerror", i);
            changeLoxoneState("error", 0.0);
            testChannelState(ERROR_CODE_CHANNEL, DecimalType.ZERO);
            changeLoxoneState("error", 1.0);
            testChannelState(ERROR_CODE_CHANNEL, new DecimalType(i));
        }
    }

    @Test
    public void testTimerCurrentTotalChannels() {
        for (Double i = 0.0; i <= 150.0; i += 0.21) {
            changeLoxoneState("timer", i);
            testChannelState(TIMER_CURRENT_CHANNEL, new DecimalType(i));
            changeLoxoneState("timertotal", i * 1.3);
            testChannelState(TIMER_TOTAL_CHANNEL, new DecimalType(i * 1.3));
        }
    }

    @Test
    public void testTimerTriggerChannel() {
        for (int i = 0; i <= 10; i++) {
            executeCommand(TIMER_TRIGGER_CHANNEL, DecimalType.ZERO);
            testAction(null);
            testChannelState(TIMER_TRIGGER_CHANNEL, OnOffType.OFF);
            executeCommand(TIMER_TRIGGER_CHANNEL, OnOffType.ON);
            testAction("starttimer");
            testChannelState(TIMER_TRIGGER_CHANNEL, OnOffType.OFF);
            executeCommand(TIMER_TRIGGER_CHANNEL, OnOffType.OFF);
            testAction(null);
            testChannelState(TIMER_TRIGGER_CHANNEL, OnOffType.OFF);
        }
    }

    @Test
    public void vaporPowerChannel() {
        testNoChannel(VAPOR_POWER_CHANNEL);
    }

    @Test
    public void testOutOfWaterChannel() {
        testNoChannel(OUT_OF_WATER_CHANNEL);
    }

    @Test
    public void testActualHumidityChannel() {
        testNoChannel(ACTUAL_HUMIDITY_CHANNEL);
    }

    @Test
    public void testTargetHumidityChannel() {
        testNoChannel(TARGET_HUMIDITY_CHANNEL);
    }

    @Test
    public void testEvaporatorModelChannel() {
        testNoChannel(EVAPORATOR_MODE_CHANNEL);
    }

    @Test
    public void testNextStateTriggerChannel() {
        for (int i = 0; i <= 10; i++) {
            executeCommand(NEXT_STATE_CHANNEL, DecimalType.ZERO);
            testAction(null);
            testChannelState(NEXT_STATE_CHANNEL, OnOffType.OFF);
            executeCommand(NEXT_STATE_CHANNEL, OnOffType.ON);
            testAction("pulse");
            testChannelState(NEXT_STATE_CHANNEL, OnOffType.OFF);
            executeCommand(NEXT_STATE_CHANNEL, OnOffType.OFF);
            testAction(null);
            testChannelState(NEXT_STATE_CHANNEL, OnOffType.OFF);
        }
    }
}
