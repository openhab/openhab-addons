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

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleHostCommandSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleHostCommandSubHandler subHandler;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        subHandler = new LcnModuleHostCommandSubHandler(handler, info);
    }

    @Test
    public void testA1Hit() {
        subHandler.tryParse("+M004000005.STH065001");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "A1:HIT");
    }

    @Test
    public void testA1Make() {
        subHandler.tryParse("+M004000005.STH066001");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "A1:MAKE");
    }

    @Test
    public void testC8Break() {
        subHandler.tryParse("+M004000005.STH112128");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "C8:BREAK");
    }

    @Test
    public void testC1Hit() {
        subHandler.tryParse("+M004000005.STH080001");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "C1:HIT");
    }

    @Test
    public void testMultiple() {
        subHandler.tryParse("+M004000005.STH121034");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "A2:HIT");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "A6:HIT");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "B2:MAKE");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "B6:MAKE");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "C2:BREAK");
        verify(handler).triggerChannel(LcnChannelGroup.HOSTCOMMAND, "sendKeys", "C6:BREAK");
    }
}
