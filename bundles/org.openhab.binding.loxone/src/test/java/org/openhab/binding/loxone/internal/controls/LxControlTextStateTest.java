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
import org.openhab.core.library.types.StringType;

/**
 * Test class for (@link LxControlTextState}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlTextStateTest extends LxControlTest {
    @BeforeEach
    public void setup() {
        setupControl("106bed36-016d-6dd8-ffffffe6109fb656", "0b734138-038c-0386-ffff403fb0c34b9e",
                "0fe665f4-0161-4773-ffff403fb0c34b9e", "Gate");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlTextState.class, 1, 0, 1, 1, 1);
    }

    @Test
    public void testChannels() {
        testChannel("String", null, null, null, null, null, true, null);
    }

    @Test
    public void testLoxoneStateChanges() {
        String s = new String();
        for (char c = ' '; c <= '~'; c++) {
            changeLoxoneState("textandicon", s);
            testChannelState(new StringType(s));
            s = s + c;
        }
        s = s + "\n\tabc\ndef\n";
        changeLoxoneState("textandicon", s);
        testChannelState(new StringType(s));
    }
}
