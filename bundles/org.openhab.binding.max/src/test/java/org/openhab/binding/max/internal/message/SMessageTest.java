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
public class SMessageTest {

    private static final String RAW_DATA_1 = "S:01,0,31";
    private static final String RAW_DATA_2 = "S:00,1,00";

    private final SMessage message1 = new SMessage(RAW_DATA_1);
    private final SMessage message2 = new SMessage(RAW_DATA_2);

    @Test
    public void getMessageTypeTest() {
        MessageType messageType = ((Message) message1).getType();
        assertEquals(MessageType.S, messageType);
    }

    @Test
    public void getDutyCycleTest() {
        int dutyCycle = message1.getDutyCycle();
        assertEquals(1, dutyCycle);
        dutyCycle = message2.getDutyCycle();
        assertEquals(0, dutyCycle);
    }

    @Test
    public void getCommandDiscardedTest() {
        boolean commandDiscarded = message1.isCommandDiscarded();
        assertEquals(false, commandDiscarded);
        commandDiscarded = message2.isCommandDiscarded();
        assertEquals(true, commandDiscarded);
    }

    @Test
    public void getFreeMemTest() {
        int freeMemory = message1.getFreeMemorySlots();
        assertEquals(49, freeMemory);
        freeMemory = message2.getDutyCycle();
        assertEquals(0, freeMemory);
    }
}
