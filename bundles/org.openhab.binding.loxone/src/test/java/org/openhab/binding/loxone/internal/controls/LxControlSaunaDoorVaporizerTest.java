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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.UnDefType;

/**
 * Test class for (@link LxControlSauna} - version with vaporizer and door sensor
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlSaunaDoorVaporizerTest extends LxControlSaunaDoorTest {
    @Override
    @BeforeEach
    public void setup() {
        setupControl("17452951-02ae-1b6e-ffff266cf17271dd", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Sauna Controller With Vaporizer With Door Sensor");
    }

    @Override
    @Test
    public void testControlCreation() {
        testControlCreation(LxControlSauna.class, 3, 0, 18, 18, 21);
    }

    @Override
    @Test
    public void testChannels() {
        super.testChannels();
        testChannel("Number", VAPOR_POWER_CHANNEL);
        testChannel("Switch", OUT_OF_WATER_CHANNEL);
        testChannel("Number", ACTUAL_HUMIDITY_CHANNEL);
        testChannel("Number", TARGET_HUMIDITY_CHANNEL);
        testChannel("Number", EVAPORATOR_MODE_CHANNEL);
    }

    @Override
    @Test
    public void vaporPowerChannel() {
        for (Double i = 0.0; i <= 100.0; i += 1.0) {
            changeLoxoneState("vaporpower", i);
            testChannelState(VAPOR_POWER_CHANNEL, new PercentType(i.intValue()));
        }
        changeLoxoneState("vaporpower", -1.0);
        testChannelState(VAPOR_POWER_CHANNEL, UnDefType.UNDEF);
        changeLoxoneState("vaporpower", 100.1);
        testChannelState(VAPOR_POWER_CHANNEL, UnDefType.UNDEF);
    }

    @Override
    @Test
    public void testOutOfWaterChannel() {
        for (int i = 0; i < 5; i++) {
            changeLoxoneState("lesswater", 0.0);
            testChannelState(OUT_OF_WATER_CHANNEL, OnOffType.OFF);
            changeLoxoneState("lesswater", 1.0);
            testChannelState(OUT_OF_WATER_CHANNEL, OnOffType.ON);
        }
    }

    @Override
    @Test
    public void testActualHumidityChannel() {
        for (Double i = 0.0; i <= 100.0; i += 0.17) {
            changeLoxoneState("humidityactual", i);
            testChannelState(ACTUAL_HUMIDITY_CHANNEL, new DecimalType(i));
        }
    }

    @Override
    @Test
    public void testTargetHumidityChannel() {
        for (Double i = 0.0; i <= 100.0; i += 0.17) {
            changeLoxoneState("humiditytarget", i);
            testChannelState(TARGET_HUMIDITY_CHANNEL, new DecimalType(i));
        }
        for (Double i = 0.0; i <= 100.0; i += 0.13) {
            executeCommand(TARGET_HUMIDITY_CHANNEL, new DecimalType(i));
            testAction("humidity/" + i.toString());
        }
    }

    @Override
    @Test
    public void testEvaporatorModelChannel() {
        for (Double i = 0.0; i <= 6.0; i += 1.0) {
            changeLoxoneState("mode", i);
            testChannelState(EVAPORATOR_MODE_CHANNEL, new DecimalType(i));
        }
        for (Double i = -10.0; i < 0.0; i += 0.4) {
            executeCommand(EVAPORATOR_MODE_CHANNEL, new DecimalType(i));
            testAction(null);
        }
        for (Double i = 0.0; i < 6.0; i += 1.0) {
            executeCommand(EVAPORATOR_MODE_CHANNEL, new DecimalType(i));
            testAction("mode/" + i.toString());
        }
        for (Double i = 6.1; i < 15.0; i += 0.1) {
            executeCommand(EVAPORATOR_MODE_CHANNEL, new DecimalType(i));
            testAction(null);
        }
        for (Double i = 0.3; i < 6.0; i += 1.0) {
            executeCommand(EVAPORATOR_MODE_CHANNEL, new DecimalType(i));
            testAction(null);
        }
    }
}
