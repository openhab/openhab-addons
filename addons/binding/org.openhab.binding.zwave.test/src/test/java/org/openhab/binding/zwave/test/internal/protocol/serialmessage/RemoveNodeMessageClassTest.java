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
import org.openhab.binding.zwave.internal.protocol.serialmessage.RemoveNodeMessageClass;

/**
 * Test cases for RemoveNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class RemoveNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponseStart = { 1, 5, 0, 75, 1, 1, -79 };
        byte[] expectedResponseStop = { 1, 5, 0, 75, 5, -2, 74 };

        RemoveNodeMessageClass handler = new RemoveNodeMessageClass();
        SerialMessage msg;

        msg = handler.doRequestStart();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseStart));

        msg = handler.doRequestStop();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseStop));
    }
}
