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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.rfxcom.internal.RFXComTestHelper.commandChannelUID;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.config.RFXComRawDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author James Hewitt-Thomas - New addition to the PRO RFXCom firmware
 */
@NonNullByDefault
public class RFXComRawMessageTest {
    private void testMessageRx(String hexMsg, RFXComRawMessage.SubType subType, int seqNbr, int repeat, String pulses)
            throws RFXComException {
        final RFXComRawMessage msg = (RFXComRawMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals("RAW", msg.getDeviceId(), "Device Id");
        assertEquals(repeat, msg.repeat, "Repeat");
        byte[] payload = new byte[msg.pulses.length * 2];
        ByteBuffer.wrap(payload).asShortBuffer().put(msg.pulses);
        assertEquals(pulses, HexUtils.bytesToHex(payload), "Pulses");
    }

    @Test
    public void testSomeRxMessages() throws RFXComException {
        testMessageRx("087F0027051356ECC0", RFXComRawMessage.SubType.RAW_PACKET1, 0x27, 5, "1356ECC0");
    }

    private void testMessageTx(RFXComRawDeviceConfiguration config, Command command, String hexMsg)
            throws RFXComException {
        RFXComRawMessage msg = (RFXComRawMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(PacketType.RAW,
                config, commandChannelUID, command);
        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Transmitted message");
    }

    @Test
    public void testTxBasicPulses() throws RFXComException {
        RFXComRawDeviceConfiguration config = new RFXComRawDeviceConfiguration();
        config.deviceId = "RAW";
        config.subType = "RAW_PACKET1";
        config.repeat = 5;
        config.onPulsesArray = new short[] { 0x10, 0x20, 0x30, 0x40 };

        testMessageTx(config, OnOffType.ON, "0C7F0000050010002000300040");
    }

    @Test
    public void testTxMissingPulses() throws RFXComException {
        RFXComRawDeviceConfiguration config = new RFXComRawDeviceConfiguration();
        config.deviceId = "RAW";
        config.subType = "RAW_PACKET1";
        config.repeat = 5;
        config.onPulsesArray = new short[] { 0x10, 0x20, 0x30, 0x40 };

        assertThrows(RFXComInvalidStateException.class,
                () -> testMessageTx(config, OnOffType.OFF, "0C7F0000050010002000300040"));
    }
}
