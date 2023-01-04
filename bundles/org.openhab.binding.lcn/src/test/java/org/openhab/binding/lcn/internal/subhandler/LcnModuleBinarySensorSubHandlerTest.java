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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.core.library.types.OpenClosedType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleBinarySensorSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testStatusAllClosed() {
        tryParseAllHandlers("=M000005Bx000");
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "1", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "2", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "3", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "4", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "5", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "6", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "7", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "8", OpenClosedType.CLOSED);
        verify(handler, times(8)).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatusAllOpen() {
        tryParseAllHandlers("=M000005Bx255");
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "1", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "2", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "3", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "5", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "6", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "7", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "8", OpenClosedType.OPEN);
        verify(handler, times(8)).updateChannel(any(), any(), any());
    }

    @Test
    public void testStatus1And7Closed() {
        tryParseAllHandlers("=M000005Bx065");
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "1", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "2", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "3", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "4", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "5", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "6", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "7", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "8", OpenClosedType.CLOSED);
        verify(handler, times(8)).updateChannel(any(), any(), any());
    }
}
