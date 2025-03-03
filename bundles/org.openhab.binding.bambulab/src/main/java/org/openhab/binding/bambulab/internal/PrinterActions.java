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
import static java.util.Objects.requireNonNull;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.BINDING_ID;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand.LedMode.FLASHING;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

import pl.grzeslowski.jbambuapi.PrinterClient.Channel.AmsControlCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.AmsFilamentSettingCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.AmsUserSettingCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.ChangeFilamentCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.Command;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.GCodeFileCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.GCodeLineCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.InfoCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.IpCamRecordCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.IpCamTimelapsCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand.LedMode;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand.LedNode;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.PrintCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.PrintSpeedCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.PushingCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.SystemCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.XCamControlCommand;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ThingActionsScope(name = BINDING_ID)
@NonNullByDefault
public class PrinterActions implements ThingActions {
    private @Nullable PrinterHandler handler;

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        this.handler = (PrinterHandler) thingHandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.sendCommand.label", description = "@text/action.sendCommand.description")
    public void sendCommand(
            @ActionInput(name = "time", label = "@text/action.sendCommand.commandLabel", description = "@text/action.sendCommand.commandDescription") String stringCommand) {
        var localHandler = handler;
        if (localHandler == null) {
            return;
        }

        var command = parseCommand(stringCommand);
        localHandler.sendCommand(command);
    }

    private Command parseCommand(String stringCommand) {
        var split = stringCommand.split(":");
        if (split.length <= 1) {
            throw new IllegalArgumentException("Command too short, class name not passed. Command: " + stringCommand);
        }
        var commandName = split[0] + "Command";
        var tail = tail(split);
        if (commandName.equals(InfoCommand.class.getSimpleName())) {
            return parseInfoCommand(tail);
        }
        if (commandName.equals(PushingCommand.class.getSimpleName())) {
            return parsePushingCommand(tail);
        }
        if (commandName.equals(PrintCommand.class.getSimpleName())) {
            return parsePrintCommand(tail);
        }
        if (commandName.equals(ChangeFilamentCommand.class.getSimpleName())) {
            return parseChangeFilamentCommand(tail);
        }
        if (commandName.equals(AmsUserSettingCommand.class.getSimpleName())) {
            return parseAmsUserSettingCommand(tail);
        }
        if (commandName.equals(AmsFilamentSettingCommand.class.getSimpleName())) {
            return parseAmsFilamentSettingCommand(tail);
        }
        if (commandName.equals(AmsControlCommand.class.getSimpleName())) {
            return parseAmsControlCommand(tail);
        }
        if (commandName.equals(PrintSpeedCommand.class.getSimpleName())) {
            return parsePrintSpeedCommand(tail);
        }
        if (commandName.equals(GCodeFileCommand.class.getSimpleName())) {
            return parseGCodeFileCommand(tail);
        }
        if (commandName.equals(GCodeLineCommand.class.getSimpleName())) {
            var gcodeLineSplit = stringCommand.split(":", 2);
            requireLength(gcodeLineSplit, 2);
            return parseGCodeLineCommand(gcodeLineSplit[1]);
        }
        if (commandName.equals(LedControlCommand.class.getSimpleName())) {
            return parseLedControlCommand(tail);
        }
        if (commandName.equals(SystemCommand.class.getSimpleName())) {
            return parseSystemCommand(tail);
        }
        if (commandName.equals(IpCamRecordCommand.class.getSimpleName())) {
            return parseIpCamRecordCommand(tail);
        }
        if (commandName.equals(IpCamTimelapsCommand.class.getSimpleName())) {
            return parseIpCamTimelapsCommand(tail);
        }
        if (commandName.equals(XCamControlCommand.class.getSimpleName())) {
            return parseXCamControlCommand(tail);
        }

        throw new IllegalArgumentException("Unknown command name: " + commandName);
    }

    private String[] tail(String[] command) {
        return copyOfRange(command, 1, command.length);
    }

    private void requireLength(String[] commandLine, int length) {
        if (commandLine.length != length) {
            throw new IllegalArgumentException("Command line length does not match! Should be %s, but was %s!"
                    .formatted(length, commandLine.length));
        }
    }

