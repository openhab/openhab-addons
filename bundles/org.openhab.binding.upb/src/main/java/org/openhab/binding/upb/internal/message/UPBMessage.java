/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model for a message sent or received from a UPB modem.
 *
 * @author cvanorman - Initial contribution
 */
@NonNullByDefault
public class UPBMessage {

    /**
     * An enum of possible modem response types.
     *
     * @author cvanorman
     *
     */
    public enum Type {
        ACCEPT("PA"),
        BUSY("PB"),
        ERROR("PE"),
        ACK("PK"),
        NAK("PN"),
        MESSAGE_REPORT("PU"),
        NONE("");

        private final String prefix;

        Type(final String prefix) {
            this.prefix = prefix;
        }

        /**
         * @return the message prefix string for this type
         */
        public String prefix() {
            return prefix;
        }

        /**
         * Returns the message type for a protocol string prefix.
         *
         * @param value the prefix string
         * @return the message type for the given string
         */
        public static Type forPrefix(final String value) {
            for (final Type t : values()) {
                if (t.prefix().equals(value)) {
                    return t;
                }
            }
            return NONE;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(UPBMessage.class);

    private final Type type;

    private ControlWord controlWord = new ControlWord();
    private byte network;
    private byte destination;
    private byte source;

    private Command command = Command.NONE;
    private byte @Nullable [] arguments;

    private UPBMessage(final Type type) {
        this.type = type;
    }

    /**
     * Converts a hex string into a {@link UPBMessage}.
     *
     * @param commandString
     *                          the string as returned by the modem.
     * @return a new UPBMessage.
     */
    public static UPBMessage fromString(String commandString) {
        final String prefix = commandString.substring(0, 2);
        final UPBMessage msg = new UPBMessage(Type.forPrefix(prefix));

        try {
            if (commandString.length() > 2) {
                byte[] data = HexUtils.hexToBytes(commandString.substring(2));
                msg.getControlWord().setBytes(data[1], data[0]);
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
        } catch (Exception e) {
            logger.warn("Attempted to parse invalid message: {}", commandString, e);
        }

        return msg;
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
     *                        the controlWord to set
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
     *                    the network to set
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
     *                        the destination to set
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
     *                   the source to set
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
     *                    the command to set
     */
    public void setCommand(Command command) {
        this.command = command;
    }

    /**
     * @return the arguments
     */
    public byte @Nullable [] getArguments() {
        return arguments;
    }

    /**
     * @param arguments
     *                      the arguments to set
     */
    public void setArguments(byte[] arguments) {
        this.arguments = arguments;
    }
}
