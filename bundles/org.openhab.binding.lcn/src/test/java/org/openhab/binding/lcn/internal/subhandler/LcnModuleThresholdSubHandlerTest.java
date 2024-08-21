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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.Variable;
import org.openhab.core.library.types.DecimalType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleThresholdSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleThresholdSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleThresholdSubHandler(handler, info);
    }

    @Test
    public void testThreshold11() {
        tryParseAllHandlers("=M000005.T1112345");
        verify(handler).updateChannel(LcnChannelGroup.THRESHOLDREGISTER1, "1", new DecimalType(12345));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testThreshold14() {
        tryParseAllHandlers("=M000005.T140");
        verify(handler).updateChannel(LcnChannelGroup.THRESHOLDREGISTER1, "4", new DecimalType(0));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testThreshold41() {
        tryParseAllHandlers("=M000005.T4112345");
        verify(handler).updateChannel(LcnChannelGroup.THRESHOLDREGISTER4, "1", new DecimalType(12345));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testThresholdLegacy() {
        tryParseAllHandlers("=M000005.S1123451123411123000000000112345");
        verify(handler).updateChannel(LcnChannelGroup.THRESHOLDREGISTER1, "1", new DecimalType(12345));
        verify(handler).updateChannel(LcnChannelGroup.THRESHOLDREGISTER1, "2", new DecimalType(11234));
        verify(handler).updateChannel(LcnChannelGroup.THRESHOLDREGISTER1, "3", new DecimalType(11123));
        verify(handler).updateChannel(LcnChannelGroup.THRESHOLDREGISTER1, "4", new DecimalType(0));
        verify(handler).updateChannel(LcnChannelGroup.THRESHOLDREGISTER1, "5", new DecimalType(1));
        verify(handler, times(5)).updateChannel(any(), any(), any());
    }

    @Test
    public void testhandleCommandThreshold11Positive() throws LcnException {
        when(info.getVariableValue(Variable.THRESHOLDREGISTER11)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(true);
        l.handleCommandDecimal(new DecimalType(1100), LcnChannelGroup.THRESHOLDREGISTER1, 0);
        verify(handler).sendPck("SSR0100AR11");
    }

    @Test
    public void testhandleCommandThreshold11Negative() throws LcnException {
        when(info.getVariableValue(Variable.THRESHOLDREGISTER11)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(true);
        l.handleCommandDecimal(new DecimalType(900), LcnChannelGroup.THRESHOLDREGISTER1, 0);
        verify(handler).sendPck("SSR0100SR11");
    }

    @Test
    public void testhandleCommandThreshold44Positive() throws LcnException {
        when(info.getVariableValue(Variable.THRESHOLDREGISTER44)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(true);
        l.handleCommandDecimal(new DecimalType(1100), LcnChannelGroup.THRESHOLDREGISTER4, 3);
        verify(handler).sendPck("SSR0100AR44");
    }

    @Test
    public void testhandleCommandThreshold44Negative() throws LcnException {
        when(info.getVariableValue(Variable.THRESHOLDREGISTER44)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(true);
        l.handleCommandDecimal(new DecimalType(900), LcnChannelGroup.THRESHOLDREGISTER4, 3);
        verify(handler).sendPck("SSR0100SR44");
    }

    @Test
    public void testhandleCommandThreshold11LegacyPositive() throws LcnException {
        when(info.getVariableValue(Variable.THRESHOLDREGISTER11)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        l.handleCommandDecimal(new DecimalType(1100), LcnChannelGroup.THRESHOLDREGISTER1, 0);
        verify(handler).sendPck("SSR0100A10000");
    }

    @Test
    public void testhandleCommandThreshold11LegacyNegative() throws LcnException {
        when(info.getVariableValue(Variable.THRESHOLDREGISTER11)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        l.handleCommandDecimal(new DecimalType(900), LcnChannelGroup.THRESHOLDREGISTER1, 0);
        verify(handler).sendPck("SSR0100S10000");
    }

    @Test
    public void testhandleCommandThreshold14Legacy() throws LcnException {
        when(info.getVariableValue(Variable.THRESHOLDREGISTER14)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        l.handleCommandDecimal(new DecimalType(1100), LcnChannelGroup.THRESHOLDREGISTER1, 3);
        verify(handler).sendPck("SSR0100A00010");
    }

    @Test
    public void testhandleCommandThreshold15Legacy() throws LcnException {
        when(info.getVariableValue(Variable.THRESHOLDREGISTER15)).thenReturn(1000L);
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        l.handleCommandDecimal(new DecimalType(1100), LcnChannelGroup.THRESHOLDREGISTER1, 4);
        verify(handler).sendPck("SSR0100A00001");
    }
}
