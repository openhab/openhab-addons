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
import org.openhab.core.library.types.OnOffType;

/**
 * Test class for (@link LxControlUpDownDigital}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlUpDownDigitalTest extends LxControlTest {
    String upChannel;
    String downChannel;

    @BeforeEach
    public void setup() {
        upChannel = " / Up";
        downChannel = " / Down";
        setupControl("0fd08ca6-01a6-d72a-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0b734138-033e-02d4-ffff403fb0c34b9e", "First Floor Scene");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlUpDownDigital.class, 1, 0, 2, 2, 0);
    }

    @Test
    public void testChannels() {
        testChannel("Switch", upChannel);
        testChannel("Switch", downChannel);
    }

    @Test
    public void testCommands() {
        testChannelState(upChannel, OnOffType.OFF);
        testChannelState(downChannel, OnOffType.OFF);

        executeCommand(upChannel, OnOffType.ON);
        testAction("UpOn");
        testChannelState(upChannel, OnOffType.ON);
        testChannelState(downChannel, OnOffType.OFF);
        executeCommand(upChannel, OnOffType.OFF);
        testAction("UpOff");
        testChannelState(upChannel, OnOffType.OFF);
        testChannelState(downChannel, OnOffType.OFF);

        executeCommand(downChannel, OnOffType.ON);
        testAction("DownOn");
        testChannelState(upChannel, OnOffType.OFF);
        testChannelState(downChannel, OnOffType.ON);
        executeCommand(downChannel, OnOffType.OFF);
        testAction("DownOff");
        testChannelState(upChannel, OnOffType.OFF);
        testChannelState(downChannel, OnOffType.OFF);

        executeCommand(upChannel, OnOffType.ON);
        testAction("UpOn");
        testChannelState(upChannel, OnOffType.ON);
        testChannelState(downChannel, OnOffType.OFF);
        executeCommand(downChannel, OnOffType.ON);
        testAction("DownOn");
        testChannelState(upChannel, OnOffType.OFF);
        testChannelState(downChannel, OnOffType.ON);

        executeCommand(upChannel, OnOffType.ON);
        testAction("UpOn");
        testChannelState(upChannel, OnOffType.ON);
        testChannelState(downChannel, OnOffType.OFF);

        executeCommand(upChannel, OnOffType.OFF);
        testAction("UpOff");
        testChannelState(upChannel, OnOffType.OFF);
        testChannelState(downChannel, OnOffType.OFF);

        executeCommand(upChannel, OnOffType.OFF);
        testAction(null);
        testChannelState(upChannel, OnOffType.OFF);
        testChannelState(downChannel, OnOffType.OFF);

        executeCommand(downChannel, OnOffType.OFF);
        testAction(null);
        testChannelState(upChannel, OnOffType.OFF);
        testChannelState(downChannel, OnOffType.OFF);
    }
}
