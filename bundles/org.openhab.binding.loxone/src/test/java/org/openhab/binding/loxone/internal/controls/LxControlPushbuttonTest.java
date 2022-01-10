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

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * Test class for (@link LxControlPushbutton}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlPushbuttonTest extends LxControlSwitchTest {
    @Override
    @BeforeEach
    public void setup() {
        setupControl("0e3684cc-026e-28e0-ffff403fb0c34b9e", "0b734138-038c-035e-ffff403fb0c34b9e",
                "0b734138-033e-02d8-ffff403fb0c34b9e", "Kitchen All Blinds Up");
    }

    @Override
    @Test
    public void testControlCreation() {
        testControlCreation(LxControlPushbutton.class, 1, 0, 1, 1, 1);
    }

    @Override
    @Test
    public void testChannels() {
        testChannel("Switch", Collections.singleton("Switchable"));
    }

    @Override
    @Test
    public void testCommands() {
        for (int i = 0; i < 100; i++) {
            executeCommand(OnOffType.ON);
            testAction("Pulse");
            executeCommand(DecimalType.ZERO);
            testAction(null);
            executeCommand(OnOffType.OFF);
            testAction("Off");
            executeCommand(StringType.EMPTY);
            testAction(null);
        }
    }
}
