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
import org.openhab.binding.zwave.internal.protocol.serialmessage.DeleteSucReturnRouteMessageClass;

/**
 * Test cases for DeleteSucReturnRouteMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class DeleteSucReturnRouteMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 1, 5, 0, 85, 12, 1, -94 };

        DeleteSucReturnRouteMessageClass handler = new DeleteSucReturnRouteMessageClass();
        SerialMessage msg = handler.doRequest(12);

        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }
}
