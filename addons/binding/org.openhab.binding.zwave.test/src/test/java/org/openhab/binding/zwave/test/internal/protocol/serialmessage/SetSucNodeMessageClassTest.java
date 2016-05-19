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
import org.openhab.binding.zwave.internal.protocol.serialmessage.SetSucNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SetSucNodeMessageClass.SUCType;

/**
 * Test cases for SetSucNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class SetSucNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponseNone = { 1, 8, 0, 84, 12, 1, 0, 0, 1, -81 };
        byte[] expectedResponseBasic = { 1, 8, 0, 84, 12, 0, 0, 0, 1, -82 };

        SerialMessage msg;
        SetSucNodeMessageClass handler = new SetSucNodeMessageClass();

        msg = handler.doRequest(12, SUCType.BASIC);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseNone));

        msg = handler.doRequest(12, SUCType.NONE);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseBasic));
    }
}
