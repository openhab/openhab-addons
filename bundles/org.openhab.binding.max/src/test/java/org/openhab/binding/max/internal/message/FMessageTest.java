/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests cases for {@link FMessage}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class FMessageTest {

    public static final String RAW_DATA = "F:nl.ntp.pool.org,ntp.homematic.com";

    private final FMessage message = new FMessage(RAW_DATA);

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
