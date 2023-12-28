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
package org.openhab.binding.emotiva.internal.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_AUDIO_BITS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_AUDIO_BITSTREAM;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_AUDIO_INPUT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_BACK;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_BASS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_CENTER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_CHANNEL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_DIM;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_FREQUENCY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_HEIGHT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT1;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT2;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT3;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT4;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT5;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT6;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT7;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT8;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_LOUDNESS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_VOLUME;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_VOLUME_DB;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_ZONE_POWER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_CONTROL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_DOWN;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_ENTER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_LEFT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_RIGHT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_UP;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_ALL_STEREO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_AUTO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_DIRECT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_DOLBY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_DTS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_MOVIE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_MUSIC;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_REF_STEREO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_STEREO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_SURROUND;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MUTE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SEEK;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SELECTED_MODE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SELECTED_MOVIE_MUSIC;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SOURCE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SPEAKER_PRESET;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_STANDBY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SUBWOOFER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SURROUND;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TREBLE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_BAND;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_CHANNEL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_CHANNEL_SELECT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_PROGRAM;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_RDS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_SIGNAL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_VIDEO_FORMAT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_VIDEO_INPUT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_VIDEO_SPACE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_WIDTH;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_MUTE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_POWER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_SOURCE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_VOLUME;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_VOLUME_DB;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.DEFAULT_CONTROL_ACK_VALUE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_SOURCES_MAIN_ZONE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_SOURCES_ZONE_2;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.all_stereo;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.auto;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.back_trim_set;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_am;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_fm;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.bass_down;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.center_trim_set;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.channel;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.channel_1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.channel_2;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.channel_3;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.coax1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.dim;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.direct;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.dolby;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.down;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.dts;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.enter;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.frequency;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.hdmi1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.height_trim_set;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.left;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.loudness_off;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.loudness_on;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.menu;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.mode_down;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.mode_up;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.movie;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.music;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.mute_off;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.mute_on;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.none;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.power_off;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.power_on;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.preset2;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.reference_stereo;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.right;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.seek;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.set_volume;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.source_1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.source_2;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.speaker_preset;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.standby;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.stereo;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.subwoofer_trim_set;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.surround_mode;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.surround_trim_set;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.treble_down;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.treble_up;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.up;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.width_trim_set;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.zone2_ARC;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.zone2_coax1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.zone2_follow_main;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.zone2_mute_off;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.zone2_mute_on;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.zone2_power_off;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.zone2_power_on;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.zone2_set_volume;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.PROTOCOL_V2;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.PROTOCOL_V3;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.tuner_band;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.tuner_channel;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.emotiva.internal.EmotivaCommandHelper;
import org.openhab.binding.emotiva.internal.dto.EmotivaControlDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Unit tests for EmotivaControl requests.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaControlRequestTest {

    private static Stream<Arguments> channelToDTOs() {
        return Stream.of(Arguments.of(CHANNEL_STANDBY, OnOffType.ON, standby, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_STANDBY, OnOffType.OFF, standby, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MAIN_ZONE_POWER, OnOffType.ON, power_on, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MAIN_ZONE_POWER, OnOffType.OFF, power_off, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SOURCE, new StringType("HDMI1"), hdmi1, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SOURCE, new StringType("SHIELD"), source_2, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SOURCE, new StringType("hdmi1"), hdmi1, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SOURCE, new StringType("coax1"), coax1, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SOURCE, new StringType("NOT_REAL"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU, new StringType("0"), menu, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_CONTROL, new StringType("0"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_CONTROL, new StringType("MENU"), menu, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_CONTROL, new StringType("ENTER"), enter, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_CONTROL, new StringType("UP"), up, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_CONTROL, new StringType("DOWN"), down, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_CONTROL, new StringType("LEFT"), left, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_CONTROL, new StringType("RIGHT"), right, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_UP, new StringType("0"), up, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_DOWN, new StringType("0"), down, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_LEFT, new StringType("0"), left, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_RIGHT, new StringType("0"), right, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MENU_ENTER, new StringType("0"), enter, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MUTE, OnOffType.ON, mute_on, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MUTE, OnOffType.OFF, mute_off, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_DIM, OnOffType.ON, dim, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_DIM, OnOffType.OFF, dim, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE, new StringType("mode_ref_stereo"), reference_stereo, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE, new StringType("surround_mode"), surround_mode, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE, new StringType("mode_surround"), surround_mode, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE, new StringType("surround"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE, new StringType("1"), mode_up, PROTOCOL_V2, "1"),
                Arguments.of(CHANNEL_MODE, new DecimalType(-1), mode_down, PROTOCOL_V2, "-1"),
                Arguments.of(CHANNEL_MODE, OnOffType.ON, none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE, new DecimalType(1), mode_up, PROTOCOL_V2, "1"),
                Arguments.of(CHANNEL_MODE, new DecimalType(-10), mode_down, PROTOCOL_V2, "-1"),
                Arguments.of(CHANNEL_CENTER, new QuantityType<>(10, Units.DECIBEL), center_trim_set, PROTOCOL_V2,
                        "20.0"),
                Arguments.of(CHANNEL_CENTER, new QuantityType<>(10, Units.DECIBEL), center_trim_set, PROTOCOL_V3,
                        "20.0"),
                Arguments.of(CHANNEL_CENTER, new DecimalType(-30), center_trim_set, PROTOCOL_V2, "-24.0"),
                Arguments.of(CHANNEL_CENTER, new DecimalType(-30), center_trim_set, PROTOCOL_V3, "-24.0"),
                Arguments.of(CHANNEL_SUBWOOFER, new DecimalType(1), subwoofer_trim_set, PROTOCOL_V2, "2.0"),
                Arguments.of(CHANNEL_SUBWOOFER, new DecimalType(1), subwoofer_trim_set, PROTOCOL_V3, "2.0"),
                Arguments.of(CHANNEL_SUBWOOFER, new DecimalType(-25), subwoofer_trim_set, PROTOCOL_V2, "-24.0"),
                Arguments.of(CHANNEL_SUBWOOFER, new DecimalType(-25), subwoofer_trim_set, PROTOCOL_V3, "-24.0"),
                Arguments.of(CHANNEL_SURROUND, new DecimalType(30), surround_trim_set, PROTOCOL_V2, "24.0"),
                Arguments.of(CHANNEL_SURROUND, new DecimalType(30), surround_trim_set, PROTOCOL_V3, "24.0"),
                Arguments.of(CHANNEL_SURROUND, new DecimalType(-3.5), surround_trim_set, PROTOCOL_V2, "-7.0"),
                Arguments.of(CHANNEL_SURROUND, new DecimalType(-3), surround_trim_set, PROTOCOL_V3, "-6.0"),
                Arguments.of(CHANNEL_BACK, new DecimalType(-3), back_trim_set, PROTOCOL_V2, "-6.0"),
                Arguments.of(CHANNEL_BACK, new DecimalType(-3), back_trim_set, PROTOCOL_V3, "-6.0"),
                Arguments.of(CHANNEL_BACK, new DecimalType(30), back_trim_set, PROTOCOL_V2, "24.0"),
                Arguments.of(CHANNEL_BACK, new DecimalType(30), back_trim_set, PROTOCOL_V3, "24.0"),
                Arguments.of(CHANNEL_MODE_SURROUND, new StringType("0"), surround_mode, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SPEAKER_PRESET, OnOffType.ON, speaker_preset, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SPEAKER_PRESET, OnOffType.OFF, speaker_preset, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SPEAKER_PRESET, new StringType("preset2"), preset2, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SPEAKER_PRESET, new StringType("1"), speaker_preset, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SPEAKER_PRESET, new StringType("speaker_preset"), speaker_preset, PROTOCOL_V2,
                        "0"),
                Arguments.of(CHANNEL_MAIN_VOLUME, new DecimalType(30), set_volume, PROTOCOL_V2, "15.0"),
                Arguments.of(CHANNEL_MAIN_VOLUME, new PercentType("50"), set_volume, PROTOCOL_V2, "-41"),
                Arguments.of(CHANNEL_MAIN_VOLUME_DB, new QuantityType<>(-96, Units.DECIBEL), set_volume, PROTOCOL_V2,
                        "-96.0"),
                Arguments.of(CHANNEL_MAIN_VOLUME_DB, new QuantityType<>(-100, Units.DECIBEL), set_volume, PROTOCOL_V2,
                        "-96.0"),
                Arguments.of(CHANNEL_LOUDNESS, OnOffType.ON, loudness_on, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_LOUDNESS, OnOffType.OFF, loudness_off, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_POWER, OnOffType.ON, zone2_power_on, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_POWER, OnOffType.OFF, zone2_power_off, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_VOLUME, new DecimalType(30), zone2_set_volume, PROTOCOL_V2, "15.0"),
                Arguments.of(CHANNEL_ZONE2_VOLUME, new PercentType("50"), zone2_set_volume, PROTOCOL_V2, "-41"),
                Arguments.of(CHANNEL_ZONE2_VOLUME_DB, new QuantityType<>(-96, Units.DECIBEL), zone2_set_volume,
                        PROTOCOL_V2, "-96.0"),
                Arguments.of(CHANNEL_ZONE2_VOLUME_DB, new QuantityType<>(-100, Units.DECIBEL), zone2_set_volume,
                        PROTOCOL_V2, "-96.0"),
                Arguments.of(CHANNEL_ZONE2_MUTE, OnOffType.ON, zone2_mute_on, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_MUTE, OnOffType.OFF, zone2_mute_off, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_SOURCE, new StringType("HDMI1"), hdmi1, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_SOURCE, new StringType("SHIELD"), source_2, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_SOURCE, new StringType("hdmi1"), hdmi1, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_SOURCE, new StringType("coax1"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_SOURCE, new StringType("zone2_coax1"), zone2_coax1, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_SOURCE, new StringType("zone2_ARC"), zone2_ARC, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_SOURCE, new StringType("NOT_REAL"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_ZONE2_SOURCE, new StringType("zone2_follow_main"), zone2_follow_main, PROTOCOL_V2,
                        "0"),
                Arguments.of(CHANNEL_FREQUENCY, UpDownType.UP, frequency, PROTOCOL_V2, "1"),
                Arguments.of(CHANNEL_FREQUENCY, UpDownType.DOWN, frequency, PROTOCOL_V2, "-1"),
                Arguments.of(CHANNEL_SEEK, UpDownType.UP, seek, PROTOCOL_V2, "1"),
                Arguments.of(CHANNEL_SEEK, UpDownType.DOWN, seek, PROTOCOL_V2, "-1"),
                Arguments.of(CHANNEL_CHANNEL, UpDownType.UP, channel, PROTOCOL_V2, "1"),
                Arguments.of(CHANNEL_CHANNEL, UpDownType.DOWN, channel, PROTOCOL_V2, "-1"),
                Arguments.of(CHANNEL_TUNER_BAND, new StringType("band_am"), band_am, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_BAND, new StringType("band_fm"), band_fm, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_CHANNEL, new StringType("FM 107.90MHz"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_CHANNEL, QuantityType.valueOf(103000000, Units.HERTZ), none, PROTOCOL_V2,
                        "0"),
                Arguments.of(CHANNEL_TUNER_CHANNEL, new StringType("channel_1"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_CHANNEL_SELECT, new StringType("channel_1"), channel_1, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_CHANNEL_SELECT, new StringType("CHANNEL_2"), channel_2, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_CHANNEL_SELECT, new StringType("FM 107.90MHz"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_CHANNEL_SELECT, QuantityType.valueOf(103000000, Units.HERTZ), none,
                        PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_SIGNAL, new StringType("Mono   0dBuV"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_PROGRAM, new StringType("Black Metal"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TUNER_RDS, new StringType("The Zombie Apocalypse is upon us!"), none, PROTOCOL_V2,
                        "0"),
                Arguments.of(CHANNEL_AUDIO_INPUT, new StringType("HDMI 1"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_AUDIO_BITSTREAM, new StringType("HDMI 1"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_AUDIO_BITS, new StringType("PCM 5.1"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_VIDEO_INPUT, new StringType("HDMI 1"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_VIDEO_FORMAT, new StringType("1080P/60"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_VIDEO_SPACE, new StringType("RGB 8bits"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_INPUT1, new StringType("HDMI1"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_INPUT2, new StringType("HDMI2"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_INPUT3, new StringType("HDMI3"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_INPUT4, new StringType("HDMI4"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_INPUT5, new StringType("HDMI5"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_INPUT6, new StringType("HDMI6"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_INPUT7, new StringType("HDMI7"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_INPUT8, new StringType("HDMI8"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_REF_STEREO, new StringType("0"), reference_stereo, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_REF_STEREO, new StringType("0"), reference_stereo, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_REF_STEREO, REFRESH, none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_REF_STEREO, REFRESH, none, PROTOCOL_V3, "0"),
                Arguments.of(CHANNEL_MODE_STEREO, new StringType("0"), stereo, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_MUSIC, new StringType("0"), music, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_MOVIE, new StringType("0"), movie, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_DIRECT, new StringType("0"), direct, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_DOLBY, new StringType("0"), dolby, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_DTS, new StringType("0"), dts, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_ALL_STEREO, new StringType("0"), all_stereo, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_MODE_AUTO, new StringType("0"), auto, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SELECTED_MODE, new StringType("Auto"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_SELECTED_MOVIE_MUSIC, new StringType("Surround"), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TREBLE, new DecimalType(0.5), treble_up, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TREBLE, new DecimalType(-1), treble_up, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_TREBLE, new DecimalType(0.5), treble_up, PROTOCOL_V3, "0"),
                Arguments.of(CHANNEL_TREBLE, new DecimalType(-4), treble_down, PROTOCOL_V3, "0"),
                Arguments.of(CHANNEL_BASS, new QuantityType<>(0, Units.DECIBEL), none, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_BASS, new QuantityType<>(-1, Units.DECIBEL), bass_down, PROTOCOL_V2, "0"),
                Arguments.of(CHANNEL_BASS, new QuantityType<>(0, Units.DECIBEL), none, PROTOCOL_V3, "0"),
                Arguments.of(CHANNEL_BASS, new QuantityType<>(-1, Units.DECIBEL), bass_down, PROTOCOL_V3, "0"),
                Arguments.of(CHANNEL_WIDTH, new DecimalType(30), width_trim_set, PROTOCOL_V2, "24.0"),
                Arguments.of(CHANNEL_WIDTH, new DecimalType(30), width_trim_set, PROTOCOL_V3, "24.0"),
                Arguments.of(CHANNEL_WIDTH, new QuantityType<>(-1, Units.DECIBEL), width_trim_set, PROTOCOL_V2, "-2.0"),
                Arguments.of(CHANNEL_WIDTH, new QuantityType<>(-1, Units.DECIBEL), width_trim_set, PROTOCOL_V3, "-2.0"),
                Arguments.of(CHANNEL_HEIGHT, new DecimalType(0.499999), height_trim_set, PROTOCOL_V2, "1.0"),
                Arguments.of(CHANNEL_HEIGHT, new DecimalType(-1.00000000001), height_trim_set, PROTOCOL_V3, "-2.0"),
                Arguments.of(CHANNEL_HEIGHT, new QuantityType<>(-1, Units.DECIBEL), height_trim_set, PROTOCOL_V2,
                        "-2.0"),
                Arguments.of(CHANNEL_HEIGHT, new QuantityType<>(-1, Units.DECIBEL), height_trim_set, PROTOCOL_V3,
                        "-2.0"));
    }

    private static final EnumMap<EmotivaControlCommands, String> map_sources_main_zone = new EnumMap<>(
            EmotivaControlCommands.class);
    private static final EnumMap<EmotivaControlCommands, String> map_sources_zone_2 = new EnumMap<>(
            EmotivaControlCommands.class);
    private static final EnumMap<EmotivaControlCommands, String> channelMap = new EnumMap<>(
            EmotivaControlCommands.class);
    private static final EnumMap<EmotivaControlCommands, String> bandMap = new EnumMap<>(EmotivaControlCommands.class);

    private static final Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Map<EmotivaControlCommands, String>> commandMaps = new ConcurrentHashMap<>();

    @BeforeAll
    static void beforeAll() {
        map_sources_main_zone.put(source_1, "HDMI 1");
        map_sources_main_zone.put(source_2, "SHIELD");
        map_sources_main_zone.put(hdmi1, "HDMI1");
        map_sources_main_zone.put(coax1, "Coax 1");
        commandMaps.put(MAP_SOURCES_MAIN_ZONE, map_sources_main_zone);

        map_sources_zone_2.put(source_1, "HDMI 1");
        map_sources_zone_2.put(source_2, "SHIELD");
        map_sources_zone_2.put(hdmi1, "HDMI1");
        map_sources_zone_2.put(zone2_coax1, "Coax 1");
        map_sources_zone_2.put(zone2_ARC, "Audio Return Channel");
        map_sources_zone_2.put(zone2_follow_main, "Follow Main");
        commandMaps.put(MAP_SOURCES_ZONE_2, map_sources_zone_2);

        channelMap.put(channel_1, "Channel 1");
        channelMap.put(channel_2, "Channel 2");
        channelMap.put(channel_3, "My Radio Channel");
        commandMaps.put(tuner_channel.getName(), channelMap);

        bandMap.put(band_am, "AM");
        bandMap.put(band_fm, "FM");
        commandMaps.put(tuner_band.getName(), bandMap);

        stateMap.put(CHANNEL_TREBLE, new DecimalType(-3));
        stateMap.put(CHANNEL_TUNER_CHANNEL, new StringType("FM    87.50MHz"));
        stateMap.put(CHANNEL_FREQUENCY, QuantityType.valueOf(107.90, Units.HERTZ));
    }

    @ParameterizedTest
    @MethodSource("channelToDTOs")
    void createDTO(String channel, Command ohValue, EmotivaControlCommands controlCommand,
            EmotivaProtocolVersion protocolVersion, String requestValue) {

        EmotivaControlRequest controlRequest = EmotivaCommandHelper.channelToControlRequest(channel, commandMaps,
                protocolVersion);

        EmotivaControlDTO dto = controlRequest.createDTO(ohValue, stateMap.get(channel));
        assertThat(dto.getCommands().size()).isEqualTo(1);
        assertThat(dto.getCommands().get(0).getName()).isEqualTo(controlCommand.name());
        assertThat(dto.getCommands().get(0).getValue()).isEqualTo(requestValue);
        assertThat(dto.getCommands().get(0).getVisible()).isEqualTo(null);
        assertThat(dto.getCommands().get(0).getStatus()).isEqualTo(null);
        assertThat(dto.getCommands().get(0).getAck()).isEqualTo(DEFAULT_CONTROL_ACK_VALUE);
    }
}
