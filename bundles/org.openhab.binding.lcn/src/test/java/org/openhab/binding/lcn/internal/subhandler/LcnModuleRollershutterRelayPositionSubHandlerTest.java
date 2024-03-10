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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.core.library.types.PercentType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRollershutterRelayPositionSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) AbstractLcnModuleRollershutterRelaySubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleRollershutterRelayPositionSubHandler(handler, info);
    }

    @Test
    public void testMotor1percent0() throws LcnException {
        l.handleCommandPercent(PercentType.ZERO, LcnChannelGroup.ROLLERSHUTTERRELAY, 0);
        verify(handler).sendPck("JH000001");
    }

    @Test
    public void testMotor1percent100() throws LcnException {
        l.handleCommandPercent(PercentType.HUNDRED, LcnChannelGroup.ROLLERSHUTTERRELAY, 0);
        verify(handler).sendPck("JH100001");
    }

    @Test
    public void testMotor1percent50() throws LcnException {
        l.handleCommandPercent(new PercentType(50), LcnChannelGroup.ROLLERSHUTTERRELAY, 0);
        verify(handler).sendPck("JH050001");
    }

    @Test
    public void testMotor4percent50() throws LcnException {
        l.handleCommandPercent(new PercentType(50), LcnChannelGroup.ROLLERSHUTTERRELAY, 3);
        verify(handler).sendPck("JH050008");
    }

    @Test
    public void testInvalidMotor() throws LcnException {
        assertThrows(LcnException.class, () -> {
            l.handleCommandPercent(new PercentType(50), LcnChannelGroup.ROLLERSHUTTERRELAY, 4);
        });
    }

    @Test
    public void testInvalidPercent() throws LcnException {
        assertThrows(LcnException.class, () -> {
            PckGenerator.controlShutterPosition(0, 101);
        });
    }
}
