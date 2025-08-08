/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bambulab.internal;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.copyOfRange;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.LedMode.FLASHING;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import pl.grzeslowski.jbambuapi.mqtt.PrinterClient;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class CommandParser {

    public static final String SEQUENCE_ID = "%sequence_id%";

    public static PrinterClient.Channel.Command parseCommand(String stringCommand) {
        var split = stringCommand.split(":");
        if (split.length <= 1) {
            throw new IllegalArgumentException("Command too short, class name not passed. Command: " + stringCommand);
        }
        var commandName = split[0] + "Command";
        var tail = tail(split);
        if (commandName.equals(PrinterClient.Channel.InfoCommand.class.getSimpleName())) {
            return parseInfoCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.PushingCommand.class.getSimpleName())) {
            return parsePushingCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.PrintCommand.class.getSimpleName())) {
            return parsePrintCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.ChangeFilamentCommand.class.getSimpleName())) {
            return parseChangeFilamentCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.AmsUserSettingCommand.class.getSimpleName())) {
            return parseAmsUserSettingCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.AmsFilamentSettingCommand.class.getSimpleName())) {
            return parseAmsFilamentSettingCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.AmsControlCommand.class.getSimpleName())) {
            return parseAmsControlCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.PrintSpeedCommand.class.getSimpleName())) {
            return parsePrintSpeedCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.GCodeFileCommand.class.getSimpleName())) {
            return parseGCodeFileCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.GCodeLineCommand.class.getSimpleName())) {
            var gcodeLineSplit = stringCommand.split(":", 2);
            requireLength(gcodeLineSplit, 2);
            return parseGCodeLineCommand(gcodeLineSplit[1]);
        }
        if (commandName.equals(PrinterClient.Channel.LedControlCommand.class.getSimpleName())) {
            return parseLedControlCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.SystemCommand.class.getSimpleName())) {
            return parseSystemCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.IpCamRecordCommand.class.getSimpleName())) {
            return parseIpCamRecordCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.IpCamTimelapsCommand.class.getSimpleName())) {
            return parseIpCamTimelapsCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.XCamControlCommand.class.getSimpleName())) {
            return parseXCamControlCommand(tail);
        }
        if (commandName.equals(PrinterClient.Channel.RawCommand.class.getSimpleName())//
                || commandName.equals(PrinterClient.Channel.RawStringCommand.class.getSimpleName())) {
            return parseRawCommand(tail);
        }

        throw new IllegalArgumentException("Unknown command name: " + commandName);
    }

    private static String[] tail(String[] command) {
        return copyOfRange(command, 1, command.length);
    }

    private static void requireLength(String[] commandLine, int length) {
        if (commandLine.length != length) {
            throw new IllegalArgumentException("Command line length does not match! Should be %s, but was %s!"
                    .formatted(length, commandLine.length));
        }
    }

    private static PrinterClient.Channel.InfoCommand parseInfoCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return PrinterClient.Channel.InfoCommand.valueOf(commandLine[0]);
    }

    private static PrinterClient.Channel.PushingCommand parsePushingCommand(String[] commandLine) {
        if (commandLine.length == 0) {
            return PrinterClient.Channel.PushingCommand.defaultPushingCommand();
        }
        requireLength(commandLine, 2);
        return new PrinterClient.Channel.PushingCommand(parseInt(commandLine[0]), parseInt(commandLine[1]));
    }

    private static PrinterClient.Channel.PrintCommand parsePrintCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return PrinterClient.Channel.PrintCommand.valueOf(commandLine[0]);
    }

    private static PrinterClient.Channel.ChangeFilamentCommand parseChangeFilamentCommand(String[] commandLine) {
        requireLength(commandLine, 3);
        return new PrinterClient.Channel.ChangeFilamentCommand(parseInt(commandLine[0]), parseInt(commandLine[1]),
                parseInt(commandLine[2]));
    }

    private static PrinterClient.Channel.AmsUserSettingCommand parseAmsUserSettingCommand(String[] commandLine) {
        requireLength(commandLine, 3);
        return new PrinterClient.Channel.AmsUserSettingCommand(parseInt(commandLine[0]), parseBoolean(commandLine[1]),
                parseBoolean(commandLine[2]));
    }

    private static PrinterClient.Channel.AmsFilamentSettingCommand parseAmsFilamentSettingCommand(
            String[] commandLine) {
        requireLength(commandLine, 7);
        return new PrinterClient.Channel.AmsFilamentSettingCommand(parseInt(commandLine[0]), parseInt(commandLine[1]),
                commandLine[2], commandLine[3], parseInt(commandLine[4]), parseInt(commandLine[5]), commandLine[6]);
    }

    private static PrinterClient.Channel.AmsControlCommand parseAmsControlCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return PrinterClient.Channel.AmsControlCommand.valueOf(commandLine[0]);
    }

    private static PrinterClient.Channel.PrintSpeedCommand parsePrintSpeedCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return PrinterClient.Channel.PrintSpeedCommand.findByName(commandLine[0]);
    }

    private static PrinterClient.Channel.GCodeFileCommand parseGCodeFileCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return new PrinterClient.Channel.GCodeFileCommand(commandLine[0]);
    }

    private static PrinterClient.Channel.GCodeLineCommand parseGCodeLineCommand(String commandLine) {
        var split = commandLine.split("\n");
        if (split.length < 2) {
            throw new IllegalArgumentException("There are no lines for GCodeLineCommand!");
        }
        var lines = Arrays.stream(split).skip(1).toList();
        return new PrinterClient.Channel.GCodeLineCommand(lines, split[0]);
    }

    private static PrinterClient.Channel.LedControlCommand parseLedControlCommand(String[] commandLine) {
        if (commandLine.length < 2) {
            throw new IllegalArgumentException(
                    "Command line length does not match! Should be %s, but was %s!".formatted(2, commandLine.length));
        }
        var ledNode = PrinterClient.Channel.LedControlCommand.LedNode.valueOf(commandLine[0]);
        var ledMode = PrinterClient.Channel.LedControlCommand.LedMode.valueOf(commandLine[1]);
        @Nullable
        Integer ledOnTime = null, ledOffTime = null, loopTimes = null, intervalTime = null;
        if (ledMode == FLASHING) {
            requireLength(commandLine, 6);
            ledOnTime = parseInt(commandLine[2]);
            ledOffTime = parseInt(commandLine[3]);
            loopTimes = parseInt(commandLine[4]);
            intervalTime = parseInt(commandLine[5]);
        }
        return new PrinterClient.Channel.LedControlCommand(ledNode, ledMode, ledOnTime, ledOffTime, loopTimes,
                intervalTime);
    }

    private static PrinterClient.Channel.SystemCommand parseSystemCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return PrinterClient.Channel.SystemCommand.valueOf(commandLine[0]);
    }

    private static PrinterClient.Channel.IpCamRecordCommand parseIpCamRecordCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return new PrinterClient.Channel.IpCamRecordCommand(parseBoolean(commandLine[0]));
    }

    private static PrinterClient.Channel.IpCamTimelapsCommand parseIpCamTimelapsCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return new PrinterClient.Channel.IpCamTimelapsCommand(parseBoolean(commandLine[0]));
    }

    private static PrinterClient.Channel.XCamControlCommand parseXCamControlCommand(String[] commandLine) {
        requireLength(commandLine, 3);
        return new PrinterClient.Channel.XCamControlCommand(
                PrinterClient.Channel.XCamControlCommand.Module.valueOf(commandLine[0]), parseBoolean(commandLine[1]),
                parseBoolean(commandLine[2]));
    }

    private static PrinterClient.Channel.RawCommand parseRawCommand(String[] commandLine) {
        requireLength(commandLine, 2);
        var raw = commandLine[1];
        if (!raw.contains(SEQUENCE_ID)) {
            throw new IllegalArgumentException(
                    "Command line does not contain sequence ID. Please add %s into second parameter."
                            .formatted(SEQUENCE_ID));
        }
        return new PrinterClient.Channel.RawStringCommand() {
            @Override
            public String topic() {
                return commandLine[0];
            }

            @Override
            public String buildRawStringCommand(long sequenceId) {
                return raw.replace(SEQUENCE_ID, String.valueOf(sequenceId));
            }
        };
    }
}
