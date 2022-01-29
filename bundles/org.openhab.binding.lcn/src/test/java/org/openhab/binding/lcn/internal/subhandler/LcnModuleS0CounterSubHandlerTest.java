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
    private @NonNullByDefault({}) LcnModuleS0CounterSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleS0CounterSubHandler(handler, info);
    }

    @Test
    public void testZero() {
        l.tryParse("=M000005.C10");
        verify(handler).updateChannel(LcnChannelGroup.S0INPUT, "1", new DecimalType(0));
    }

    @Test
    public void testMaxValue() {
        l.tryParse("=M000005.C14294967295");
        verify(handler).updateChannel(LcnChannelGroup.S0INPUT, "1", new DecimalType(4294967295L));
    }

    @Test
    public void test4() {
        l.tryParse("=M000005.C412345");
        verify(handler).updateChannel(LcnChannelGroup.S0INPUT, "4", new DecimalType(12345));
    }
}
