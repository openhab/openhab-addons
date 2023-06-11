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
package org.openhab.binding.onkyo.internal.eiscp;

/**
 * Represents all possible eISCP commands.
 *
 * @author Thomas.Eichstaedt-Engelen - initial contribution
 * @author Pauli Anttila - add additional commands
 * @author Paul Frank - update for openHAB 2
 * @author Marcel Verpaalen - fix issues with some Zone 2 commands
 */
public enum EiscpCommand {

    // Main zone

    POWER_QUERY("PWR", "QSTN"),
    POWER_SET("PWR", "%02X"),
    POWER("PWR", ""),

    MUTE_QUERY("AMT", "QSTN"),
    MUTE_SET("AMT", "%02X"),
    MUTE("AMT", ""),

    VOLUME_UP("MVL", "UP"),
    VOLUME_DOWN("MVL", "DOWN"),
    VOLUME_QUERY("MVL", "QSTN"),
    VOLUME_SET("MVL", "%02X"),
    VOLUME("MVL", ""),

    AUDIOINFO("IFA", ""),
    AUDIOINFO_QUERY("IFA", "QSTN"),
    VIDEOINFO("IFV", ""),
    VIDEOINFO_QUERY("IFV", "QSTN"),

    SOURCE_UP("SLI", "UP"),
    SOURCE_DOWN("SLI", "DOWN"),
    SOURCE_QUERY("SLI", "QSTN"),
    SOURCE_SET("SLI", "%02X"),
    SOURCE("SLI", ""),

    LISTEN_MODE_UP("LMD", "UP"),
    LISTEN_MODE_DOWN("LMD", "DOWN"),
    LISTEN_MODE_QUERY("LMD", "QSTN"),
    LISTEN_MODE_SET("LMD", "%02X"),
    LISTEN_MODE("LMD", ""),

    INFO_QUERY("NRI", "QSTN"),
    INFO("NRI", ""),

    NETUSB_OP_PLAY("NTC", "PLAY"),
    NETUSB_OP_STOP("NTC", "STOP"),
    NETUSB_OP_PAUSE("NTC", "PAUSE"),
    NETUSB_OP_TRACKUP("NTC", "TRUP"),
    NETUSB_OP_TRACKDWN("NTC", "TRDN"),
    NETUSB_OP_FF("NTC", "FF"),
    NETUSB_OP_REW("NTC", "REW"),
    NETUSB_OP_REPEAT("NTC", "REPEAT"),
    NETUSB_OP_RANDOM("NTC", "RANDOM"),
    NETUSB_OP_DISPLAY("NTC", "DISPLAY"),
    NETUSB_OP_RIGHT("NTC", "RIGHT"),
    NETUSB_OP_LEFT("NTC", "LEFT"),
    NETUSB_OP_UP("NTC", "UP"),
    NETUSB_OP_DOWN("NTC", "DOWN"),
    NETUSB_OP_SELECT("NTC", "SELECT"),
    NETUSB_OP_1("NTC", "1"),
    NETUSB_OP_2("NTC", "2"),
    NETUSB_OP_3("NTC", "3"),
    NETUSB_OP_4("NTC", "4"),
    NETUSB_OP_5("NTC", "5"),
    NETUSB_OP_6("NTC", "6"),
    NETUSB_OP_7("NTC", "7"),
    NETUSB_OP_8("NTC", "8"),
    NETUSB_OP_9("NTC", "9"),
    NETUSB_OP_0("NTC", "0"),
    NETUSB_OP_DELETE("NTC", "DELETE"),
    NETUSB_OP_CAPS("NTC", "CAPS"),
    NETUSB_OP_SETUP("NTC", "SETUP"),
    NETUSB_OP_RETURN("NTC", "RETURN"),
    NETUSB_OP_CHANUP("NTC", "CHUP"),
    NETUSB_OP_CHANDWN("NTC", "CHDN"),
    NETUSB_OP_MENU("NTC", "MENU"),
    NETUSB_OP_TOPMENU("NTC", "TOP"),

    NETUSB_SONG_ARTIST_QUERY("NAT", "QSTN"),
    NETUSB_SONG_ARTIST("NAT", ""),
    NETUSB_SONG_ALBUM_QUERY("NAL", "QSTN"),
    NETUSB_SONG_ALBUM("NAL", ""),
    NETUSB_SONG_TITLE_QUERY("NTI", "QSTN"),
    NETUSB_SONG_TITLE("NTI", ""),
    NETUSB_SONG_ELAPSEDTIME_QUERY("NTM", "QSTN"),
    NETUSB_SONG_ELAPSEDTIME("NTM", ""),
    NETUSB_SONG_TRACK_QUERY("NTR", "QSTN"),
    NETUSB_SONG_TRACK("NTR", ""),
    NETUSB_PLAY_STATUS_QUERY("NST", "QSTN"),
    NETUSB_PLAY_STATUS("NST", ""),

