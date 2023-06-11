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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.StateOption;

/**
 * Test class for (@link LxControlRadio} - variant with no 'all off' selection
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlRadioTest extends LxControlTest {
    @BeforeEach
    public void setup() {
        setupControl("4255054f-0355-af47-ffff403fb0c34b9e", "11d68cf4-0080-7697-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Sprinkler 1");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlRadio.class, 2, 0, 1, 1, 1);
    }

    @Test
    public void testChannels() {
        List<StateOption> opts = new ArrayList<>();
        for (Integer i = 1; i <= 6; i++) {
            opts.add(new StateOption(i.toString(), "Sprinkler " + i.toString()));
        }
        testChannel("Number", null, BigDecimal.ZERO, new BigDecimal(16), BigDecimal.ONE, null, false, opts);
    }

    @Test
    public void testLoxoneCommonStateChanges() {
        testChannelState(null);
        for (int i = 1; i <= 6; i++) {
            changeLoxoneState("activeoutput", i * 1.0);
            testChannelState(new DecimalType(i));
        }
        changeLoxoneState("activeoutput", 0.5);
        testChannelState(null);

        changeLoxoneState("activeoutput", 7.0);
        testChannelState(null);
        changeLoxoneState("activeoutput", 17.0);
        testChannelState(null);
    }

    @Test
    public void testLoxoneZeroIndexChanges() {
        changeLoxoneState("activeoutput", 0.0);
        testChannelState(null);
    }

    @Test
    public void testCommonCommands() {
        for (Integer i = 1; i <= 6; i++) {
            executeCommand(new DecimalType(i));
            testAction(i.toString());
        }
        executeCommand(new DecimalType(7));
        testAction(null);
        executeCommand(new DecimalType(17));
        testAction(null);

        executeCommand(PercentType.HUNDRED);
        testAction(null);
    }

    @Test
    public void testZeroIndexCommands() {
        executeCommand(DecimalType.ZERO);
        testAction(null);
        executeCommand(OnOffType.OFF);
        testAction(null);
    }
}
