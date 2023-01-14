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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.openhab.binding.rfxcom.internal.RFXComTestHelper.commandChannelUID;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.HOME_CONFORT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComHomeConfortMessage.SubType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 * @author Mike Jagdis - added message handling and real test
 */
@NonNullByDefault
public class RFXComHomeConfortMessageTest {
    private void testMessage(SubType subType, Command command, String deviceId, String data) throws RFXComException {
        RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();

        config.deviceId = deviceId;
        config.subType = subType.toString();

        RFXComHomeConfortMessage message = (RFXComHomeConfortMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HOME_CONFORT, config, commandChannelUID, command);

        assertArrayEquals(HexUtils.hexToBytes(data), message.decodeMessage());
    }

    @Test
    public void testMessage1() throws RFXComException {
        testMessage(SubType.TEL_010, OnOffType.ON, "1118739.A.4", "0C1B0000111213410401000000");
    }
}
