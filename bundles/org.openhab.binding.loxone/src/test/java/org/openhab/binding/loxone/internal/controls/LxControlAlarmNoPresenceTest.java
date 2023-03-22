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

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Test class for (@link LxControlAlarm} - version for alarm without presence sensors
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlAlarmNoPresenceTest extends LxControlTest {
    static final String ARM_DELAYED_CHANNEL = " / Arm Delayed";
    static final String NEXT_LEVEL_CHANNEL = " / Next Level";
    static final String NEXT_LEVEL_DELAY_CHANNEL = " / Next Level Delay";
    static final String NEXT_LEVEL_DELAY_TOTAL_CHANNEL = " / Next Level Delay Total";
    static final String LEVEL_CHANNEL = " / Level";
    static final String START_TIME_CHANNEL = " / Start Time";
    static final String ARMED_DELAY_CHANNEL = " / Armed Delay";
    static final String ARMED_TOTAL_DELAY_CHANNEL = " / Armed Total Delay";
    static final String SENSORS_CHANNEL = " / Sensors";
    static final String QUIT_CHANNEL = " / Acknowledge";

    private static final String NUMBER_CHANNELS[] = { NEXT_LEVEL_CHANNEL, NEXT_LEVEL_DELAY_CHANNEL,
            NEXT_LEVEL_DELAY_TOTAL_CHANNEL, LEVEL_CHANNEL, ARMED_DELAY_CHANNEL, ARMED_TOTAL_DELAY_CHANNEL };

    @BeforeEach
    public void setup() {
        setupControl("233d5db0-0333-5865-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Burglar Alarm No Presence");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlAlarm.class, 2, 1, 11, 12, 10);
    }

    @Test
    public void testChannels() {
        // read-write channels
        testChannel("Switch");
        testChannel("Switch", ARM_DELAYED_CHANNEL);
        testChannel("Switch", QUIT_CHANNEL, null, null, null, null, true, null);
        // read-only channels
        testChannel("Number", NEXT_LEVEL_CHANNEL);
        testChannel("Number", NEXT_LEVEL_DELAY_CHANNEL);
        testChannel("Number", NEXT_LEVEL_DELAY_TOTAL_CHANNEL);
        testChannel("Number", LEVEL_CHANNEL);
        testChannel("Number", ARMED_DELAY_CHANNEL);
        testChannel("Number", ARMED_TOTAL_DELAY_CHANNEL);
        testChannel("String", SENSORS_CHANNEL);
        testChannel("DateTime", START_TIME_CHANNEL);
    }

    @Test
    public void testCommandsDefaultChannel() {
        testAction(null);
        for (int i = 0; i < 20; i++) {
            changeLoxoneState("disabledmove", 0.0);
            executeCommand(OnOffType.ON);
            testAction("on");
            executeCommand(OnOffType.OFF);
            testAction("off");
            changeLoxoneState("disabledmove", 1.0);
            executeCommand(OnOffType.ON);
            testAction("on");
            executeCommand(OnOffType.OFF);
            testAction("off");
        }
    }

    @Test
    public void testCommandsArmWithDelayChannel() {
        testAction(null);
        for (int i = 0; i < 20; i++) {
            changeLoxoneState("disabledmove", 0.0);
            executeCommand(ARM_DELAYED_CHANNEL, OnOffType.ON);
            testAction("delayedon");
            executeCommand(ARM_DELAYED_CHANNEL, OnOffType.OFF);
            testAction("off");
            changeLoxoneState("disabledmove", 1.0);
            executeCommand(ARM_DELAYED_CHANNEL, OnOffType.ON);
            testAction("delayedon");
            executeCommand(ARM_DELAYED_CHANNEL, OnOffType.OFF);
            testAction("off");
        }
    }

    @Test
    public void testCommandsQuitChannel() {
        testAction(null);
        for (int i = 0; i < 20; i++) {
            executeCommand(QUIT_CHANNEL, OnOffType.ON);
            testAction("quit");
            executeCommand(QUIT_CHANNEL, OnOffType.OFF);
            testAction(null);
        }
    }

    @Test
    public void testNumberChannels() {
        testNumberChannel(NEXT_LEVEL_CHANNEL, "nextlevel");
        testNumberChannel(NEXT_LEVEL_DELAY_CHANNEL, "nextleveldelay");
        testNumberChannel(NEXT_LEVEL_DELAY_TOTAL_CHANNEL, "nextleveldelaytotal");
        testNumberChannel(LEVEL_CHANNEL, "level");
        testNumberChannel(ARMED_DELAY_CHANNEL, "armeddelay");
        testNumberChannel(ARMED_TOTAL_DELAY_CHANNEL, "armeddelaytotal");
    }

    @Test
    public void testStartedTimeChannel() {
        changeLoxoneState("starttime", "2019-11-18 14:54:21");
        LocalDateTime ldt = LocalDateTime.of(2019, 11, 18, 14, 54, 21);
        ZonedDateTime dt = ldt.atZone(ZoneId.systemDefault());
        testChannelState(START_TIME_CHANNEL, new DateTimeType(dt));

        changeLoxoneState("starttime", "something else");
        testChannelState(START_TIME_CHANNEL, null);

        changeLoxoneState("starttime", "1981-01-02 03:04:05");
        ldt = LocalDateTime.of(1981, 1, 2, 3, 4, 5);
        dt = ldt.atZone(ZoneId.systemDefault());
        testChannelState(START_TIME_CHANNEL, new DateTimeType(dt));

        changeLoxoneState("starttime", "1981-13-02 03:04:05");
        testChannelState(START_TIME_CHANNEL, null);
    }

    @Test
    public void testSensorsChannel() {
        testChannelState(SENSORS_CHANNEL, null);
        for (int i = 0; i < 20; i++) {
            changeLoxoneState("sensors", "test sensors channel string " + i);
            testChannelState(SENSORS_CHANNEL, new StringType("test sensors channel string " + i));
        }
    }

    @Test
    public void testLevelAndAcknowledge() {
        changeLoxoneState("level", 0.0);
        testChannel("Switch", QUIT_CHANNEL, null, null, null, null, true, null);
        for (Double i = 1.0; i <= 6.0; i++) {
            changeLoxoneState("level", i);
            testChannel("Switch", QUIT_CHANNEL, null, null, null, null, false, null);
            changeLoxoneState("level", 0.0);
            testChannel("Switch", QUIT_CHANNEL, null, null, null, null, true, null);
        }
    }

    private void testNumberChannel(String channel, String state) {
        Map<String, State> states = new HashMap<>();
        for (String s : NUMBER_CHANNELS) {
            states.put(s, getChannelState(s));
        }
        for (Double i = -100.0; i <= 100.0; i += 2.341) {
            changeLoxoneState(state, i);
            testChannelState(channel, new DecimalType(i));
            states.entrySet().stream().filter(v -> !v.getKey().equals(channel)).forEach(v -> {
                String key = v.getKey();
                assertEquals(states.get(key), getChannelState(key));
            });
        }
        changeLoxoneState(state, Double.NaN);
        testChannelState(channel, UnDefType.UNDEF);
    }
}
