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
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRvarSetpointSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleRvarSetpointSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleRvarSetpointSubHandler(handler, info);
    }

    @Test
    public void testhandleCommandRvar1Positive() throws LcnException {
        when(info.hasExtendedMeasurementProcessing()).thenReturn(true);
        l.handleCommandDecimal(new DecimalType(1000), LcnChannelGroup.RVARSETPOINT, 0);
        verify(handler).sendPck("X2030032000");
    }

    @Test
    public void testhandleCommandRvar2Positive() throws LcnException {
        when(info.hasExtendedMeasurementProcessing()).thenReturn(true);
        l.handleCommandDecimal(new DecimalType(1100), LcnChannelGroup.RVARSETPOINT, 1);
        verify(handler).sendPck("X2030096100");
    }

    @Test
    public void testhandleCommandRvar1Negative() throws LcnException {
        when(info.hasExtendedMeasurementProcessing()).thenReturn(true);
        l.handleCommandDecimal(new DecimalType(0), LcnChannelGroup.RVARSETPOINT, 0);
        verify(handler).sendPck("X2030043232");
    }

    @Test
    public void testhandleCommandRvar2Negative() throws LcnException {
        when(info.hasExtendedMeasurementProcessing()).thenReturn(true);
        l.handleCommandDecimal(new DecimalType(999), LcnChannelGroup.RVARSETPOINT, 1);
        verify(handler).sendPck("X2030104001");
    }

    @Test
    public void testhandleCommandRvar1PositiveLegacy() throws LcnException {
        when(info.getVariableValue(Variable.RVARSETPOINT1)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        l.handleCommandDecimal(new DecimalType(1100), LcnChannelGroup.RVARSETPOINT, 0);
        verify(handler).sendPck("X2030032100");
    }

    @Test
    public void testhandleCommandRvar2PositiveLegacy() throws LcnException {
        when(info.getVariableValue(Variable.RVARSETPOINT2)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        l.handleCommandDecimal(new DecimalType(1100), LcnChannelGroup.RVARSETPOINT, 1);
        verify(handler).sendPck("X2030096100");
    }

    @Test
    public void testhandleCommandRvar1NegativeLegacy() throws LcnException {
        when(info.getVariableValue(Variable.RVARSETPOINT1)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        l.handleCommandDecimal(new DecimalType(900), LcnChannelGroup.RVARSETPOINT, 0);
        verify(handler).sendPck("X2030040100");
    }

    @Test
    public void testhandleCommandRvar2NegativeLegacy() throws LcnException {
        when(info.getVariableValue(Variable.RVARSETPOINT2)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        l.handleCommandDecimal(new DecimalType(900), LcnChannelGroup.RVARSETPOINT, 1);
        verify(handler).sendPck("X2030104100");
    }

    @Test
    public void testRvar1() {
        tryParseAllHandlers("=M000005.S11234");
        verify(handler).updateChannel(LcnChannelGroup.RVARSETPOINT, "1", new DecimalType(1234));
        verify(handler).updateChannel(LcnChannelGroup.RVARLOCK, "1", OnOffType.OFF);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testRvar2() {
        tryParseAllHandlers("=M000005.S21234");
        verify(handler).updateChannel(LcnChannelGroup.RVARSETPOINT, "2", new DecimalType(1234));
        verify(handler).updateChannel(LcnChannelGroup.RVARLOCK, "2", OnOffType.OFF);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testRvar1SensorDefective() {
        tryParseAllHandlers("=M000005.S132512");
        verify(handler).updateChannel(LcnChannelGroup.RVARSETPOINT, "1",
                new StringType("Sensor defective: RVARSETPOINT1"));
        verify(handler).updateChannel(LcnChannelGroup.RVARLOCK, "1", OnOffType.OFF);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testRvar1Locked() {
        tryParseAllHandlers("=M000005.S134002");
        verify(handler).updateChannel(LcnChannelGroup.RVARSETPOINT, "1", new DecimalType(1234));
        verify(handler).updateChannel(LcnChannelGroup.RVARLOCK, "1", OnOffType.ON);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }

    @Test
    public void testRvar2Locked() {
        tryParseAllHandlers("=M000005.S234002");
        verify(handler).updateChannel(LcnChannelGroup.RVARSETPOINT, "2", new DecimalType(1234));
        verify(handler).updateChannel(LcnChannelGroup.RVARLOCK, "2", OnOffType.ON);
        verify(handler, times(2)).updateChannel(any(), any(), any());
    }
}
