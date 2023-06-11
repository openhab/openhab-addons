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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 * Builder class for building UPB messages.
 *
 * @author cvanorman - Initial contribution
 * @since 1.9.0
 */
@NonNullByDefault
public final class MessageBuilder {

    private byte network;
    private byte source = -1;
    private byte destination;
    private byte command;
    private byte[] args = new byte[0];
    private boolean link;
    private boolean ackMessage;

    private MessageBuilder(final Command cmd) {
        this.command = cmd.toByte();
    }

    /**
     * @return a new MessageBuilder for the specified command
     */
    public static MessageBuilder forCommand(final Command cmd) {
        return new MessageBuilder(cmd);
    }

    /**
     * Sets where this message is for a device or a link.
     *
     * @param link
     *            set to true if this message is for a link.
     * @return this builder
     */
    public MessageBuilder link(boolean link) {
        this.link = link;
        return this;
    }

    /**
     * Sets the UPB network of the message.
     *
     * @param network
     *            the network of the message.
     * @return this builder
     */
    public MessageBuilder network(byte network) {
        this.network = network;
        return this;
    }

    /**
     * Sets the source id of the message (defaults to 0xFF).
     *
     * @param source
     *            the source if of the message.
     * @return this builder
     */
    public MessageBuilder source(byte source) {
        this.source = source;
        return this;
    }

    /**
     * Sets the destination id of the message.
     *
     * @param destination
     *            the destination id.
     * @return this builder
     */
    public MessageBuilder destination(byte destination) {
        this.destination = destination;
        return this;
    }

    /**
     * Sets any command arguments.
     *
     * @param args the arguments (bytes following the command byte)
     * @return this builder
     */
    public MessageBuilder args(byte... args) {
        this.args = args;
        return this;
    }

    /**
     * Sets whether an Acknowledgement Response message should be requested
     * (by setting the the MSG-bit in the control word).
     *
     * @param ackMessage {@code true} if the MSG-bit should be set
     * @return this builder
     */
    public MessageBuilder ackMessage(final boolean ackMessage) {
        this.ackMessage = ackMessage;
        return this;
    }

    /**
     * Builds the message as a HEX string.
     *
     * @return a HEX string of the message.
     */
    public String build() {
        ControlWord controlWord = new ControlWord();

        int packetLength = args.length + 7;

        controlWord.setPacketLength(packetLength);
        controlWord.setAckPulse(true);
        controlWord.setAckMessage(ackMessage);
        controlWord.setLink(link);

        byte[] bytes = new byte[packetLength];
        bytes[0] = controlWord.getHi();
        bytes[1] = controlWord.getLo();
        bytes[2] = network;
        bytes[3] = destination;
        bytes[4] = source;
        bytes[5] = command;
        System.arraycopy(args, 0, bytes, 6, args.length);

        // Calculate the checksum
        // The checksum is the 2's complement of the sum.
        int sum = 0;
        for (byte b : bytes) {
            sum += b;
        }

        bytes[bytes.length - 1] = (byte) (-sum >>> 0);

        return HexUtils.bytesToHex(bytes);
    }
}
