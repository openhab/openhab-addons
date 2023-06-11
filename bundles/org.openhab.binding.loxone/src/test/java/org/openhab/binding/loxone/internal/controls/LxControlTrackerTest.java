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
import org.openhab.core.library.types.StringType;

/**
 * Test class for (@link LxControlTracker}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlTrackerTest extends LxControlTest {
    @BeforeEach
    public void setup() {
        setupControl("132aa43b-01d4-56ea-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Tracker Control");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlTracker.class, 1, 0, 1, 1, 1);
    }

    @Test
    public void testChannels() {
        testChannel("String", null, null, null, null, null, true, null);
    }

    @Test
    public void testLoxoneStateChanges() {
        for (int i = 0; i < 20; i++) {
            String s = new String();
            for (int j = 0; j < i; j++) {
                for (char c = 'a'; c <= 'a' + j; c++) {
                    s = s + c;
                }
                if (j != i - 1) {
                    s = s + '|';
                }
            }
            changeLoxoneState("entries", s);
            testChannelState(new StringType(s));
        }
    }
}
