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
import org.openhab.binding.zwave.internal.protocol.serialmessage.EnableSucMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.EnableSucMessageClass.SUCType;

/**
 * Test cases for EnableSUCMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class EnableSUCMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponseNone = { 1, 5, 0, 82, 0, 0, -88 };
        byte[] expectedResponseBasic = { 1, 5, 0, 82, 1, 0, -87 };
        byte[] expectedResponseServer = { 1, 5, 0, 82, 1, 1, -88 };

        SerialMessage msg;
        EnableSucMessageClass handler = new EnableSucMessageClass();

        msg = handler.doRequest(SUCType.NONE);
        msg.setCallbackId(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseNone));

        msg = handler.doRequest(SUCType.BASIC);
        msg.setCallbackId(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseBasic));

        msg = handler.doRequest(SUCType.SERVER);
        msg.setCallbackId(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseServer));
    }
}
