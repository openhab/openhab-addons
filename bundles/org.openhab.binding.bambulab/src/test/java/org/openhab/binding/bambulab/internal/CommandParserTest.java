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

import static org.assertj.core.api.Assertions.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.LedMode.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.LedNode.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PrintSpeedCommand.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;

import pl.grzeslowski.jbambuapi.mqtt.PrinterClient;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class CommandParserTest {

    @Test
    @DisplayName("should correctly parse GCodeLineCommand")
    void gcodeLines() {
        // given
        var command = """
                GCodeLine:123
                G28 ; Home all axes
                G90 ; Set to absolute positioning
                G1 X50 Y50 Z10 F1500 ; Move to position (50,50,10) at 1500 mm/min
                G1 X100 Y100 Z20 F2000 ; Move to position (100,100,20) at 2000 mm/min
                M104 S200 ; Set hotend temperature to 200°C""";

        // when
        var parsedCommand = CommandParser.parseCommand(command);

        // then
        assertThat(parsedCommand).isEqualTo(new PrinterClient.Channel.GCodeLineCommand(
                List.of("G28 ; Home all axes", "G90 ; Set to absolute positioning",
                        "G1 X50 Y50 Z10 F1500 ; Move to position (50,50,10) at 1500 mm/min",
                        "G1 X100 Y100 Z20 F2000 ; Move to position (100,100,20) at 2000 mm/min",
                        "M104 S200 ; Set hotend temperature to 200°C"),
                "123"));
    }

    @Test
    @DisplayName("should throw exception if there are no lines for GCodeLineCommand")
    void gcodeLineNoLines() {
        // given
        var command = "GCodeLine:123";

        // when
        ThrowableAssert.ThrowingCallable when = () -> CommandParser.parseCommand(command);

        // then
        assertThatThrownBy(when)//
                .isInstanceOf(IllegalArgumentException.class)//
                .hasMessage("There are no lines for GCodeLineCommand!");
    }

    @ParameterizedTest(name = "{index}: should {0}")
    @MethodSource
    void shouldRunCommand(String command, PrinterClient.Channel.Command expectedCommand) {
        // when
        var parsedCommand = CommandParser.parseCommand(command);

        // then
        assertThat(parsedCommand).isEqualTo(expectedCommand);
    }

    static Stream<Arguments> shouldRunCommand() {
        var infoCommandStream = Arrays.stream(PrinterClient.Channel.InfoCommand.values())//
                .map(value -> Arguments.of("Info:" + value.name(), value));
        var pushingCommandStream = stream(
                Arguments.of("Pushing:11:22", new PrinterClient.Channel.PushingCommand(11, 22)));
        var printCommandStream = Arrays.stream(PrinterClient.Channel.PrintCommand.values())//
                .map(value -> Arguments.of("Print:" + value.name(), value));
        var changeFilamentCommandStream = stream(
                Arguments.of("ChangeFilament:11:22:33", new PrinterClient.Channel.ChangeFilamentCommand(11, 22, 33)));
        var amsUserSettingCommandStream = stream(Arguments.of("AmsUserSetting:11:tRuE:FaLsE",
                new PrinterClient.Channel.AmsUserSettingCommand(11, true, false)));
        var amsFilamentSettingCommandStream = stream(Arguments.of("AmsFilamentSetting:11:22:s3:s4:55:66:s7",
                new PrinterClient.Channel.AmsFilamentSettingCommand(11, 22, "s3", "s4", 55, 66, "s7")));
        var amsControlCommandStream = Arrays.stream(PrinterClient.Channel.AmsControlCommand.values())//
                .map(value -> Arguments.of("AmsControl:" + value.name(), value));
        var printSpeedCommandStream = Stream.of(SILENT, STANDARD, SPORT, LUDICROUS)//
                .map(value -> Arguments.of("PrintSpeed:" + value.getName(), value));
        var gCodeFileCommandStream = stream(
                Arguments.of("GCodeFile:s1", new PrinterClient.Channel.GCodeFileCommand("s1")));
        var gCodeLineCommandStream = stream(Arguments.of("""
                GCodeLine:s1
                l1
                l2
                l3""", new PrinterClient.Channel.GCodeLineCommand(List.of("l1", "l2", "l3"), "s1")));
        var ledControlCommandStream = stream(//
                Arguments.of("LedControl:CHAMBER_LIGHT:ON",
                        new PrinterClient.Channel.LedControlCommand(CHAMBER_LIGHT, ON, null, null, null, null)), //
                Arguments.of("LedControl:WORK_LIGHT:OFF",
                        new PrinterClient.Channel.LedControlCommand(WORK_LIGHT, OFF, null, null, null, null)), //
                Arguments.of("LedControl:CHAMBER_LIGHT:FLASHING:11:22:33:44",
                        new PrinterClient.Channel.LedControlCommand(CHAMBER_LIGHT, FLASHING, 11, 22, 33, 44))//
        );
        var systemCommandStream = Arrays.stream(PrinterClient.Channel.SystemCommand.values())//
                .map(value -> Arguments.of("System:" + value.name(), value));
        var ipCamRecordCommandStream = stream(//
                Arguments.of("IpCamRecord:tRue", new PrinterClient.Channel.IpCamRecordCommand(true)), //
                Arguments.of("IpCamRecord:fAlSe", new PrinterClient.Channel.IpCamRecordCommand(false))//
        );
        var ipCamTimelapsCommandStream = stream(//
                Arguments.of("IpCamTimelaps:tRue", new PrinterClient.Channel.IpCamTimelapsCommand(true)), //
                Arguments.of("IpCamTimelaps:fAlSe", new PrinterClient.Channel.IpCamTimelapsCommand(false))//
        );
        var xCamControlCommandStream = Arrays.stream(PrinterClient.Channel.XCamControlCommand.Module.values())//
                .map(moduleValue -> Arguments.of("XCamControl:%s:trUE:FAlse".formatted(moduleValue),
                        new PrinterClient.Channel.XCamControlCommand(moduleValue, true, false)));

        return concat(infoCommandStream, pushingCommandStream, printCommandStream, changeFilamentCommandStream,
                amsUserSettingCommandStream, amsFilamentSettingCommandStream, amsControlCommandStream,
                printSpeedCommandStream, gCodeFileCommandStream, gCodeLineCommandStream, ledControlCommandStream,
                systemCommandStream, ipCamRecordCommandStream, ipCamTimelapsCommandStream, xCamControlCommandStream);
    }

    @SafeVarargs
    static Stream<Arguments> concat(Stream<Arguments>... streams) {
        return Stream.of(streams).flatMap(s -> s);
    }

    @SafeVarargs
    static <T> Stream<T> stream(T... values) {
        return Stream.of(values);
    }

    @ParameterizedTest(name = "{index}: should support command {0}")
    @MethodSource
    void shouldSupportCommand(String command) {
        // when
        ThrowableAssert.ThrowingCallable when = () -> CommandParser.parseCommand(command);

        // then
        assertThatCode(when)//
                .as("Command should be supported! But it was not! Command: %s", command)
                // if an error message starts with "Unknown..." it means that the giant if statement did not cover
                // command.
                //
                // it might happen if JBambuApi library adds a new subclass of Command and addon developers would not
                // add it to PrinterActions.
                //
                // it might happen that there will be some other IllegalArgumentException (like not enough params) would
                // be thrown, but none of them starts with "Unknown...".
                .hasMessageNotContaining("Unknown command");
    }

    static Stream<Arguments> shouldSupportCommand() {
        return new Reflections(PrinterClient.Channel.Command.class)//
                .getSubTypesOf(PrinterClient.Channel.Command.class)//
                .stream()//
                .map(Class::getSimpleName)//
                .sorted()//
                .map(command -> command.replace("Command", ""))//
                .map(command -> command + ":x:y:z:w")//
                .map(Arguments::of);
    }
}
