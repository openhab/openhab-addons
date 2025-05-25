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
package org.openhab.binding.emotiva.internal.protocol;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.analog1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.hdmi1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.source_2;

import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link EmotivaControlCommands}.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaControlCommandsTest extends AbstractEmotivaControlTest {

    private static Stream<Arguments> mapInputToSourceCommand() {
        return Stream.of(Arguments.of("", "none", Map.ofEntries(Map.entry(hdmi1, hdmi1.getLabel()))),
                Arguments.of("  ", "none", Map.ofEntries(Map.entry(hdmi1, hdmi1.getLabel()))),
                Arguments.of("  ", "none", Map.ofEntries(Map.entry(hdmi1, hdmi1.getLabel()))),
                Arguments.of("HDMI1", "hdmi1", Map.ofEntries(Map.entry(hdmi1, hdmi1.getLabel()))),
                Arguments.of("HDMI1\n", "hdmi1", Map.ofEntries(Map.entry(hdmi1, hdmi1.getLabel()))),
                Arguments.of("HDMI1\r", "hdmi1", Map.ofEntries(Map.entry(hdmi1, hdmi1.getLabel()))),
                Arguments.of("\n\rHDMI1\r\n", "hdmi1", Map.ofEntries(Map.entry(hdmi1, hdmi1.getLabel()))),
                Arguments.of("Hdmi1", "hdmi1", Map.ofEntries(Map.entry(hdmi1, hdmi1.getLabel()))),
                Arguments.of(" Hdmi1 ", "hdmi1", Map.ofEntries(Map.entry(hdmi1, hdmi1.getLabel()))),
                Arguments.of("source_2", "source_2", Map.ofEntries(Map.entry(source_2, "SHIELD"))),
                Arguments.of("SHIELD", "source_2", Map.ofEntries(Map.entry(source_2, "SHIELD"))),
                Arguments.of("Analog 1", "analog1", Map.ofEntries(Map.entry(analog1, "Analog 1"))),
                Arguments.of("Analog1", "analog1", Map.ofEntries(Map.entry(analog1, "Analog1"))));
    }

    @ParameterizedTest
    @MethodSource("mapInputToSourceCommand")
    void mapInputToSourceCommand2(String input, String expected,
            Map<EmotivaControlCommands, String> mainZoneSourceOverrides) {
        mainZoneSourceOverrides.forEach(state::updateSourcesMainZone);
        EmotivaControlCommands sourceCommand = EmotivaControlCommands.matchFromSourceInput(input,
                mainZoneSourceOverrides);
        assertThat(sourceCommand.name(), is(expected));
    }
}
