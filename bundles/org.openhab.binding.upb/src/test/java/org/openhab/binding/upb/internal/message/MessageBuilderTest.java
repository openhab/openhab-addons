/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.upb.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class MessageBuilderTest {
    @Test
    public void testActivateCmd() {
        final MessageBuilder msg = MessageBuilder.forCommand(Command.ACTIVATE).network((byte) 1).destination((byte) 2);
        assertEquals("07100102FF20C7", msg.build());
    }

    @Test
    public void testGoto() {
        final MessageBuilder msg = MessageBuilder.forCommand(Command.GOTO).args((byte) 0x32).network((byte) 1)
                .destination((byte) 2);
        assertEquals("08100102FF223292", msg.build());
    }

    @Test
    public void testDeactivateLink() {
        final MessageBuilder msg = MessageBuilder.forCommand(Command.DEACTIVATE).network((byte) 1).destination((byte) 2)
                .link(true);
        assertEquals("87100102FF2146", msg.build());
    }
}
