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

import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.core.library.types.PercentType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRollershutterRelaySlatAngleSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) AbstractLcnModuleRollershutterRelaySubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleRollershutterRelaySlatAngleSubHandler(handler, info);
    }

    @Test
    public void testMotor1percent0() throws LcnException {
        l.handleCommandPercent(PercentType.ZERO, LcnChannelGroup.ROLLERSHUTTERRELAYSLAT, 0);
        verify(handler).sendPck("JW000001");
    }

    @Test
    public void testMotor1percent100() throws LcnException {
        l.handleCommandPercent(PercentType.HUNDRED, LcnChannelGroup.ROLLERSHUTTERRELAYSLAT, 0);
        verify(handler).sendPck("JW100001");
    }

    @Test
    public void testMotor1percent50() throws LcnException {
        l.handleCommandPercent(new PercentType(50), LcnChannelGroup.ROLLERSHUTTERRELAYSLAT, 0);
        verify(handler).sendPck("JW050001");
    }

    @Test
    public void testMotor4percent50() throws LcnException {
        l.handleCommandPercent(new PercentType(50), LcnChannelGroup.ROLLERSHUTTERRELAYSLAT, 3);
        verify(handler).sendPck("JW050008");
    }
}
