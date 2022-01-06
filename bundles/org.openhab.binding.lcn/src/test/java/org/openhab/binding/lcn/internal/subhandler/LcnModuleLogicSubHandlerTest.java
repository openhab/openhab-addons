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
import org.openhab.core.library.types.StringType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleLogicSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private static final StringType ON = new StringType("ON");
    private static final StringType OFF = new StringType("OFF");
    private static final StringType BLINK = new StringType("BLINK");
    private static final StringType FLICKER = new StringType("FLICKER");
    private static final StringType NOT = new StringType("NOT");
    private static final StringType OR = new StringType("OR");
    private static final StringType AND = new StringType("AND");
    private @NonNullByDefault({}) LcnModuleLogicSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleLogicSubHandler(handler, info);
    }

    @Test
    public void testStatusLedOffLogicNot() {
        l.tryParse("=M000005.TLAAAAAAAAAAAANNNN");
        verify(handler).updateChannel(LcnChannelGroup.LED, "1", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "2", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "3", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "4", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "5", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "6", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "7", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "8", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "9", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "10", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "11", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "12", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "1", NOT);
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "2", NOT);
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "3", NOT);
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "4", NOT);
    }

    @Test
    public void testStatusMixed() {
        l.tryParse("=M000005.TLAEBFAAAAAAAFNVNT");
        verify(handler).updateChannel(LcnChannelGroup.LED, "1", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "2", ON);
        verify(handler).updateChannel(LcnChannelGroup.LED, "3", BLINK);
        verify(handler).updateChannel(LcnChannelGroup.LED, "4", FLICKER);
        verify(handler).updateChannel(LcnChannelGroup.LED, "5", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "6", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "7", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "8", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "9", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "10", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "11", OFF);
        verify(handler).updateChannel(LcnChannelGroup.LED, "12", FLICKER);
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "1", NOT);
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "2", AND);
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "3", NOT);
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "4", OR);
    }

    @Test
    public void testStatusSingleLogic1Not() {
        l.tryParse("=M000005S1000");
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "1", NOT);
    }

    @Test
    public void testStatusSingleLogic4Or() {
        l.tryParse("=M000005S4025");
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "4", OR);
    }

    @Test
    public void testStatusSingleLogic3And() {
        l.tryParse("=M000005S3050");
        verify(handler).updateChannel(LcnChannelGroup.LOGIC, "3", AND);
    }
}
