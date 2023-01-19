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
package org.openhab.binding.lcn.internal.subhandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.OnOffType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleKeyLockTableSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleKeyLockTableSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleKeyLockTableSubHandler(handler, info);
    }

    @Test
    public void testStatus() {
        tryParseAllHandlers("=M000005.TX098036000255");
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEA, "1", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEA, "2", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEA, "3", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEA, "4", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEA, "5", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEA, "6", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEA, "7", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEA, "8", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEB, "1", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEB, "2", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEB, "3", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEB, "4", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEB, "5", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEB, "6", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEB, "7", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEB, "8", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEC, "1", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEC, "2", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEC, "3", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEC, "4", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEC, "5", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEC, "6", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEC, "7", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLEC, "8", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLED, "1", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLED, "2", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLED, "3", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLED, "4", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLED, "5", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLED, "6", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLED, "7", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.KEYLOCKTABLED, "8", OnOffType.ON);
        verify(handler, times(32)).updateChannel(any(), any(), any());
    }

    @Test
    public void testHandleCommandA1Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.KEYLOCKTABLEA, 0);
        verify(handler).sendPck("TXA0-------");
    }

    @Test
    public void testHandleCommandA1On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.KEYLOCKTABLEA, 0);
        verify(handler).sendPck("TXA1-------");
    }

    @Test
    public void testHandleCommandA8Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.KEYLOCKTABLEA, 7);
        verify(handler).sendPck("TXA-------0");
    }

    @Test
    public void testHandleCommandA8On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.KEYLOCKTABLEA, 7);
        verify(handler).sendPck("TXA-------1");
    }

    @Test
    public void testHandleCommandB1Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.KEYLOCKTABLEB, 0);
        verify(handler).sendPck("TXB0-------");
    }

    @Test
    public void testHandleCommandB1On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.KEYLOCKTABLEB, 0);
        verify(handler).sendPck("TXB1-------");
    }

    @Test
    public void testHandleCommandB8Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.KEYLOCKTABLEB, 7);
        verify(handler).sendPck("TXB-------0");
    }

    @Test
    public void testHandleCommandB8On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.KEYLOCKTABLEB, 7);
        verify(handler).sendPck("TXB-------1");
    }

    @Test
    public void testHandleCommandC1Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.KEYLOCKTABLEC, 0);
        verify(handler).sendPck("TXC0-------");
    }

    @Test
    public void testHandleCommandC1On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.KEYLOCKTABLEC, 0);
        verify(handler).sendPck("TXC1-------");
    }

    @Test
    public void testHandleCommandC8Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.KEYLOCKTABLEC, 7);
        verify(handler).sendPck("TXC-------0");
    }

    @Test
    public void testHandleCommandC8On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.KEYLOCKTABLEC, 7);
        verify(handler).sendPck("TXC-------1");
    }

    @Test
    public void testHandleCommandD1Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.KEYLOCKTABLED, 0);
        verify(handler).sendPck("TXD0-------");
    }

    @Test
    public void testHandleCommandD1On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.KEYLOCKTABLED, 0);
        verify(handler).sendPck("TXD1-------");
    }

    @Test
    public void testHandleCommandD8Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.KEYLOCKTABLED, 7);
        verify(handler).sendPck("TXD-------0");
    }

    @Test
    public void testHandleCommandD8On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.KEYLOCKTABLED, 7);
        verify(handler).sendPck("TXD-------1");
    }
}
