/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests cases for {@link F_Message}.
 *
 * @author Marcel Verpaalen - Initial Version
 * @since 2.0
 */
public class F_MessageTest {

    public final String rawData = "F:nl.ntp.pool.org,ntp.homematic.com";

    private F_Message message = null;

    @Before
    public void Before() {
        message = new F_Message(rawData);
    }

    @Test
    public void getMessageTypeTest() {

        MessageType messageType = ((Message) message).getType();
        assertEquals(MessageType.F, messageType);
    }

    @Test
    public void getServer1Test() {
        String ntpServer1 = message.getNtpServer1();
        assertEquals("nl.ntp.pool.org", ntpServer1);
    }

    @Test
    public void getServer2Test() {
        String ntpServer1 = message.getNtpServer2();
        assertEquals("ntp.homematic.com", ntpServer1);
    }
}
