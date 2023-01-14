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
import org.openhab.core.library.types.StringType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleVariableSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleVariableSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleVariableSubHandler(handler, info);
    }

    @Test
    public void testStatusVariable1() {
        tryParseAllHandlers("=M000005.A00112345");
        verify(handler).updateChannel(LcnChannelGroup.VARIABLE, "1", new DecimalType(12345));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusVariable12() {
        tryParseAllHandlers("=M000005.A01212345");
        verify(handler).updateChannel(LcnChannelGroup.VARIABLE, "12", new DecimalType(12345));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusLegacyVariable3() {
        when(info.getLastRequestedVarWithoutTypeInResponse()).thenReturn(Variable.VARIABLE3);
        tryParseAllHandlers("=M000005.12345");
        verify(handler).updateChannel(LcnChannelGroup.VARIABLE, "3", new DecimalType(12345));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testHandleCommandLegacyTvarPositive() throws LcnException {
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        when(info.getVariableValue(Variable.VARIABLE1)).thenReturn(1000L);
        l.handleCommandDecimal(new DecimalType(1234), LcnChannelGroup.VARIABLE, 0);
        verify(handler).sendPck("ZA234");
    }

    @Test
    public void testHandleCommandLegacyTvarNegative() throws LcnException {
        when(info.hasExtendedMeasurementProcessing()).thenReturn(false);
        when(info.getVariableValue(Variable.VARIABLE1)).thenReturn(2000L);
        l.handleCommandDecimal(new DecimalType(1100), LcnChannelGroup.VARIABLE, 0);
        verify(handler).sendPck("ZS900");
    }

    @Test
    public void testStatusVariable10SensorDefective() {
        tryParseAllHandlers("=M000005.A01032512");
        verify(handler).updateChannel(LcnChannelGroup.VARIABLE, "10", new StringType("Sensor defective: VARIABLE10"));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusVariable8NotConfigured() {
        tryParseAllHandlers("=M000005.A00865535");
        verify(handler).updateChannel(LcnChannelGroup.VARIABLE, "8",
                new StringType("Not configured in LCN-PRO: VARIABLE8"));
        verify(handler).updateChannel(any(), any(), any());
    }
}
