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
import static org.mockito.Mockito.*;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.Channel.*;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand.*;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand.LedNode.*;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.bambulab.internal.BambuLabBindingConstants.Channel;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

import pl.grzeslowski.jbambuapi.PrinterClient;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class PrinterHandlerTest {
    @ParameterizedTest(name = "Should handle {0} command for {1} channel and send {2}")
    @MethodSource
    public void testSendLightCommand(OnOffType command, Channel channel, PrinterClient.Channel.Command sendCommand) {
        // Given
        var printerHandler = spy(new PrinterHandler(mock(Thing.class)));
        var channelUID = new ChannelUID("bambulab:printer:test:" + channel);
        doNothing().when(printerHandler).sendCommand(any(PrinterClient.Channel.Command.class));

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

    @ParameterizedTest(name = "Command to channel {0} should not invoke `client.sendCommand`")
    @MethodSource
    public void notImplementedCommands(Channel channel) {
        // Given
        var printerHandler = spy(new PrinterHandler(mock(Thing.class)));
        var channelUID = new ChannelUID("bambulab:printer:test:" + channel);

        // When
        printerHandler.handleCommand(channelUID, OnOffType.ON);

        // Then
        verify(printerHandler, never()).sendCommand(any());
    }

    static Stream<Arguments> notImplementedCommands() {
        return stream(Channel.values())//
                .filter(not(Channel::isSupportCommand))//
                .map(Arguments::of);
    }
}
