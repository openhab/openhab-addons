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
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleOperatingHoursCounterSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testStatusOutput1Duration0() {
        tryParseAllHandlers("$M000005A10000000000");
        verify(handler).updateChannel(LcnChannelGroup.OPERATINGHOURS, "output1", QuantityType.valueOf(0, Units.SECOND));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutput1Duration9999999999() {
        tryParseAllHandlers("$M000005A49999999999");
        verify(handler).updateChannel(LcnChannelGroup.OPERATINGHOURS, "output4",
                QuantityType.valueOf(9999999999L, Units.SECOND));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutputRelativeWork1Duration0() {
        tryParseAllHandlers("$M000005I10000000000");
        verify(handler).updateChannel(LcnChannelGroup.OPERATINGHOURS, "outputrelativework1",
                QuantityType.valueOf(0, Units.SECOND));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusOutputRelativeWork1Duration9999999999() {
        tryParseAllHandlers("$M000005I49999999999");
        verify(handler).updateChannel(LcnChannelGroup.OPERATINGHOURS, "outputrelativework4",
                QuantityType.valueOf(9999999999L, Units.SECOND));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusRelay1Duration0() {
        tryParseAllHandlers("$M000005R10000000000");
        verify(handler).updateChannel(LcnChannelGroup.OPERATINGHOURS, "relay1", QuantityType.valueOf(0, Units.SECOND));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusRelay1Duration9999999999() {
        tryParseAllHandlers("$M000005R49999999999");
        verify(handler).updateChannel(LcnChannelGroup.OPERATINGHOURS, "relay4",
                QuantityType.valueOf(9999999999L, Units.SECOND));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusBinarySensor1Duration0() {
        tryParseAllHandlers("$M000005B10000000000");
        verify(handler).updateChannel(LcnChannelGroup.OPERATINGHOURS, "binarysensor1",
                QuantityType.valueOf(0, Units.SECOND));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusBinarySensor1Duration9999999999() {
        tryParseAllHandlers("$M000005B49999999999");
        verify(handler).updateChannel(LcnChannelGroup.OPERATINGHOURS, "binarysensor4",
                QuantityType.valueOf(9999999999L, Units.SECOND));
        verify(handler).updateChannel(any(), any(), any());
    }
}
