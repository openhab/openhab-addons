/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.serialmessage;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AddNodeMessageClass;

/**
 * Test cases for AddNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class AddNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponseStartLocal = { 1, 5, 0, 74, 1, 1, -80 };
        byte[] expectedResponseStartHigh = { 1, 5, 0, 74, -127, 1, 48 };
        byte[] expectedResponseStartNetwork = { 1, 5, 0, 74, -63, 1, 112 };
        byte[] expectedResponseStop = { 1, 5, 0, 74, 5, 1, -76 };

        SerialMessage msg;
        AddNodeMessageClass handler = new AddNodeMessageClass();

        msg = handler.doRequestStart(false, false);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseStartLocal));

        msg = handler.doRequestStart(true, false);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseStartHigh));

        msg = handler.doRequestStart(true, true);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseStartNetwork));

        msg = handler.doRequestStop();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseStop));
    }
}
