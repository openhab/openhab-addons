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
package org.openhab.binding.alarmdecoder.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ADCommand} class represents an alarm decoder command, and contains the static methods and definitions
 * used to construct one. Not all supported AD commands are necessarily used by the current binding.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public final class ADCommand {

    public static final String SPECIAL_KEY_1 = "\u0001\u0001\u0001";
    public static final String SPECIAL_KEY_2 = "\u0002\u0002\u0002";
    public static final String SPECIAL_KEY_3 = "\u0003\u0003\u0003";
    public static final String SPECIAL_KEY_4 = "\u0004\u0004\u0004";
    public static final String SPECIAL_KEY_5 = "\u0005\u0005\u0005";
    public static final String SPECIAL_KEY_6 = "\u0006\u0006\u0006";
    public static final String SPECIAL_KEY_7 = "\u0007\u0007\u0007";
    public static final String SPECIAL_KEY_8 = "\u0008\u0008\u0008";

    public static final int ZONE_OPEN = 1;
    public static final int ZONE_CLOSED = 0;

    // public static final String KEYPAD_COMMAND_CHARACTERS = "0123456789*#<>";
    public static final String KEYPAD_COMMAND_REGEX = "^[0-9A-H*#<>]+$";

    private static final String TERM = "\r\n";

    private static final String COMMAND_REBOOT = "=";
    private static final String COMMAND_CONFIG = "C";
    private static final String COMMAND_ZONE = "L";
    private static final String COMMAND_ERROR = "E";
    private static final String COMMAND_VERSION = "V";
    private static final String COMMAND_ADDRMSG = "K";
    private static final String COMMAND_ACKCRC = "R";

    public final String command;

    public ADCommand(String command) {
        this.command = command + TERM;
    }

    @Override
    public String toString() {
        return command;
    }

    public static ADCommand reboot() {
        return new ADCommand(COMMAND_REBOOT);
    }

    /**
     * Construct an AD configuration command. If configParam is null, a query configuration command will be created.
     * If configParam consists of one or more NAME=value pairs (separated by {@code '&'} characters), a set
     * configuration command will be created. The validity of configParam is not checked.
     *
     * @param configParam String containing parameters to set or null
     * @return ADCommand object containing the constructed command
     */
    public static ADCommand config(@Nullable String configParam) {
        if (configParam == null) {
            return new ADCommand(COMMAND_CONFIG);
        } else {
            return new ADCommand(COMMAND_CONFIG + configParam);
        }
    }

    /**
     * Construct an AD command to set the state of an emulated zone.
     *
     * @param zone The emulated zone number (0-99) for the command.
     * @param state The new state (0 or 1) for the emulated zone.
     * @return ADCommand object containing the constructed command
     * @throws IllegalArgumentException
     */
    public static ADCommand setZone(int zone, int state) throws IllegalArgumentException {
        if (zone < 0 || zone > 99 || state < 0 || state > 1) {
            throw new IllegalArgumentException("Invalid parameter(s)");
        }
        return new ADCommand(String.format("%s%02d%d", COMMAND_ZONE, zone, state));
    }

    /**
     * Construct an AD command to get and clear the error counters.
     *
     * @return ADCommand object containing the constructed command
     */
    public static ADCommand getErrors() {
        return new ADCommand(COMMAND_ERROR);
    }

    /**
     * Construct an AD command to request a version info message.
     *
     * @return ADCommand object containing the constructed command
     */
    public static ADCommand getVersion() {
        return new ADCommand(COMMAND_VERSION);
    }

    /**
     * Construct an AD command to send a message from a specific partition or keypad address, rather than from the Alarm
     * Decoder unit's configured address.
     *
     * @param address The keypad address or partition (0-99) from which to send the command
     * @param message A String containing the message to send. Length must be > 0.
     * @return ADCommand object containing the constructed command
     * @throws IllegalArgumentException
     */
    public static ADCommand addressedMessage(int address, String message) throws IllegalArgumentException {
        if (address < 0 || address > 99 || message.length() < 1) {
            throw new IllegalArgumentException("Invalid parameter(s)");
        }
        return new ADCommand(String.format("%s%02d%s", COMMAND_ADDRMSG, address, message));
    }

    /**
     * Construct an AD command to acknowledge that a received CRC message was valid.
     *
     * @return ADCommand object containing the constructed command
     */
    public static ADCommand ackCRC() {
        return new ADCommand(COMMAND_ACKCRC);
    }
}
