/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertArrayEquals;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.HOME_CONFORT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComHomeConfortMessage.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComHomeConfortMessage.SubType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 * @author Mike Jagdis - added message handling and real test
 */
@NonNullByDefault
public class RFXComHomeConfortTest {
    private void testMessage(SubType subType, Commands command, String deviceId, String data) throws RFXComException {
        RFXComHomeConfortMessage message = (RFXComHomeConfortMessage) RFXComMessageFactory.createMessage(HOME_CONFORT);
        message.setSubType(subType);
        message.command = command;
        message.setDeviceId(deviceId);

        assertArrayEquals(HexUtils.hexToBytes(data), message.decodeMessage());
    }

    @Test
    public void testMessage1() throws RFXComException {
        testMessage(SubType.TEL_010, Commands.GROUP_ON, "1118739.A.4", "0C1B0000111213410403000000");
    }
}
