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
package org.openhab.binding.lcn.internal.subhandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRelaySubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleRelaySubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleRelaySubHandler(handler, info);
    }

    @Test
    public void testStatusAllOff() {
        tryParseAllHandlers("=M000005Rx000");
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "1", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "2", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "3", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "4", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "5", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "6", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "7", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "8", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "1", UpDownType.UP);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "2", UpDownType.UP);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "3", UpDownType.UP);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "4", UpDownType.UP);
        verify(handler, times(12)).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusAllOn() {
        tryParseAllHandlers("=M000005Rx255");
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "1", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "2", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "3", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "5", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "6", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "7", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "8", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "1", UpDownType.DOWN);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "2", UpDownType.DOWN);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "3", UpDownType.DOWN);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "4", UpDownType.DOWN);
        verify(handler, times(12)).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusRelay1Relay7On() {
        tryParseAllHandlers("=M000005Rx065");
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "1", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "2", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "3", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "4", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "5", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "6", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "7", OnOffType.ON);
        verify(handler).updateChannel(LcnChannelGroup.RELAY, "8", OnOffType.OFF);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "1", UpDownType.UP);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "2", UpDownType.UP);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "3", UpDownType.UP);
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTERRELAY, "4", UpDownType.UP);
        verify(handler, times(12)).updateChannel(any(), any(), any());
    }

    @Test
    public void testHandleCommandRelay1On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.RELAY, 0);
        verify(handler).sendPck("R81-------");
    }

    @Test
    public void testHandleCommandRelay8On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.RELAY, 7);
        verify(handler).sendPck("R8-------1");
    }

    @Test
    public void testHandleCommandRelay1Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.RELAY, 0);
        verify(handler).sendPck("R80-------");
    }

    @Test
    public void testHandleCommandRelay8Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.RELAY, 7);
        verify(handler).sendPck("R8-------0");
    }

    @Test
    public void testHandleCommandRelay8Percent1() throws LcnException {
        l.handleCommandPercent(new PercentType(1), LcnChannelGroup.RELAY, 7);
        verify(handler).sendPck("R8-------1");
    }

    @Test
    public void testHandleCommandRelay1Percent0() throws LcnException {
        l.handleCommandPercent(PercentType.ZERO, LcnChannelGroup.RELAY, 0);
    }
}
