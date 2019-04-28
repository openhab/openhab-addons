/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for (@link LxControlSwitch}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlSwitchTest extends LxControlTest {
    @Before
    public void setup() {
        setupControl("0f2f6b5d-0349-83b1-ffff403fb0c34b9e", "0b734138-038c-0382-ffff403fb0c34b9e",
                "0b734138-033e-02d4-ffff403fb0c34b9e", "Switch Button");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlSwitch.class, 1, 0, 1, 1, 1);
    }

    @Test
    public void testChannels() {
        testChannel("Switch");
    }

    @Test
    public void testLoxoneStateChanges() {
        for (Double i = 2.0; i < 100.0; i++) {
            changeLoxoneState("active", 0.0);
            testChannelState(OnOffType.OFF);
            changeLoxoneState("active", 1.0);
            testChannelState(OnOffType.ON);
            changeLoxoneState("active", 1.0 / i);
            testChannelState(UnDefType.UNDEF);
        }
    }

    @Test
    public void testCommands() {
        for (int i = 0; i < 100; i++) {
            executeCommand(OnOffType.ON);
            testAction("On");
            executeCommand(DecimalType.ZERO);
            testAction(null);
            executeCommand(OnOffType.OFF);
            testAction("Off");
            executeCommand(StringType.EMPTY);
            testAction(null);
        }
    }
}
