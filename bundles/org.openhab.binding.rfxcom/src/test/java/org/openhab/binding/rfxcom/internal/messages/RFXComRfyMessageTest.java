/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openhab.binding.rfxcom.internal.RFXComTestHelper.thingUID;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.RFY;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.RFXComBindingConstants;
import org.openhab.binding.rfxcom.internal.RFXComTestHelper;
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.messages.RFXComRfyMessage.SubType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 * @author Leo Siepel - Remove contact command support
 */
@NonNullByDefault
public class RFXComRfyMessageTest {
    private static ChannelUID shutterChannelUID = new ChannelUID(thingUID, RFXComBindingConstants.CHANNEL_SHUTTER);
    private static ChannelUID venetionBlindChannelUID = new ChannelUID(thingUID,
            RFXComBindingConstants.CHANNEL_VENETIAN_BLIND);

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();
        config.deviceId = "1.1";
        config.subType = SubType.RFY.toString();

        RFXComRfyMessage message = (RFXComRfyMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(RFY, config,
                shutterChannelUID, UpDownType.UP);

        RFXComTestHelper.basicBoundaryCheck(RFY, message);
    }

    private void testMessage(SubType subType, Command command, String deviceId, String data) throws RFXComException {
        RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();
        config.deviceId = "66051.4";
        config.subType = subType.toString();

        RFXComRfyMessage message = (RFXComRfyMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(RFY, config,
                venetionBlindChannelUID, command);

        assertArrayEquals(HexUtils.hexToBytes(data), message.decodeMessage());
    }

    @Test
    public void testMessage1() throws RFXComException {
        testMessage(SubType.RFY, OnOffType.OFF, "66051.4", "0C1A0000010203040F00000000");
    }

    @Test
    public void testSupportedShutterCommands() throws RFXComException {
        RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();
        config.deviceId = "66051.4";
        config.subType = SubType.RFY.toString();
        for (UpDownType command : UpDownType.values()) {
            RFXComRfyMessage message = (RFXComRfyMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(RFY, config,
                    shutterChannelUID, command);
            assertEquals(command == UpDownType.UP ? RFXComRfyMessage.Commands.UP : RFXComRfyMessage.Commands.DOWN,
                    message.command);
        }
        for (OnOffType command : OnOffType.values()) {
            RFXComRfyMessage message = (RFXComRfyMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(RFY, config,
                    venetionBlindChannelUID, command);
            assertEquals(
                    command == OnOffType.ON ? RFXComRfyMessage.Commands.DOWN_SHORT : RFXComRfyMessage.Commands.UP_SHORT,
                    message.command);
        }
    }

    @Test
    public void testContactStatesAreNotCommands() {
        RFXComRfyMessage message = new RFXComRfyMessage();
        for (OpenClosedType state : OpenClosedType.values()) {
            assertThrows(RFXComUnsupportedChannelException.class,
                    () -> message.convertFromState(RFXComBindingConstants.CHANNEL_SHUTTER, state));
            assertThrows(RFXComUnsupportedChannelException.class,
                    () -> message.convertFromState(RFXComBindingConstants.CHANNEL_VENETIAN_BLIND, state));
        }
    }

    @Test
    public void testMessage2() throws RFXComException {
        testMessage(SubType.ASA, IncreaseDecreaseType.INCREASE, "66051.4", "0C1A0300010203041200000000");
    }
}
