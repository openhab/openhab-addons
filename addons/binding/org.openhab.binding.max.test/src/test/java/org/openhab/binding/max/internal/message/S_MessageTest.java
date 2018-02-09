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
public class S_MessageTest {

    private final String rawData1 = "S:01,0,31";
    private final String rawData2 = "S:00,1,00";

    private S_Message message1 = null;
    private S_Message message2 = null;

    @Before
    public void Before() {
        message1 = new S_Message(rawData1);
        message2 = new S_Message(rawData2);
    }

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
