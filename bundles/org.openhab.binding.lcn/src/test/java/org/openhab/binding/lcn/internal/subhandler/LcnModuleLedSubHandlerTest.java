/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.subhandler;

import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleLedSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleLedSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleLedSubHandler(handler, info);
    }

    @Test
    public void testHandleCommandLed1Off() throws LcnException {
        l.handleCommandString(new StringType(LcnDefs.LedStatus.OFF.name()), 0);
        verify(handler).sendPck("LA001A");
    }

    @Test
    public void testHandleCommandLed1On() throws LcnException {
        l.handleCommandString(new StringType(LcnDefs.LedStatus.ON.name()), 0);
        verify(handler).sendPck("LA001E");
    }

    @Test
    public void testHandleCommandLed1Blink() throws LcnException {
        l.handleCommandString(new StringType(LcnDefs.LedStatus.BLINK.name()), 0);
        verify(handler).sendPck("LA001B");
    }

    @Test
    public void testHandleCommandLed1Flicker() throws LcnException {
        l.handleCommandString(new StringType(LcnDefs.LedStatus.FLICKER.name()), 0);
        verify(handler).sendPck("LA001F");
    }

    @Test
    public void testHandleCommandLed12On() throws LcnException {
        l.handleCommandString(new StringType(LcnDefs.LedStatus.ON.name()), 11);
        verify(handler).sendPck("LA012E");
    }

    @Test
    public void testHandleOnOffCommandLed1Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.LED, 0);
        verify(handler).sendPck("LA001A");
    }

    @Test
    public void testHandleOnOffCommandLed1On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.LED, 0);
        verify(handler).sendPck("LA001E");
    }
}
