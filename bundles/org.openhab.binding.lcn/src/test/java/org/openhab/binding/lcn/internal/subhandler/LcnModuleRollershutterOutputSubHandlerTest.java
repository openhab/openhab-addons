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
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRollershutterOutputSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleRollershutterOutputSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleRollershutterOutputSubHandler(handler, info);
    }

    @Test
    public void testUp() throws LcnException {
        l.handleCommandUpDown(UpDownType.UP, LcnChannelGroup.ROLLERSHUTTEROUTPUT, 0, false);
        verify(handler).sendPck("A1DI100008");
    }

    @Test
    public void testUpInverted() throws LcnException {
        l.handleCommandUpDown(UpDownType.UP, LcnChannelGroup.ROLLERSHUTTEROUTPUT, 0, true);
        verify(handler).sendPck("A2DI100008");
    }

    @Test
    public void testDown() throws LcnException {
        l.handleCommandUpDown(UpDownType.DOWN, LcnChannelGroup.ROLLERSHUTTEROUTPUT, 0, false);
        verify(handler).sendPck("A2DI100008");
    }

    @Test
    public void testDownInverted() throws LcnException {
        l.handleCommandUpDown(UpDownType.DOWN, LcnChannelGroup.ROLLERSHUTTEROUTPUT, 0, true);
        verify(handler).sendPck("A1DI100008");
    }

    @Test
    public void testStop() throws LcnException {
        l.handleCommandStopMove(StopMoveType.STOP, LcnChannelGroup.ROLLERSHUTTEROUTPUT, 0);
        verify(handler).sendPck("A1DI000000");
        verify(handler).sendPck("A2DI000000");
    }

    @Test
    public void testMove() throws LcnException {
        l.handleCommandStopMove(StopMoveType.MOVE, LcnChannelGroup.ROLLERSHUTTEROUTPUT, 0);
        verify(handler).sendPck("A2DI100008");
    }
}