    NETUSB_MENU_SELECT("NLS", "L%X"),
    NETUSB_MENU("NLS", ""),

    NETUSB_TITLE("NLT", ""),
    NETUSB_TITLE_QUERY("NLT", "QSTN"),

    NETUSB_ALBUM_ART_QUERY("NJA", "REQ"),
    NETUSB_ALBUM_ART("NJA", ""),

    /*
     * Zone 2
     */

    ZONE2_POWER_QUERY("ZPW", "QSTN"),
    ZONE2_POWER_SET("ZPW", "%02X"),
    ZONE2_POWER("ZPW", ""),

    ZONE2_MUTE_QUERY("ZMT", "QSTN"),
    ZONE2_MUTE_SET("ZMT", "%02X"),
    ZONE2_MUTE("ZMT", ""),

    ZONE2_VOLUME_UP("ZVL", "UP"),
    ZONE2_VOLUME_DOWN("ZVL", "DOWN"),
    ZONE2_VOLUME_QUERY("ZVL", "QSTN"),
    ZONE2_VOLUME_SET("ZVL", "%02X"),
    ZONE2_VOLUME("ZVL", ""),

    ZONE2_SOURCE_UP("SLZ", "UP"),
    ZONE2_SOURCE_DOWN("SLZ", "DOWN"),
    ZONE2_SOURCE_QUERY("SLZ", "QSTN"),
    ZONE2_SOURCE_SET("SLZ", "%02X"),
    ZONE2_SOURCE("SLZ", ""),

    /*
     * Zone 3
     */

    ZONE3_POWER_QUERY("PW3", "QSTN"),
    ZONE3_POWER_SET("PW3", "%02X"),
    ZONE3_POWER("PW3", ""),

    ZONE3_MUTE_QUERY("MT3", "QSTN"),
    ZONE3_MUTE_SET("MT3", "%02X"),
    ZONE3_MUTE("MT3", ""),

    ZONE3_VOLUME_UP("VL3", "UP"),
    ZONE3_VOLUME_DOWN("VL3", "DOWN"),
    ZONE3_VOLUME_QUERY("VL3", "QSTN"),
    ZONE3_VOLUME_SET("VL3", "%02X"),
    ZONE3_VOLUME("VL3", ""),

    ZONE3_SOURCE_UP("SL3", "UP"),
    ZONE3_SOURCE_DOWN("SL3", "DOWN"),
    ZONE3_SOURCE_QUERY("SL3", "QSTN"),
    ZONE3_SOURCE_SET("SL3", "%02X"),
    ZONE3_SOURCE("SL3", "");

    public static enum Zone {
        MAIN,
        ZONE1,
        ZONE2,
        ZONE3
    }

    private String command;
    private String value;

    private EiscpCommand(String command, String value) {
        this.command = command;
        this.value = value;
    }

    /**
     * @return the iscp command string (example 'PWR')
     */
    public String getCommand() {
        return command;
    }

    /**
     * @return the iscp value string (example 'QSTN')
     */
    public String getValue() {
        return value;
    }

    public static EiscpCommand getCommandForZone(Zone zone, EiscpCommand baseCommand) throws IllegalArgumentException {
        if (zone == Zone.MAIN || zone == Zone.ZONE1) {
            return baseCommand;
        } else {
            return EiscpCommand.valueOf(zone.toString() + "_" + baseCommand);
        }
    }

    /**
     * @param command the command to find a matching command name for.
     * @return the commandName that is associated with the passed command.
     */
    public static EiscpCommand getCommandByCommandStr(String command) throws IllegalArgumentException {
        for (EiscpCommand candidate : values()) {
            if (candidate.getCommand().equals(command)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("There is no matching commandName for command '" + command + "'");
    }

    /**
     * @param command the command to find a matching command name for.
     * @param value the value to find a matching value for.
     *
     * @return the commandName that is associated with the passed command.
     */
    public static EiscpCommand getCommandByCommandAndValueStr(String command, String value)
            throws IllegalArgumentException {
        for (EiscpCommand candidate : values()) {
            if (candidate.getCommand().equals(command) && candidate.getValue().equals(value)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(
                "There is no matching commandName for command '" + command + "' and value '" + value + "'");
    }
}
