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
package org.openhab.binding.powermax.internal.state;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * All defined arm modes
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum PowermaxArmMode {

    DISARMED(0, "Disarmed", "Disarmed", false, (byte) 0x00, false),
    HOME_EXIT_DELAY(1, "Home Exit Delay", "ExitDelay", false, (byte) 0xFF, false),
    AWAY_EXIT_DELAY(2, "Away Exit Delay", "ExitDelay", false, (byte) 0xFF, false),
    ENTRY_DELAY(3, "Entry Delay", "EntryDelay", true, (byte) 0xFF, false),
    ARMED_HOME(4, "Armed Home", "Stay", true, (byte) 0x04, false),
    ARMED_AWAY(5, "Armed Away", "Armed", true, (byte) 0x05, false),
    USER_TEST(6, "User Test", "UserTest", false, (byte) 0xFF, false),
    DOWNLOADING(7, "Downloading", "NotReady", false, (byte) 0xFF, false),
    PROGRAMMING(8, "Programming", "NotReady", false, (byte) 0xFF, false),
    INSTALLER(9, "Installer", "NotReady", false, (byte) 0xFF, false),
    HOME_BYPASS(10, "Home Bypass", "Force", true, (byte) 0xFF, false),
    AWAY_BYPASS(11, "Away Bypass", "Force", true, (byte) 0xFF, false),
    READY(12, "Ready", "Ready", false, (byte) 0xFF, false),
    NOT_READY(13, "Not Ready", "NotReady", false, (byte) 0xFF, false),
    ARMED_NIGHT(14, "Armed Night", "Night", true, (byte) 0x04, false),
    ARMED_NIGHT_INSTANT(15, "Armed Night Instant", "NightInstant", true, (byte) 0x14, false),
    DISARMED_INSTANT(16, "Disarmed Instant", "DisarmedInstant", false, (byte) 0xFF, false),
    HOME_INSTANT_EXIT_DELAY(17, "Home Instant Exit Delay", "ExitDelay", false, (byte) 0xFF, false),
    AWAY_INSTANT_EXIT_DELAY(18, "Away Instant Exit Delay", "ExitDelay", false, (byte) 0xFF, false),
    ENTRY_DELAY_INSTANT(19, "Entry Delay Instant", "EntryDelay", true, (byte) 0xFF, false),
    ARMED_HOME_INSTANT(20, "Armed Home Instant", "StayInstant", true, (byte) 0x14, false),
    ARMED_AWAY_INSTANT(21, "Armed Away Instant", "ArmedInstant", true, (byte) 0x15, false);

    private final int code;
    private final String name;
    private final String shortName;
    private final boolean armed;
    private final byte commandCode;
    private boolean allowedCommand;

    private PowermaxArmMode(int code, String name, String shortName, boolean armed, byte commandCode,
            boolean allowedCommand) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.armed = armed;
        this.commandCode = commandCode;
        this.allowedCommand = allowedCommand;
    }

    /**
     * @return the code identifying the mode
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the full mode name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @return true if the mode is considered as armed
     */
    public boolean isArmed() {
        return armed;
    }

    /**
     * @return the command code
     */
    public byte getCommandCode() {
        return commandCode;
    }

    /**
     * @return true if the mode is an allowed command
     */
    public boolean isAllowedCommand() {
        return allowedCommand;
    }

    /**
     * Set whether the mode is an allowed command or not
     * To be allowed, the mode must have a valid command code.
     *
     * @param allowedCommand true if the mode must be an allowed command
     */
    public void setAllowedCommand(boolean allowedCommand) {
        this.allowedCommand = getCommandCode() == 0xFF ? false : allowedCommand;
    }

    /**
     * Get the ENUM value from its code
     *
     * @param code the code identifying the mode
     *
     * @return the corresponding ENUM value
     *
     * @throws IllegalArgumentException if no ENUM value corresponds to this code
     */
    public static PowermaxArmMode fromCode(int code) throws IllegalArgumentException {
        for (PowermaxArmMode mode : PowermaxArmMode.values()) {
            if (mode.getCode() == code) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }

    /**
     * Get the ENUM value from its name
     *
     * @param name the full mode name
     *
     * @return the corresponding ENUM value
     *
     * @throws IllegalArgumentException if no ENUM value corresponds to this name
     */
    public static PowermaxArmMode fromName(String name) throws IllegalArgumentException {
        for (PowermaxArmMode mode : PowermaxArmMode.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Invalid name: " + name);
    }

    /**
     * Get the ENUM value from its short name
     *
     * @param shortName the mode short name
     *
     * @return the first corresponding ENUM value
     *
     * @throws IllegalArgumentException if no ENUM value corresponds to this short name
     */
    public static PowermaxArmMode fromShortName(String shortName) throws IllegalArgumentException {
        for (PowermaxArmMode mode : PowermaxArmMode.values()) {
            if (mode.getShortName().equalsIgnoreCase(shortName)) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Invalid short name: " + shortName);
    }
}
