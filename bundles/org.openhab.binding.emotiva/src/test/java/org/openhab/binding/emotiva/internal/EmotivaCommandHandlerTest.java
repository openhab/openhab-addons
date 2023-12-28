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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_VOLUME;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MUTE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_STANDBY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SURROUND;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.PercentType;

/**
 * Unit tests for the EmotivaCommandHandler.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaCommandHandlerTest {

    @Test
    void volumeToPercentage() {
        assertThat(EmotivaCommandHandler.volumeDecibelToPercentage("-100 dB")).isEqualTo(PercentType.valueOf("0"));
        assertThat(EmotivaCommandHandler.volumeDecibelToPercentage(" -96")).isEqualTo(PercentType.valueOf("0"));
        assertThat(EmotivaCommandHandler.volumeDecibelToPercentage("-41 dB ")).isEqualTo(PercentType.valueOf("50"));
        assertThat(EmotivaCommandHandler.volumeDecibelToPercentage("15")).isEqualTo(PercentType.valueOf("100"));
        assertThat(EmotivaCommandHandler.volumeDecibelToPercentage("20")).isEqualTo(PercentType.valueOf("100"));
    }

    @Test
    void volumeToDecibel() {
        assertThat(EmotivaCommandHandler.volumePercentageToDecibel("-10")).isEqualTo(-96);
        assertThat(EmotivaCommandHandler.volumePercentageToDecibel("0%")).isEqualTo(-96);
        assertThat(EmotivaCommandHandler.volumePercentageToDecibel("50 %")).isEqualTo(-41);
        assertThat(EmotivaCommandHandler.volumePercentageToDecibel("100 % ")).isEqualTo(15);
        assertThat(EmotivaCommandHandler.volumePercentageToDecibel("110")).isEqualTo(15);
    }

    @Test
    public void testEmotivaVolumeValue() throws UnsupportedCommandTypeException {

        EmotivaConfiguration configuration = new EmotivaConfiguration();
        EmotivaCommandHandler commandHandler = new EmotivaCommandHandler(configuration);

        assertThat(commandHandler.emotivaVolumeValue(IncreaseDecreaseType.INCREASE)).isEqualTo(1);
        assertThat(commandHandler.emotivaVolumeValue(IncreaseDecreaseType.DECREASE)).isEqualTo(-1);
        assertThat(commandHandler.emotivaVolumeValue(new PercentType(0))).isEqualTo(-96);
        assertThat(commandHandler.emotivaVolumeValue(new PercentType(50))).isEqualTo(-41);
        assertThat(commandHandler.emotivaVolumeValue(new PercentType(100))).isEqualTo(15);
        assertThat(commandHandler.emotivaVolumeValue(new DecimalType(1))).isEqualTo(1);
        assertThat(commandHandler.emotivaVolumeValue(new DecimalType(100))).isEqualTo(15);
        assertThat(commandHandler.emotivaVolumeValue(new DecimalType(-100))).isEqualTo(-96);
        assertThat(commandHandler.emotivaVolumeValue(new DecimalType(-96))).isEqualTo(-96);
        assertThat(commandHandler.emotivaVolumeValue(new DecimalType(-50))).isEqualTo(-50);
        assertThatThrownBy(() -> commandHandler.emotivaVolumeValue(new DateTimeType("2020-04-01T20:04:30.395Z")))
                .isInstanceOf(UnsupportedCommandTypeException.class);
    }

    private static Stream<Arguments> channelToControlRequest() {
        return Stream.of(
                Arguments.of(CHANNEL_SURROUND, "surround", DIMENSIONLESS_DECIBEL, surround, surround, surround,
                        surround_trim_set, PROTOCOL_V2, -12, 12),
                Arguments.of(CHANNEL_SURROUND, "surround", DIMENSIONLESS_DECIBEL, surround, surround, surround,
                        surround_trim_set, PROTOCOL_V3, -24, 24),
                Arguments.of(CHANNEL_MUTE, "mute", ON_OFF, mute, mute_on, mute_off, mute, PROTOCOL_V2, 0, 0),
                Arguments.of(CHANNEL_STANDBY, "standby", ON_OFF, standby, standby, standby, standby, PROTOCOL_V2, 0, 0),
                Arguments.of(CHANNEL_MAIN_VOLUME, "volume", DIMENSIONLESS_DECIBEL, volume, volume, volume, volume,
                        PROTOCOL_V2, -96, 15));
    }

    @ParameterizedTest
    @MethodSource("channelToControlRequest")
    void testChannelToControlRequest(String channel, String name, EmotivaDataType emotivaDataType,
            EmotivaControlCommands defaultCommand, EmotivaControlCommands onCommand, EmotivaControlCommands offCommand,
            EmotivaControlCommands setCommand, EmotivaProtocolVersion version, int min, int max) {

        final Map<String, Map<EmotivaControlCommands, String>> commandMaps = new ConcurrentHashMap<>();

        EmotivaControlRequest surround = EmotivaCommandHandler.channelToControlRequest(channel, commandMaps, version);
        assertThat(surround.getName()).isEqualTo(name);
        assertThat(surround.getChannel()).isEqualTo(channel);
        assertThat(surround.getDataType()).isEqualTo(emotivaDataType);
        assertThat(surround.getDefaultCommand()).isEqualTo(defaultCommand);
        assertThat(surround.getOnCommand()).isEqualTo(onCommand);
        assertThat(surround.getOffCommand()).isEqualTo(offCommand);
        assertThat(surround.getSetCommand()).isEqualTo(setCommand);
        assertThat(surround.getMinValue()).isEqualTo(min);
        assertThat(surround.getMaxValue()).isEqualTo(max);
    }
}
