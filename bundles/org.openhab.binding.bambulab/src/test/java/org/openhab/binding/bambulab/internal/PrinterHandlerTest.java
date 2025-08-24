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

import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.PrinterChannel.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.LedNode.CHAMBER_LIGHT;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.LedNode.WORK_LIGHT;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.off;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.on;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PrintSpeedCommand.*;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.bambulab.internal.BambuLabBindingConstants.PrinterChannel;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;

import pl.grzeslowski.jbambuapi.mqtt.PrinterClient;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.Command;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PrintSpeedCommand;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class PrinterHandlerTest {
    @Spy
    PrinterHandler printerHandler = new PrinterHandler(mock(Bridge.class), mock(HttpClient.class));

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(printerHandler).sendCommand(any(Command.class));
    }

    @ParameterizedTest(name = "Should handle {0} command for {1} channel and send {2}")
    @MethodSource
    public void testSendLightCommand(OnOffType command, PrinterChannel channel, Command sendCommand) {
        // Given
        var channelUID = new ChannelUID("bambulab:printer:test:" + channel);

        // When
        printerHandler.handleCommand(channelUID, command);

        // Then
        verify(printerHandler).sendCommand(eq(sendCommand));
    }

    static Stream<Arguments> testSendLightCommand() {
        return Stream.of(//
                Arguments.of(OnOffType.ON, CHANNEL_LED_CHAMBER_LIGHT, on(CHAMBER_LIGHT)), //
                Arguments.of(OnOffType.OFF, CHANNEL_LED_CHAMBER_LIGHT, off(CHAMBER_LIGHT)), //
                Arguments.of(OnOffType.ON, CHANNEL_LED_WORK_LIGHT, on(WORK_LIGHT)), //
                Arguments.of(OnOffType.OFF, CHANNEL_LED_WORK_LIGHT, off(WORK_LIGHT)));
    }

    @Test
    @DisplayName("should send gcode command to a printer when command to CHANNEL_GCODE_FILE is sent")
    void testGCode() {
        // given
        var channelUID = new ChannelUID("bambulab:printer:test:" + CHANNEL_GCODE_FILE.getName());

        // when
        printerHandler.handleCommand(channelUID, new StringType("gcode-file"));

        // then
        verify(printerHandler).sendCommand(eq(new PrinterClient.Channel.GCodeFileCommand("gcode-file")));
    }

    @ParameterizedTest(name = "{index}: should accept StringType(\"{0}\") and send {1} command to a printer")
    @MethodSource
    void speedLevel(String speedLevel, PrintSpeedCommand command) {
        // given
        var channelUID = new ChannelUID("bambulab:printer:test:" + CHANNEL_SPEED_LEVEL.getName());

        // when
        printerHandler.handleCommand(channelUID, new StringType(speedLevel));

        // then
        verify(printerHandler).sendCommand(eq(command));
    }

    static Stream<Arguments> speedLevel() {
        return Stream.of(SILENT, STANDARD, SPORT, LUDICROUS)//
                .map(command -> Arguments.of(command.getName(), command));
    }

    @ParameterizedTest(name = "Command to channel {0} should not invoke `client.sendCommand`")
    @MethodSource
    public void notImplementedCommands(PrinterChannel channel) {
        // Given
        var channelUID = new ChannelUID("bambulab:printer:test:" + channel);

        // When
        printerHandler.handleCommand(channelUID, OnOffType.ON);

        // Then
        verify(printerHandler, never()).sendCommand(any(Command.class));
        verify(printerHandler, never()).sendCommand(anyString());
    }

    static Stream<Arguments> notImplementedCommands() {
        return stream(PrinterChannel.values())//
                .filter(not(PrinterChannel::isSupportCommand))//
                .map(Arguments::of);
    }
}