    private InfoCommand parseInfoCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return InfoCommand.valueOf(commandLine[0]);
    }

    private PushingCommand parsePushingCommand(String[] commandLine) {
        if (commandLine.length == 0) {
            return PushingCommand.defaultPushingCommand();
        }
        requireLength(commandLine, 2);
        return new PushingCommand(parseInt(commandLine[0]), parseInt(commandLine[1]));
    }

    private PrintCommand parsePrintCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return PrintCommand.valueOf(commandLine[0]);
    }

    private ChangeFilamentCommand parseChangeFilamentCommand(String[] commandLine) {
        requireLength(commandLine, 3);
        return new ChangeFilamentCommand(parseInt(commandLine[0]), parseInt(commandLine[1]), parseInt(commandLine[2]));
    }

    private AmsUserSettingCommand parseAmsUserSettingCommand(String[] commandLine) {
        requireLength(commandLine, 3);
        return new AmsUserSettingCommand(parseInt(commandLine[0]), parseBoolean(commandLine[1]),
                parseBoolean(commandLine[2]));
    }

    private AmsFilamentSettingCommand parseAmsFilamentSettingCommand(String[] commandLine) {
        requireLength(commandLine, 7);
        return new AmsFilamentSettingCommand(parseInt(commandLine[0]), parseInt(commandLine[1]), commandLine[2],
                commandLine[3], parseInt(commandLine[4]), parseInt(commandLine[5]), commandLine[6]);
    }

    private AmsControlCommand parseAmsControlCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return AmsControlCommand.valueOf(commandLine[0]);
    }

    private PrintSpeedCommand parsePrintSpeedCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return PrintSpeedCommand.valueOf(commandLine[0]);
    }

    private GCodeFileCommand parseGCodeFileCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return new GCodeFileCommand(commandLine[0]);
    }

    private GCodeLineCommand parseGCodeLineCommand(String commandLine) {
        var split = commandLine.split("\n");
        if (split.length < 2) {
            throw new IllegalArgumentException("There are no lines for GCodeLineCommand!");
        }
        var lines = Arrays.stream(split).skip(1).toList();
        return new GCodeLineCommand(lines, split[0]);
    }

    private LedControlCommand parseLedControlCommand(String[] commandLine) {
        if (commandLine.length < 2) {
            throw new IllegalArgumentException(
                    "Command line length does not match! Should be %s, but was %s!".formatted(2, commandLine.length));
        }
        var ledNode = LedNode.valueOf(commandLine[0]);
        var ledMode = LedMode.valueOf(commandLine[1]);
        @Nullable
        Integer ledOnTime = null, ledOffTime = null, loopTimes = null, intervalTime = null;
        if (ledMode == FLASHING) {
            requireLength(commandLine, 6);
            ledOnTime = parseInt(commandLine[2]);
            ledOffTime = parseInt(commandLine[3]);
            loopTimes = parseInt(commandLine[4]);
            intervalTime = parseInt(commandLine[5]);
        }
        return new LedControlCommand(ledNode, ledMode, ledOnTime, ledOffTime, loopTimes, intervalTime);
    }

    private SystemCommand parseSystemCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return SystemCommand.valueOf(commandLine[0]);
    }

    private IpCamRecordCommand parseIpCamRecordCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return new IpCamRecordCommand(parseBoolean(commandLine[0]));
    }

    private IpCamTimelapsCommand parseIpCamTimelapsCommand(String[] commandLine) {
        requireLength(commandLine, 1);
        return new IpCamTimelapsCommand(parseBoolean(commandLine[0]));
    }

    private XCamControlCommand parseXCamControlCommand(String[] commandLine) {
        requireLength(commandLine, 3);
        return new XCamControlCommand(XCamControlCommand.Module.valueOf(commandLine[0]), parseBoolean(commandLine[1]),
                parseBoolean(commandLine[2]));
    }

    public static void sendCommand(@Nullable ThingActions actions, String stringCommand) {
        ((PrinterActions) requireNonNull(actions)).sendCommand(stringCommand);
    }

    @RuleAction(label = "@text/action.refreshChannels.label", description = "@text/action.refreshChannels.description")
    public void refreshChannels() {
        var localHandler = handler;
        if (localHandler == null) {
            return;
        }

        localHandler.refreshChannels();
    }

    public static void refreshChannels(@Nullable ThingActions actions) {
        ((PrinterActions) requireNonNull(actions)).refreshChannels();
    }
}
