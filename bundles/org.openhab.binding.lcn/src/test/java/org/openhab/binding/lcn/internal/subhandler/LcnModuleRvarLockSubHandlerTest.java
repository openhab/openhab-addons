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
import org.openhab.core.library.types.OnOffType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRvarLockSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleRvarLockSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleRvarLockSubHandler(handler, info);
    }

    @Test
    public void testLock1() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.RVARLOCK, 0);
        verify(handler).sendPck("REAXS");
    }

    @Test
    public void testLock2() throws LcnException {
        l.handleCommandOnOff(OnOffType.ON, LcnChannelGroup.RVARLOCK, 1);
        verify(handler).sendPck("REBXS");
    }

    @Test
    public void testUnlock1() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.RVARLOCK, 0);
        verify(handler).sendPck("REAXA");
    }

    @Test
    public void testUnlock2() throws LcnException {
        l.handleCommandOnOff(OnOffType.OFF, LcnChannelGroup.RVARLOCK, 1);
        verify(handler).sendPck("REBXA");
    }
}
