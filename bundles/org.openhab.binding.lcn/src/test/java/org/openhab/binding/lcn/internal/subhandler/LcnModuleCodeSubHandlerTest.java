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
        tryParseAllHandlers("=M000005.ZF255255255");
        verify(handler).triggerChannel(LcnChannelGroup.CODE, "fingerprint", "FFFFFF");
        verify(handler).triggerChannel(any(), any(), any());
    }
}
