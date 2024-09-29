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
package org.openhab.binding.anthem.internal.handler;

import static org.openhab.binding.anthem.internal.AnthemBindingConstants.COMMAND_TERMINATION_CHAR;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AnthemCommand} is responsible for creating commands to be sent to the
 * Anthem processor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemCommand {
    private static final String COMMAND_TERMINATOR = String.valueOf(COMMAND_TERMINATION_CHAR);

    private String command = "";

    public AnthemCommand(String command) {
        this.command = command;
    }

    public static AnthemCommand powerOn(Zone zone) {
        return new AnthemCommand(String.format("Z%sPOW1", zone.getValue()));
    }

    public static AnthemCommand powerOff(Zone zone) {
        return new AnthemCommand(String.format("Z%sPOW0", zone.getValue()));
    }

    public static AnthemCommand volumeUp(Zone zone, int amount) {
        return new AnthemCommand(String.format("Z%sVUP%02d", zone.getValue(), amount));
    }

    public static AnthemCommand volumeDown(Zone zone, int amount) {
        return new AnthemCommand(String.format("Z%sVDN%02d", zone.getValue(), amount));
    }

    public static AnthemCommand volume(Zone zone, int level) {
        return new AnthemCommand(String.format("Z%sVOL%02d", zone.getValue(), level));
    }

    public static AnthemCommand muteOn(Zone zone) {
        return new AnthemCommand(String.format("Z%sMUT1", zone.getValue()));
    }

    public static AnthemCommand muteOff(Zone zone) {
        return new AnthemCommand(String.format("Z%sMUT0", zone.getValue()));
    }

    public static AnthemCommand activeInput(Zone zone, int input) {
        return new AnthemCommand(String.format("Z%sINP%02d", zone.getValue(), input));
    }

    public static AnthemCommand queryPower(Zone zone) {
        return new AnthemCommand(String.format("Z%sPOW?", zone.getValue()));
    }

    public static AnthemCommand queryVolume(Zone zone) {
        return new AnthemCommand(String.format("Z%sVOL?", zone.getValue()));
    }

    public static AnthemCommand queryMute(Zone zone) {
        return new AnthemCommand(String.format("Z%sMUT?", zone.getValue()));
    }

    public static AnthemCommand queryActiveInput(Zone zone) {
        return new AnthemCommand(String.format("Z%sINP?", zone.getValue()));
    }

    public static AnthemCommand queryNumAvailableInputs() {
        return new AnthemCommand(String.format("ICN?"));
    }

    public static AnthemCommand queryInputShortName(int input) {
        return new AnthemCommand(String.format("ISN%02d?", input));
    }

    public static AnthemCommand queryInputLongName(int input) {
        return new AnthemCommand(String.format("ILN%02d?", input));
    }

    public static AnthemCommand queryModel() {
        return new AnthemCommand("IDM?");
    }

    public static AnthemCommand queryRegion() {
        return new AnthemCommand("IDR?");
    }

    public static AnthemCommand querySoftwareVersion() {
        return new AnthemCommand("IDS?");
    }

    public static AnthemCommand querySoftwareBuildDate() {
        return new AnthemCommand("IDB?");
    }

    public static AnthemCommand queryHardwareVersion() {
        return new AnthemCommand("IDH?");
    }

    public static AnthemCommand queryMacAddress() {
        return new AnthemCommand("IDN?");
    }

    public static AnthemCommand customCommand(String customCommand) {
        return new AnthemCommand(customCommand);
    }

    public String getCommand() {
        return command + COMMAND_TERMINATOR;
    }

    @Override
    public String toString() {
        return getCommand();
    }
}
