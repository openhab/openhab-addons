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
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;

/**
 * Test class.
 *
 * @author Andre Jendrysseck - Initial contribution
 */
@NonNullByDefault
public class LcnModuleCodeSubHandlerTest extends AbstractTestLcnModuleSubHandler {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testHexFingerprint() {
        tryParseAllHandlers("=M000005.ZFABCDEF");
        verify(handler).triggerChannel(LcnChannelGroup.CODE, "fingerprint", "ABCDEF");
        verify(handler).triggerChannel(any(), any(), any());
    }

    @Test
    public void testDecFingerprint() {
        tryParseAllHandlers("=M000005.ZF255001002");
        verify(handler).triggerChannel(LcnChannelGroup.CODE, "fingerprint", "FF0102");
        verify(handler).triggerChannel(any(), any(), any());
    }

    @Test
    public void testTransponder() {
        tryParseAllHandlers("=M000005.ZT255001002");
        verify(handler).triggerChannel(LcnChannelGroup.CODE, "transponder", "FF0102");
        verify(handler).triggerChannel(any(), any(), any());
    }

    @Test
    public void testRemote() {
        tryParseAllHandlers("=M000005.ZI255001002013001");
        verify(handler).triggerChannel(LcnChannelGroup.CODE, "remotecontrolkey", "B3:HIT");
        verify(handler).triggerChannel(LcnChannelGroup.CODE, "remotecontrolcode", "FF0102:B3:HIT");
        verify(handler, times(2)).triggerChannel(any(), any(), any());
    }

    @Test
    public void testRemoteBatteryLow() {
        tryParseAllHandlers("=M000005.ZI255001002008012");
        verify(handler).triggerChannel(LcnChannelGroup.CODE, "remotecontrolkey", "A8:MAKE");
        verify(handler).triggerChannel(LcnChannelGroup.CODE, "remotecontrolcode", "FF0102:A8:MAKE");
        verify(handler).triggerChannel(LcnChannelGroup.CODE, "remotecontrolbatterylow", "FF0102");
        verify(handler, times(3)).triggerChannel(any(), any(), any());
    }
}
