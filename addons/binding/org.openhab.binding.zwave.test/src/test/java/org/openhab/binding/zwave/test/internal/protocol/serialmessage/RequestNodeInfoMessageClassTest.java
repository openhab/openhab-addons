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
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeInfoMessageClass;

/**
 * Test cases for RequestNodeInfoMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class RequestNodeInfoMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 1, 4, 0, 96, 12, -105 };

        SerialMessage msg;
        RequestNodeInfoMessageClass handler = new RequestNodeInfoMessageClass();

        msg = handler.doRequest(12);
        msg.setCallbackId(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }
}
