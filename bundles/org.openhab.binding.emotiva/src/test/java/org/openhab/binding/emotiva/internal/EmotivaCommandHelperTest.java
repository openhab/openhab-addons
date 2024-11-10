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
package org.openhab.binding.emotiva.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_VOLUME;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MUTE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_STANDBY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SURROUND;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.volumeDecibelToPercentage;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.volumePercentageToDecibel;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.mute;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.mute_off;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.mute_on;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.standby;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.surround;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.surround_trim_set;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.volume;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.DIMENSIONLESS_DECIBEL;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.ON_OFF;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.PROTOCOL_V2;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.PROTOCOL_V3;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlRequest;
import org.openhab.binding.emotiva.internal.protocol.EmotivaDataType;
import org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion;
import org.openhab.core.library.types.PercentType;

/**
 * Unit tests for the EmotivaCommandHelper.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaCommandHelperTest {

    @Test
    void volumeToPercentage() {
        assertThat(volumeDecibelToPercentage("-100 dB"), is(PercentType.valueOf("0")));
        assertThat(volumeDecibelToPercentage(" -96"), is(PercentType.valueOf("0")));
        assertThat(volumeDecibelToPercentage("-41 dB "), is(PercentType.valueOf("50")));
        assertThat(volumeDecibelToPercentage("15"), is(PercentType.valueOf("100")));
        assertThat(volumeDecibelToPercentage("20"), is(PercentType.valueOf("100")));
    }

    @Test
    void volumeToDecibel() {
        assertThat(volumePercentageToDecibel("-10"), is(-96));
        assertThat(volumePercentageToDecibel("0%"), is(-96));
        assertThat(volumePercentageToDecibel("50 %"), is(-41));
        assertThat(volumePercentageToDecibel("100 % "), is(15));
        assertThat(volumePercentageToDecibel("110"), is(15));
    }

    private static Stream<Arguments> channelToControlRequest() {
        return Stream.of(
                Arguments.of(CHANNEL_SURROUND, "surround", DIMENSIONLESS_DECIBEL, surround, surround, surround,
                        surround_trim_set, PROTOCOL_V2, -24.0, 24.0),
                Arguments.of(CHANNEL_SURROUND, "surround", DIMENSIONLESS_DECIBEL, surround, surround, surround,
                        surround_trim_set, PROTOCOL_V3, -24.0, 24.0),
                Arguments.of(CHANNEL_MUTE, "mute", ON_OFF, mute, mute_on, mute_off, mute, PROTOCOL_V2, 0, 0),
                Arguments.of(CHANNEL_STANDBY, "standby", ON_OFF, standby, standby, standby, standby, PROTOCOL_V2, 0, 0),
                Arguments.of(CHANNEL_MAIN_VOLUME, "volume", DIMENSIONLESS_DECIBEL, volume, volume, volume, volume,
                        PROTOCOL_V2, -96, 15));
    }

    @ParameterizedTest
    @MethodSource("channelToControlRequest")
    void testChannelToControlRequest(String channel, String name, EmotivaDataType emotivaDataType,
            EmotivaControlCommands defaultCommand, EmotivaControlCommands onCommand, EmotivaControlCommands offCommand,
            EmotivaControlCommands setCommand, EmotivaProtocolVersion version, double min, double max) {
        var state = new EmotivaProcessorState();
        EmotivaControlRequest surround = EmotivaCommandHelper.channelToControlRequest(channel, state, version);

        assertThat(surround.getName(), is(name));
        assertThat(surround.getChannel(), is(channel));
        assertThat(surround.getDataType(), is(emotivaDataType));
        assertThat(surround.getDefaultCommand(), is(defaultCommand));
        assertThat(surround.getOnCommand(), is(onCommand));
        assertThat(surround.getOffCommand(), is(offCommand));
        assertThat(surround.getSetCommand(), is(setCommand));
        assertThat(surround.getMinValue(), is(min));
        assertThat(surround.getMaxValue(), is(max));
    }
}
