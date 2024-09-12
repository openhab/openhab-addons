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
package org.openhab.binding.elerotransmitterstick.internal.stick;

import java.nio.ByteBuffer;

/**
 * @author Volker Bier - Initial contribution
 */
public class CommandUtil {
    /**
     * Create the two channel bytes for the given channel IDs
     *
     * @param channelIds channel ids (starting from 1)
     */
    private static byte[] createChannelBits(Integer... channelIds) {
        long channels = 0;

        for (int id : channelIds) {
            channels = channels + (1 << (id - 1));
        }

        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort((short) (channels % 32768));

        return buffer.array();
    }

    public static CommandPacket createPacket(Command cmd, Integer channelId) {
        return createPacket(cmd.getCommandType(), new Integer[] { channelId });
    }

    public static CommandPacket createPacket(Command cmd) {
        return createPacket(cmd.getCommandType(), cmd.getChannelIds());
    }

    public static CommandPacket createPacket(CommandType cmd, Integer... channelIds) {
        if (cmd == CommandType.INFO) {
            byte[] channelBits = createChannelBits(channelIds);

            return new CommandPacket(
                    new byte[] { (byte) 0xAA, 0x04, CommandPacket.EASY_INFO, channelBits[0], channelBits[1] });
        }

        if (cmd == CommandType.CHECK) {
            return new CommandPacket(new byte[] { (byte) 0xaa, (byte) 0x02, CommandPacket.EASY_CHECK });
        }

        byte[] channelBits = createChannelBits(channelIds);
        byte cmdByte = getCommandByte(cmd);

        return new CommandPacket(
                new byte[] { (byte) 0xAA, 0x05, CommandPacket.EASY_SEND, channelBits[0], channelBits[1], cmdByte });
    }

    private static byte getCommandByte(CommandType command) {
        switch (command) {
            case DOWN:
                return (byte) 0x40;
            case INTERMEDIATE:
                return (byte) 0x44;
            case STOP:
                return (byte) 0x10;
            case UP:
                return (byte) 0x20;
            case VENTILATION:
                return (byte) 0x24;
            default:
                throw new IllegalArgumentException("Unhandled command type " + command);
        }
    }
}
