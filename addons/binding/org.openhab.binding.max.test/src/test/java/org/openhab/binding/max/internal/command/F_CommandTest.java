/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests cases for {@link F_Command}.
 *
 * @author Marcel Verpaalen - Initial version
 */
public class F_CommandTest {

    @Test
    public void PrefixTest() {
        F_Command scmd = new F_Command();

        String commandStr = scmd.getCommandString();
        String prefix = commandStr.substring(0, 2);

        assertEquals("f:", prefix);
        assertEquals("f:" + '\r' + '\n', commandStr);

    }

    @Test
    public void BaseCommandTest() {
        F_Command scmd = new F_Command("ntp.homematic.com", "nl.ntp.pool.org");
        String commandStr = scmd.getCommandString();
        assertEquals("f:ntp.homematic.com,nl.ntp.pool.org" + '\r' + '\n', commandStr);
    }

    @Test
    public void FCommandNullTest() {
        F_Command scmd = new F_Command("ntp.homematic.com", null);
        String commandStr = scmd.getCommandString();
        assertEquals("f:ntp.homematic.com" + '\r' + '\n', commandStr);

        scmd = new F_Command(null, "nl.ntp.pool.org");
        commandStr = scmd.getCommandString();
        assertEquals("f:nl.ntp.pool.org" + '\r' + '\n', commandStr);

        scmd = new F_Command(null, null);
        commandStr = scmd.getCommandString();
        assertEquals("f:" + '\r' + '\n', commandStr);

    }

}
