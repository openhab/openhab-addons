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
import org.openhab.core.library.types.OpenClosedType;

/**
 * Test class.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleBinarySensorSubHandlerTest extends AbstractTestLcnModuleSubHandler {
    private @NonNullByDefault({}) LcnModuleBinarySensorSubHandler l;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        l = new LcnModuleBinarySensorSubHandler(handler, info);
    }

    @Test
    public void testStatusAllClosed() {
        l.tryParse("=M000005Bx000");
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "1", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "2", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "3", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "4", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "5", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "6", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "7", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "8", OpenClosedType.CLOSED);
    }

    @Test
    public void testStatusAllOpen() {
        l.tryParse("=M000005Bx255");
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "1", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "2", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "3", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "5", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "6", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "7", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "8", OpenClosedType.OPEN);
    }

    @Test
    public void testStatus1And7Closed() {
        l.tryParse("=M000005Bx065");
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "1", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "2", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "3", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "4", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "5", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "6", OpenClosedType.CLOSED);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "7", OpenClosedType.OPEN);
        verify(handler).updateChannel(LcnChannelGroup.BINARYSENSOR, "8", OpenClosedType.CLOSED);
    }
}
