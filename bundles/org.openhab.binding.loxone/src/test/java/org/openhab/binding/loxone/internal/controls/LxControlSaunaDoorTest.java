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
import org.openhab.core.library.types.OnOffType;

/**
 * Test class for (@link LxControlSauna} - version with door sensor no vaporizer
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlSaunaDoorTest extends LxControlSaunaTest {
    @Override
    @BeforeEach
    public void setup() {
        setupControl("17452951-02ae-1b6e-ffff266cf17271dc", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Sauna Controller No Vaporizer With Door Sensor");
    }

    @Override
    @Test
    public void testControlCreation() {
        testControlCreation(LxControlSauna.class, 3, 0, 13, 13, 14);
    }

    @Override
    @Test
    public void testChannels() {
        super.testChannels();
        testChannel("Switch", DOOR_CLOSED_CHANNEL);
    }

    @Override
    @Test
    public void testDoorClosedChannel() {
        for (int i = 0; i < 5; i++) {
            changeLoxoneState("doorclosed", 0.0);
            testChannelState(DOOR_CLOSED_CHANNEL, OnOffType.OFF);
            changeLoxoneState("doorclosed", 1.0);
            testChannelState(DOOR_CLOSED_CHANNEL, OnOffType.ON);
        }
    }
}
