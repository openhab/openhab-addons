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
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.core.library.types.DecimalType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleS0CounterSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testZero() {
        tryParseAllHandlers("=M000005.C10");
        verify(handler).updateChannel(LcnChannelGroup.S0INPUT, "1", new DecimalType(0));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void testMaxValue() {
        tryParseAllHandlers("=M000005.C14294967295");
        verify(handler).updateChannel(LcnChannelGroup.S0INPUT, "1", new DecimalType(4294967295L));
        verify(handler).updateChannel(any(), any(), any());
    }

    @Test
    public void test4() {
        tryParseAllHandlers("=M000005.C412345");
        verify(handler).updateChannel(LcnChannelGroup.S0INPUT, "4", new DecimalType(12345));
        verify(handler).updateChannel(any(), any(), any());
    }
}
