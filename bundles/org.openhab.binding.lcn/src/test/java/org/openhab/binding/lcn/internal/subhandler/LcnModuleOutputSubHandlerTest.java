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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.DimmerOutputCommand;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleOutputSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleOutputSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleOutputSubHandler(handler, info);
    }

    @Test
    public void testStatusOutput1OffPercent() {
        tryParseAllHandlers(":M000005A1000");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "1", new PercentType(0));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput2OffPercent() {
        tryParseAllHandlers(":M000005A2000");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "2", new PercentType(0));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput1OffNative() {
        tryParseAllHandlers(":M000005O1000");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "1", new PercentType(0));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput2OffNative() {
        tryParseAllHandlers(":M000005O2000");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "2", new PercentType(0));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput1OnPercent() {
        tryParseAllHandlers(":M000005A1100");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "1", new PercentType(100));
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTEROUTPUT, "1", UpDownType.UP);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput2OnPercent() {
        tryParseAllHandlers(":M000005A2100");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "2", new PercentType(100));
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTEROUTPUT, "1", UpDownType.DOWN);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput1OnNative() {
        tryParseAllHandlers(":M000005O1200");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "1", new PercentType(100));
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTEROUTPUT, "1", UpDownType.UP);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput2OnNative() {
        tryParseAllHandlers(":M000005O2200");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "2", new PercentType(100));
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTEROUTPUT, "1", UpDownType.DOWN);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput2On50Percent() {
        tryParseAllHandlers(":M000005A2050");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "2", new PercentType(50));
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTEROUTPUT, "1", UpDownType.DOWN);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput1On50Native() {
        tryParseAllHandlers(":M000005O1100");
        verify(handler).updateChannel(LcnChannelGroup.OUTPUT, "1", new PercentType(50));
        verify(handler).updateChannel(LcnChannelGroup.ROLLERSHUTTEROUTPUT, "1", UpDownType.UP);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testHandleCommandOutput1On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.OUTPUT, 0);
        verify(handler).sendPck("A1DI100000");
    }

    @Test
    public void testHandleCommandOutput2On() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.OUTPUT, 1);
        verify(handler).sendPck("A2DI100000");
    }

    @Test
    public void testHandleCommandOutput1Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.OUTPUT, 0);
        verify(handler).sendPck("A1DI000000");
    }

    @Test
    public void testHandleCommandOutput2Off() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.OUTPUT, 1);
        verify(handler).sendPck("A2DI000000");
    }

    @Test
    public void testHandleCommandOutput1Percent10() throws LcnException {
        l.handleCommandPercent(new PercentType(99), LcnChannelGroup.OUTPUT, 0);
        verify(handler).sendPck("A1DI099000");
    }

    @Test
    public void testHandleCommandOutput2Percent1() throws LcnException {
        l.handleCommandPercent(new PercentType(1), LcnChannelGroup.OUTPUT, 1);
        verify(handler).sendPck("A2DI001000");
    }

    @Test
    public void testHandleCommandOutput1Percent995() throws LcnException {
        l.handleCommandPercent(new PercentType(BigDecimal.valueOf(99.5)), LcnChannelGroup.OUTPUT, 0);
        verify(handler).sendPck("O1DI199000");
    }

    @Test
    public void testHandleCommandOutput2Percent05() throws LcnException {
        l.handleCommandPercent(new PercentType(BigDecimal.valueOf(0.5)), LcnChannelGroup.OUTPUT, 1);
        verify(handler).sendPck("O2DI001000");
    }

    @Test
    public void testHandleCommandDimmerOutputAll60FixedRamp() throws LcnException {
        l.handleCommandDimmerOutput(new DimmerOutputCommand(BigDecimal.valueOf(60), true, false, LcnDefs.FIXED_RAMP_MS),
                0);
        verify(handler).sendPck("AH060");
    }

    @Test
    public void testHandleCommandDimmerOutputAll40CustomRamp() throws LcnException {
        l.handleCommandDimmerOutput(new DimmerOutputCommand(BigDecimal.valueOf(40), true, false, 1000), 0);
        verify(handler).sendPck("OY080080080080004");
    }

    @Test
    public void testHandleCommandDimmerOutput12Value100FixedRamp() throws LcnException {
        l.handleCommandDimmerOutput(
                new DimmerOutputCommand(BigDecimal.valueOf(100), false, true, LcnDefs.FIXED_RAMP_MS), 0);
        verify(handler).sendPck("X2001200200");
    }

    @Test
    public void testHandleCommandDimmerOutput12Value0FixedRamp() throws LcnException {
        l.handleCommandDimmerOutput(new DimmerOutputCommand(BigDecimal.valueOf(0), false, true, LcnDefs.FIXED_RAMP_MS),
                0);
        verify(handler).sendPck("X2001000000");
    }

    @Test
    public void testHandleCommandDimmerOutput12Value100NoRamp() throws LcnException {
        l.handleCommandDimmerOutput(new DimmerOutputCommand(BigDecimal.valueOf(100), false, true, 0), 0);
        verify(handler).sendPck("X2001253253");
    }

    @Test
    public void testHandleCommandDimmerOutput12Value0NoRamp() throws LcnException {
        l.handleCommandDimmerOutput(new DimmerOutputCommand(BigDecimal.valueOf(0), false, true, 0), 0);
        verify(handler).sendPck("X2001252252");
    }

    @Test
    public void testHandleCommandDimmerOutput12Value40() throws LcnException {
        l.handleCommandDimmerOutput(new DimmerOutputCommand(BigDecimal.valueOf(40), false, true, 0), 0);
        verify(handler).sendPck("AY040040");
    }

    @Test
    public void testHandleCommandTuneableWhite0() throws LcnException {
        l.handleCommandString(new StringType("DISABLE"), 0);
        verify(handler).sendPck("AW0");
    }

    @Test
    public void testHandleCommandTuneableWhite1() throws LcnException {
        l.handleCommandString(new StringType("OUTPUT1"), 0);
        verify(handler).sendPck("AW1");
    }

    @Test
    public void testHandleCommandTuneableWhite2() throws LcnException {
        l.handleCommandString(new StringType("BOTH"), 0);
        verify(handler).sendPck("AW2");
    }
}
