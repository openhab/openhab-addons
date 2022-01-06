/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 * Model for a message sent or received from a UPB modem.
 *
 * @author cvanorman - Initial contribution
 */
@NonNullByDefault
public class UPBMessage {

    /**
     * An enum of possible modem response types.
     */
    public enum Type {
        ACCEPT("PA"),
        BUSY("PB"),
        ERROR("PE"),
        ACK("PK"),
        NAK("PN"),
        MESSAGE_REPORT("PU"),
        NONE("");

        private final byte[] prefix;

        Type(final String prefix) {
            this.prefix = prefix.getBytes(US_ASCII);
        }

        /**
         * Returns the message type for a message buffer.
         *
         * @param buf the byte array to check for a matching type prefix
         * @return the matching message type, or {@code NONE}
         */
        public static Type forPrefix(final byte[] buf) {
            if (buf.length >= 2) {
                for (final Type t : values()) {
                    if (t.prefix.length >= 2 && buf[0] == t.prefix[0] && buf[1] == t.prefix[1]) {
                        return t;
                    }
                }
            }
            return NONE;
        }
    }

    private final Type type;

    private ControlWord controlWord = new ControlWord();
    private byte network;
    private byte destination;
    private byte source;

    private Command command = Command.NULL;
    private byte[] arguments = new byte[0];

    private UPBMessage(final Type type) {
        this.type = type;
    }

    /**
     * Converts a hex string into a {@link UPBMessage}.
     *
     * @param buf
     *            the string as returned by the modem.
     * @return a new UPBMessage.
     */
    public static UPBMessage parse(final byte[] buf) {
        if (buf.length < 2) {
            throw new MessageParseException("message too short");
        }
        final UPBMessage msg = new UPBMessage(Type.forPrefix(buf));

        try {
            if (buf.length >= 15) {
                byte[] data = unhex(buf, 2, buf.length - 1);
                msg.getControlWord().setBytes(data[0], data[1]);
                int index = 2;
                msg.setNetwork(data[index++]);
                msg.setDestination(data[index++]);
                msg.setSource(data[index++]);

                byte commandCode = data[index++];
                msg.setCommand(Command.valueOf(commandCode));

                if (index <= data.length - 1) {
                    msg.setArguments(Arrays.copyOfRange(data, index, data.length - 1));
                }
            }
        } catch (final RuntimeException e) {
            throw new MessageParseException("failed to parse message", e);
        }

        return msg;
    }

    private static byte[] unhex(final byte[] buf, final int start, final int end) {
        final byte[] res = new byte[(end - start) / 2];
        int i = start;
        int j = 0;
        while (i < end - 1) {
            res[j++] = HexUtils.hexToByte(buf[i++], buf[i++]);
        }
        return res;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the controlWord
     */
    public ControlWord getControlWord() {
        return controlWord;
    }

    /**
     * @param controlWord
     *            the controlWord to set
     */
    public void setControlWord(ControlWord controlWord) {
        this.controlWord = controlWord;
    }

    /**
     * @return the network
     */
    public byte getNetwork() {
        return network;
    }

    /**
     * @param network
     *            the network to set
     */
    public void setNetwork(byte network) {
        this.network = network;
    }

    /**
     * @return the destination
     */
    public byte getDestination() {
        return destination;
    }

    /**
     * @param destination
     *            the destination to set
     */
    public void setDestination(byte destination) {
        this.destination = destination;
    }

    /**
     * @return the source
     */
    public byte getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource(byte source) {
        this.source = source;
    }

    /**
     * @return the command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * @param command
     *            the command to set
     */
    public void setCommand(Command command) {
        this.command = command;
    }

    /**
     * @return the arguments
     */
    public byte[] getArguments() {
        return arguments;
    }

    /**
     * @param arguments
     *            the arguments to set
     */
    public void setArguments(byte[] arguments) {
        this.arguments = arguments;
    }
}
