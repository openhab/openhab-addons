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
package org.openhab.binding.paradoxalarm.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.openhab.binding.paradoxalarm.internal.communication.messages.CommandPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.EpromRequestPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderCommand;
import org.openhab.binding.paradoxalarm.internal.communication.messages.IPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.communication.messages.PartitionCommand;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TestGetBytes} This test tests creation of IP packet and it's getBytes() method
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class TestGetBytes {

    private static final int PARTITION_NUMBER = 1;

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "TRACE");
    }

    private static final Logger logger = LoggerFactory.getLogger(ParadoxUtil.class);

    private static final byte[] EXPECTED1 = { (byte) 0xAA, 0x0A, 0x00, 0x03, 0x08, (byte) 0xF0, 0x00, 0x00, 0x01,
            (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x01, 0x02, 0x03,
            0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10 };

    private static final byte[] PAYLOAD = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10 };

    @Test
    public void testParadoxIPPacket() {
        ParadoxIPPacket paradoxIPPacket = new ParadoxIPPacket(PAYLOAD, false)
                .setCommand(HeaderCommand.CONNECT_TO_IP_MODULE);
        final byte[] packetBytes = paradoxIPPacket.getBytes();

        ParadoxUtil.printByteArray("Expected =", EXPECTED1);
        ParadoxUtil.printByteArray("Packet   =", packetBytes);

        assertTrue(Arrays.equals(packetBytes, EXPECTED1));
    }

    private static final byte[] EXPECTED_COMMAND_PAYLOAD = { 0x40, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00 };

    @Test
    public void testCommandPayload() {
        CommandPayload payload = new CommandPayload(PARTITION_NUMBER, PartitionCommand.ARM);
        final byte[] packetBytes = payload.getBytes();

        ParadoxUtil.printByteArray("Expected =", EXPECTED_COMMAND_PAYLOAD);
        ParadoxUtil.printByteArray("Result   =", packetBytes);

        assertTrue(Arrays.equals(packetBytes, EXPECTED_COMMAND_PAYLOAD));
    }

    private static final byte[] EXPECTED_MEMORY_PAYLOAD = { (byte) 0xAA, 0x0A, 0x00, 0x03, 0x08, (byte) 0xF0, 0x00,
            0x00, 0x01, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x01,
            0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10 };

    @Test
    public void testMemoryRequestPayload() throws ParadoxException {
        int address = 0x3A6B + (PARTITION_NUMBER) * 107;
        byte labelLength = 16;
        IPayload payload = new EpromRequestPayload(address, labelLength);
        byte[] bytes = payload.getBytes();
        ParadoxUtil.printByteArray("Expected =", EXPECTED_MEMORY_PAYLOAD);
        ParadoxUtil.printByteArray("Result   =", bytes);
    }
}
