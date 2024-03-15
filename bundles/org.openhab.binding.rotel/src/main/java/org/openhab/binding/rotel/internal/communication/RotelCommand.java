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
package org.openhab.binding.rotel.internal.communication;

import static org.openhab.binding.rotel.internal.RotelBindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;

/**
 * Represents the different kinds of commands
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum RotelCommand {

    POWER_TOGGLE("Power Toggle", PRIMARY_CMD, (byte) 0x0A, "power_toggle", "power_toggle"),
    POWER_OFF("Power Off", PRIMARY_CMD, (byte) 0x4A, "power_off", "power_off"),
    POWER_ON("Power On", PRIMARY_CMD, (byte) 0x4B, "power_on", "power_on"),
    POWER_OFF_ALL_ZONES("Power Off All Zones", PRIMARY_CMD, (byte) 0x71),
    POWER("Request current power status", "get_current_power", "power?"),
    POWER_MODE_QUICK("Set power mode to quick", "power_mode_quick", null),
    POWER_MODE_NORMAL("Set power mode to normal", "power_mode_normal", null),
    POWER_MODE("Request current power mode", "get_power_mode", null),
    ZONE_SELECT("Zone Select", PRIMARY_CMD, (byte) 0x23),
    MAIN_ZONE_POWER_TOGGLE("Main Zone Power Toggle", MAIN_ZONE_CMD, (byte) 0x0A),
    MAIN_ZONE_POWER_OFF("Main Zone Power Off", MAIN_ZONE_CMD, (byte) 0x4A),
    MAIN_ZONE_POWER_ON("Main Zone Power On", MAIN_ZONE_CMD, (byte) 0x4B),
    ZONE2_POWER_TOGGLE("Zone 2 Power Toggle", ZONE2_CMD, (byte) 0x0A),
    ZONE2_POWER_OFF("Zone 2 Power Off", ZONE2_CMD, (byte) 0x4A),
    ZONE2_POWER_ON("Zone 2 Power On", ZONE2_CMD, (byte) 0x4B),
    ZONE3_POWER_TOGGLE("Zone 3 Power Toggle", ZONE3_CMD, (byte) 0x0A),
    ZONE3_POWER_OFF("Zone 3 Power Off", ZONE3_CMD, (byte) 0x4A),
    ZONE3_POWER_ON("Zone 3 Power On", ZONE3_CMD, (byte) 0x4B),
    ZONE4_POWER_TOGGLE("Zone 4 Power Toggle", ZONE4_CMD, (byte) 0x0A),
    ZONE4_POWER_OFF("Zone 4 Power Off", ZONE4_CMD, (byte) 0x4A),
    ZONE4_POWER_ON("Zone 4 Power On", ZONE4_CMD, (byte) 0x4B),
    VOLUME_UP("Volume Up", PRIMARY_CMD, (byte) 0x0B, "volume_up", "vol_up"),
    VOLUME_DOWN("Volume Down", PRIMARY_CMD, (byte) 0x0C, "volume_down", "vol_dwn"),
    VOLUME_SET("Set Volume to level", VOLUME_CMD, (byte) 0, "volume_", "vol_"),
    VOLUME_GET("Request current volume level", "get_volume", "volume?"),
    VOLUME_GET_MIN("Request Min volume level", "get_volume_min", null),
    VOLUME_GET_MAX("Request Max volume level", "get_volume_max", null),
    REMOTE_VOLUME_UP("Remote Volume Up", PRIMARY_CMD, (byte) 0x00),
    REMOTE_VOLUME_DOWN("Remote Volume Down", PRIMARY_CMD, (byte) 0x01),
    MUTE_TOGGLE("Mute Toggle", PRIMARY_CMD, (byte) 0x1E, "mute", "mute"),
    MUTE_ON("Mute On", "mute_on", "mute_on"),
    MUTE_OFF("Mute Off", "mute_off", "mute_off"),
    MUTE("Request current mute status", "get_mute_status", "mute?"),
    MAIN_ZONE_VOLUME_UP("Main Zone Volume Up", MAIN_ZONE_CMD, (byte) 0),
    MAIN_ZONE_VOLUME_DOWN("Main Zone Volume Down", MAIN_ZONE_CMD, (byte) 1),
    MAIN_ZONE_MUTE_TOGGLE("Main Zone Mute Toggle", MAIN_ZONE_CMD, (byte) 0x1E),
    MAIN_ZONE_MUTE_ON("Main Zone Mute On", MAIN_ZONE_CMD, (byte) 0x6C),
    MAIN_ZONE_MUTE_OFF("Main Zone Mute Off", MAIN_ZONE_CMD, (byte) 0x6D),
    ZONE1_VOLUME_UP("Zone 1 Volume Up", null, "z1:vol_up"),
    ZONE1_VOLUME_DOWN("Zone 1 Volume Down", null, "z1:vol_dwn"),
    ZONE1_VOLUME_SET("Set Zone 1 Volume to level", null, "z1:vol_"),
    ZONE1_MUTE_TOGGLE("Zone 1 Mute Toggle", null, "z1:mute"),
    ZONE1_MUTE_ON("Zone 1 Mute On", null, "z1:mute_on"),
    ZONE1_MUTE_OFF("Zone 1 Mute Off", null, "z1:mute_off"),
    ZONE2_VOLUME_UP("Zone 2 Volume Up", ZONE2_CMD, (byte) 0, null, "z2:vol_up"),
    ZONE2_VOLUME_DOWN("Zone 2 Volume Down", ZONE2_CMD, (byte) 1, null, "z2:vol_dwn"),
    ZONE2_VOLUME_SET("Set Zone 2 Volume to level", ZONE2_VOLUME_CMD, (byte) 0, null, "z2:vol_"),
    ZONE2_MUTE_TOGGLE("Zone 2 Mute Toggle", ZONE2_CMD, (byte) 0x1E, null, "z2:mute"),
    ZONE2_MUTE_ON("Zone 2 Mute On", ZONE2_CMD, (byte) 0x6C, null, "z2:mute_on"),
    ZONE2_MUTE_OFF("Zone 2 Mute Off", ZONE2_CMD, (byte) 0x6D, null, "z2:mute_off"),
    ZONE3_VOLUME_UP("Zone 3 Volume Up", ZONE3_CMD, (byte) 0, null, "z3:vol_up"),
    ZONE3_VOLUME_DOWN("Zone 3 Volume Down", ZONE3_CMD, (byte) 1, null, "z3:vol_dwn"),
    ZONE3_VOLUME_SET("Set Zone 3 Volume to level", ZONE3_VOLUME_CMD, (byte) 0, null, "z3:vol_"),
    ZONE3_MUTE_TOGGLE("Zone 3 Mute Toggle", ZONE3_CMD, (byte) 0x1E, null, "z3:mute"),
    ZONE3_MUTE_ON("Zone 3 Mute On", ZONE3_CMD, (byte) 0x6C, null, "z3:mute_on"),
    ZONE3_MUTE_OFF("Zone 3 Mute Off", ZONE3_CMD, (byte) 0x6D, null, "z3:mute_off"),
    ZONE4_VOLUME_UP("Zone 4 Volume Up", ZONE4_CMD, (byte) 0, null, "z4:vol_up"),
    ZONE4_VOLUME_DOWN("Zone 4 Volume Down", ZONE4_CMD, (byte) 1, null, "z4:vol_dwn"),
    ZONE4_VOLUME_SET("Set Zone 4 Volume to level", ZONE4_VOLUME_CMD, (byte) 0, null, "z4:vol_"),
    ZONE4_MUTE_TOGGLE("Zone 4 Mute Toggle", ZONE4_CMD, (byte) 0x1E, null, "z4:mute"),
    ZONE4_MUTE_ON("Zone 4 Mute On", ZONE4_CMD, (byte) 0x6C, null, "z4:mute_on"),
    ZONE4_MUTE_OFF("Zone 4 Mute Off", ZONE4_CMD, (byte) 0x6D, null, "z4:mute_off"),
    SOURCE_CD("Source CD", PRIMARY_CMD, (byte) 0x02, "cd", "cd"),
    SOURCE_TUNER("Source Tuner", PRIMARY_CMD, (byte) 0x03, "tuner", "tuner"),
    SOURCE_TAPE("Source Tape", PRIMARY_CMD, (byte) 0x04, "tape", "tape"),
    SOURCE_VIDEO1("Source Video 1", PRIMARY_CMD, (byte) 0x05, "video1", "video1"),
    SOURCE_VIDEO2("Source Video 2", PRIMARY_CMD, (byte) 0x06, "video2", "video2"),
    SOURCE_VIDEO3("Source Video 3", PRIMARY_CMD, (byte) 0x07, "video3", "video3"),
    SOURCE_VIDEO4("Source Video 4", PRIMARY_CMD, (byte) 0x08, "video4", "video4"),
    SOURCE_VIDEO5("Source Video 5", PRIMARY_CMD, (byte) 0x09, "video5", "video5"),
    SOURCE_VIDEO6("Source Video 6", PRIMARY_CMD, (byte) 0x94, "video6", "video6"),
    SOURCE_VIDEO7("Source Video 7", "video7", "video7"),
    SOURCE_VIDEO8("Source Video 8", "video8", "video8"),
    SOURCE_PHONO("Source Phono", PRIMARY_CMD, (byte) 0x35, "phono", "phono"),
    SOURCE_USB("Source Front USB", PRIMARY_CMD, (byte) 0x8E, "usb", "usb"),
    SOURCE_PCUSB("Source PC USB", "pc_usb", "pcusb"),
    SOURCE_MULTI_INPUT("Source Multi Input", PRIMARY_CMD, (byte) 0x15, "multi_input", "multi_input"),
    SOURCE_AUX("Source Aux", "aux", "aux"),
    SOURCE_AUX1("Source Aux 1", "aux1", "aux1"),
    SOURCE_AUX2("Source Aux 2", "aux2", "aux2"),
    SOURCE_AUX1_COAX("Source Aux 1 Coax", "aux1_coax", "aux1_coax"),
    SOURCE_AUX1_OPT("Source Aux 1 Optical", "aux1_opt", "aux1_opt"),
    SOURCE_COAX1("Source Coax 1", "coax1", "coax1"),
    SOURCE_COAX2("Source Coax 2", "coax2", "coax2"),
    SOURCE_COAX3("Source Coax 3", "coax3", "coax3"),
    SOURCE_OPT1("Source Optical 1", "opt1", "opt1"),
    SOURCE_OPT2("Source Optical 2", "opt2", "opt2"),
    SOURCE_OPT3("Source Optical 3", "opt3", "opt3"),
    SOURCE_BLUETOOTH("Source Bluetooth", "bluetooth", "bluetooth"),
    SOURCE_ROTEL_CD("Source Rotel CD", "rcd", null),
    SOURCE_XLR("Source XLR", "bal_xlr", "bal_xlr"),
    SOURCE_XLR1("Source XLR 1", "bal_xlr1", "bal_xlr1"),
    SOURCE_XLR2("Source XLR 2", "bal_xlr2", "bal_xlr2"),
    SOURCE_FM("Source FM", "fm", "fm"),
    SOURCE_DAB("Source DAB", "dab", "dab"),
    SOURCE_PLAYFI("Source PlayFi", "playfi", "playfi"),
    SOURCE_IRADIO("Source iRadio", "iradio", "iradio"),
    SOURCE_NETWORK("Source Network", "network", "network"),
    SOURCE_INPUT_A("Source Input A", null, "input_a"),
    SOURCE_INPUT_B("Source Input B", null, "input_b"),
    SOURCE_INPUT_C("Source Input C", null, "input_c"),
    SOURCE_INPUT_D("Source Input D", null, "input_d"),
    SOURCE("Request current source", "get_current_source", "source?"),
    INPUT("Request current source", null, "input?"),
    MAIN_ZONE_SOURCE_CD("Main Zone Source CD", MAIN_ZONE_CMD, (byte) 0x02, "main_zone_cd", "main_zone_cd"),
    MAIN_ZONE_SOURCE_TUNER("Main Zone Source Tuner", MAIN_ZONE_CMD, (byte) 0x03, "main_zone_tuner", "main_zone_tuner"),
    MAIN_ZONE_SOURCE_TAPE("Main Zone Source Tape", MAIN_ZONE_CMD, (byte) 0x04, "main_zone_tape", "main_zone_tape"),
    MAIN_ZONE_SOURCE_VIDEO1("Main Zone Source Video 1", MAIN_ZONE_CMD, (byte) 0x05, "main_zone_video1",
            "main_zone_video1"),
    MAIN_ZONE_SOURCE_VIDEO2("Main Zone Source Video 2", MAIN_ZONE_CMD, (byte) 0x06, "main_zone_video2",
            "main_zone_video2"),
    MAIN_ZONE_SOURCE_VIDEO3("Main Zone Source Video 3", MAIN_ZONE_CMD, (byte) 0x07, "main_zone_video3",
            "main_zone_video3"),
    MAIN_ZONE_SOURCE_VIDEO4("Main Zone Source Video 4", MAIN_ZONE_CMD, (byte) 0x08, "main_zone_video4",
            "main_zone_video4"),
    MAIN_ZONE_SOURCE_VIDEO5("Main Zone Source Video 5", MAIN_ZONE_CMD, (byte) 0x09, "main_zone_video5",
            "main_zone_video5"),
    MAIN_ZONE_SOURCE_VIDEO6("Main Zone Source Video 6", MAIN_ZONE_CMD, (byte) 0x94, "main_zone_video6",
            "main_zone_video6"),
    MAIN_ZONE_SOURCE_PHONO("Main Zone Source Phono", MAIN_ZONE_CMD, (byte) 0x35, "main_zone_phono", "main_zone_phono"),
    MAIN_ZONE_SOURCE_USB("Main Zone Source Front USB", MAIN_ZONE_CMD, (byte) 0x8E, "main_zone_usb", "main_zone_usb"),
    MAIN_ZONE_SOURCE_MULTI_INPUT("Main Zone Source Multi Input", MAIN_ZONE_CMD, (byte) 0x15, "main_zone_multi_input",
            "main_zone_multi_input"),
    ZONE1_SOURCE_INPUT_A("Zone 1 Source Input A", null, "z1:input_a"),
    ZONE1_SOURCE_INPUT_B("Zone 1 Source Input B", null, "z1:input_b"),
    ZONE1_SOURCE_INPUT_C("Zone 1 Source Input C", null, "z1:input_c"),
    ZONE1_SOURCE_INPUT_D("Zone 1 Source Input D", null, "z1:input_d"),
    RECORD_SOURCE_CD("Record Source CD", RECORD_SRC_CMD, (byte) 0x02, "record_cd", "record_cd"),
    RECORD_SOURCE_TUNER("Record Source Tuner", RECORD_SRC_CMD, (byte) 0x03, "record_tuner", "record_tuner"),
    RECORD_SOURCE_TAPE("Record Source Tape", RECORD_SRC_CMD, (byte) 0x04, "record_tape", "record_tape"),
    RECORD_SOURCE_VIDEO1("Record Source Video 1", RECORD_SRC_CMD, (byte) 0x05, "record_video1", "record_video1"),
    RECORD_SOURCE_VIDEO2("Record Source Video 2", RECORD_SRC_CMD, (byte) 0x06, "record_video2", "record_video2"),
    RECORD_SOURCE_VIDEO3("Record Source Video 3", RECORD_SRC_CMD, (byte) 0x07, "record_video3", "record_video3"),
    RECORD_SOURCE_VIDEO4("Record Source Video 4", RECORD_SRC_CMD, (byte) 0x08, "record_video4", "record_video4"),
    RECORD_SOURCE_VIDEO5("Record Source Video 5", RECORD_SRC_CMD, (byte) 0x09, "record_video5", "record_video5"),
    RECORD_SOURCE_VIDEO6("Record Source Video 6", RECORD_SRC_CMD, (byte) 0x94, "record_video6", "record_video6"),
    RECORD_SOURCE_PHONO("Record Source Phono", RECORD_SRC_CMD, (byte) 0x35, "record_phono", "record_phono"),
    RECORD_SOURCE_USB("Record Source Front USB", RECORD_SRC_CMD, (byte) 0x8E, "record_usb", "record_usb"),
    RECORD_SOURCE_MAIN("Record Follow Main Zone Source", RECORD_SRC_CMD, (byte) 0x6B, "record_follow_main",
            "record_follow_main"),
    ZONE2_SOURCE_CD("Zone 2 Source CD", ZONE2_CMD, (byte) 0x02, "zone2_cd", "zone2_cd"),
    ZONE2_SOURCE_TUNER("Zone 2 Source Tuner", ZONE2_CMD, (byte) 0x03, "zone2_tuner", "zone2_tuner"),
    ZONE2_SOURCE_TAPE("Zone 2 Source Tape", ZONE2_CMD, (byte) 0x04, "zone2_tape", "zone2_tape"),
    ZONE2_SOURCE_VIDEO1("Zone 2 Source Video 1", ZONE2_CMD, (byte) 0x05, "zone2_video1", "zone2_video1"),
    ZONE2_SOURCE_VIDEO2("Zone 2 Source Video 2", ZONE2_CMD, (byte) 0x06, "zone2_video2", "zone2_video2"),
    ZONE2_SOURCE_VIDEO3("Zone 2 Source Video 3", ZONE2_CMD, (byte) 0x07, "zone2_video3", "zone2_video3"),
    ZONE2_SOURCE_VIDEO4("Zone 2 Source Video 4", ZONE2_CMD, (byte) 0x08, "zone2_video4", "zone2_video4"),
    ZONE2_SOURCE_VIDEO5("Zone 2 Source Video 5", ZONE2_CMD, (byte) 0x09, "zone2_video5", "zone2_video5"),
    ZONE2_SOURCE_VIDEO6("Zone 2 Source Video 6", ZONE2_CMD, (byte) 0x94, "zone2_video6", "zone2_video6"),
    ZONE2_SOURCE_PHONO("Zone 2 Source Phono", ZONE2_CMD, (byte) 0x35, "zone2_phono", "zone2_phono"),
    ZONE2_SOURCE_USB("Zone 2 Source Front USB", ZONE2_CMD, (byte) 0x8E, "zone2_usb", "zone2_usb"),
    ZONE2_SOURCE_MAIN("Zone 2 Follow Main Zone Source", ZONE2_CMD, (byte) 0x6B, "zone2_follow_main",
            "zone2_follow_main"),
    ZONE2_SOURCE_INPUT_A("Zone 2 Source Input A", null, "z2:input_a"),
    ZONE2_SOURCE_INPUT_B("Zone 2 Source Input B", null, "z2:input_b"),
    ZONE2_SOURCE_INPUT_C("Zone 2 Source Input C", null, "z2:input_c"),
    ZONE2_SOURCE_INPUT_D("Zone 2 Source Input D", null, "z2:input_d"),
    ZONE3_SOURCE_CD("Zone 3 Source CD", ZONE3_CMD, (byte) 0x02, "zone3_cd", "zone3_cd"),
    ZONE3_SOURCE_TUNER("Zone 3 Source Tuner", ZONE3_CMD, (byte) 0x03, "zone3_tuner", "zone3_tuner"),
    ZONE3_SOURCE_TAPE("Zone 3 Source Tape", ZONE3_CMD, (byte) 0x04, "zone3_tape", "zone3_tape"),
    ZONE3_SOURCE_VIDEO1("Zone 3 Source Video 1", ZONE3_CMD, (byte) 0x05, "zone3_video1", "zone3_video1"),
    ZONE3_SOURCE_VIDEO2("Zone 3 Source Video 2", ZONE3_CMD, (byte) 0x06, "zone3_video2", "zone3_video2"),
    ZONE3_SOURCE_VIDEO3("Zone 3 Source Video 3", ZONE3_CMD, (byte) 0x07, "zone3_video3", "zone3_video3"),
    ZONE3_SOURCE_VIDEO4("Zone 3 Source Video 4", ZONE3_CMD, (byte) 0x08, "zone3_video4", "zone3_video4"),
    ZONE3_SOURCE_VIDEO5("Zone 3 Source Video 5", ZONE3_CMD, (byte) 0x09, "zone3_video5", "zone3_video5"),
    ZONE3_SOURCE_VIDEO6("Zone 3 Source Video 6", ZONE3_CMD, (byte) 0x94, "zone3_video6", "zone3_video6"),
    ZONE3_SOURCE_PHONO("Zone 3 Source Phono", ZONE3_CMD, (byte) 0x35, "zone3_phono", "zone3_phono"),
    ZONE3_SOURCE_USB("Zone 3 Source Front USB", ZONE3_CMD, (byte) 0x8E, "zone3_usb", "zone3_usb"),
    ZONE3_SOURCE_MAIN("Zone 3 Follow Main Zone Source", ZONE3_CMD, (byte) 0x6B, "zone3_follow_main",
            "zone3_follow_main"),
    ZONE3_SOURCE_INPUT_A("Zone 3 Source Input A", null, "z3:input_a"),
    ZONE3_SOURCE_INPUT_B("Zone 3 Source Input B", null, "z3:input_b"),
    ZONE3_SOURCE_INPUT_C("Zone 3 Source Input C", null, "z3:input_c"),
    ZONE3_SOURCE_INPUT_D("Zone 3 Source Input D", null, "z3:input_d"),
    ZONE4_SOURCE_CD("Zone 4 Source CD", ZONE4_CMD, (byte) 0x02, "zone4_cd", "zone4_cd"),
    ZONE4_SOURCE_TUNER("Zone 4 Source Tuner", ZONE4_CMD, (byte) 0x03, "zone4_tuner", "zone4_tuner"),
    ZONE4_SOURCE_TAPE("Zone 4 Source Tape", ZONE4_CMD, (byte) 0x04, "zone4_tape", "zone4_tape"),
    ZONE4_SOURCE_VIDEO1("Zone 4 Source Video 1", ZONE4_CMD, (byte) 0x05, "zone4_video1", "zone4_video1"),
    ZONE4_SOURCE_VIDEO2("Zone 4 Source Video 2", ZONE4_CMD, (byte) 0x06, "zone4_video2", "zone4_video2"),
    ZONE4_SOURCE_VIDEO3("Zone 4 Source Video 3", ZONE4_CMD, (byte) 0x07, "zone4_video3", "zone4_video3"),
    ZONE4_SOURCE_VIDEO4("Zone 4 Source Video 4", ZONE4_CMD, (byte) 0x08, "zone4_video4", "zone4_video4"),
    ZONE4_SOURCE_VIDEO5("Zone 4 Source Video 5", ZONE4_CMD, (byte) 0x09, "zone4_video5", "zone4_video5"),
    ZONE4_SOURCE_VIDEO6("Zone 4 Source Video 6", ZONE4_CMD, (byte) 0x94, "zone4_video6", "zone4_video6"),
    ZONE4_SOURCE_PHONO("Zone 4 Source Phono", ZONE4_CMD, (byte) 0x35, "zone4_phono", "zone4_phono"),
    ZONE4_SOURCE_USB("Zone 4 Source Front USB", ZONE4_CMD, (byte) 0x8E, "zone4_usb", "zone4_usb"),
    ZONE4_SOURCE_MAIN("Zone 4 Follow Main Zone Source", ZONE4_CMD, (byte) 0x6B, "zone4_follow_main",
            "zone4_follow_main"),
    ZONE4_SOURCE_INPUT_A("Zone 4 Source Input A", null, "z4:input_a"),
    ZONE4_SOURCE_INPUT_B("Zone 4 Source Input B", null, "z4:input_b"),
    ZONE4_SOURCE_INPUT_C("Zone 4 Source Input C", null, "z4:input_c"),
    ZONE4_SOURCE_INPUT_D("Zone 4 Source Input D", null, "z4:input_d"),
    STEREO("Stereo", PRIMARY_CMD, (byte) 0x11, "2channel", "2channel"),
    STEREO_BYPASS_TOGGLE("Stereo / Bypass Toggle", PRIMARY_CMD, (byte) 0x11),
    STEREO3("Dolby 3 Stereo ", PRIMARY_CMD, (byte) 0x12, "3channel", "3channel"),
    STEREO5("5 Channel Stereo", PRIMARY_CMD, (byte) 0x5B, "5channel", "5channel"),
    STEREO7("7 Channel Stereo", PRIMARY_CMD, (byte) 0x5C, "7channel", "7channel"),
    STEREO9("9 Channel Stereo", "9channel", "9channel"),
    STEREO11("11 Channel Stereo", "11channel", "11channel"),
    PROLOGIC_TOGGLE("Dolby Pro Logic Toggle", PRIMARY_CMD, (byte) 0x13),
    DSP_TOGGLE("DSP Music Mode Toggle", PRIMARY_CMD, (byte) 0x14),
    DSP1("DSP 1", PRIMARY_CMD, (byte) 0x57),
    DSP2("DSP 2", PRIMARY_CMD, (byte) 0x58),
    DSP3("DSP 3", PRIMARY_CMD, (byte) 0x59),
    DSP4("DSP 4", PRIMARY_CMD, (byte) 0x5A),
    DOLBY_TOGGLE("Dolby 3 Stereo / Pro Logic Toggle", PRIMARY_CMD, (byte) 0x53),
    PROLOGIC("Dolby Pro Logic", PRIMARY_CMD, (byte) 0x5F),
    PLII_CINEMA("Dolby PLII Cinema", PRIMARY_CMD, (byte) 0x5D, "prologic_movie", "prologic_movie"),
    PLII_MUSIC("Dolby PLII Music", PRIMARY_CMD, (byte) 0x5E, "prologic_music", "prologic_music"),
    PLII_GAME("Dolby PLII Game", PRIMARY_CMD, (byte) 0x74, "prologic_game", "prologic_game"),
    PLIIZ("Dolby PLIIz", PRIMARY_CMD, (byte) 0x92, "prologic_iiz", "prologic_iiz"),
    PLII_PANORAMA_TOGGLE("PLII Panorama Toggle", PRIMARY_CMD, (byte) 0x62),
    PLII_DIMENSION_UP("PLII Dimension Up", PRIMARY_CMD, (byte) 0x63),
    PLII_DIMENSION_DOWN("PLII Dimension Down", PRIMARY_CMD, (byte) 0x64),
    PLII_CENTER_WIDTH_UP("PLII Center Width Up", PRIMARY_CMD, (byte) 0x65),
    PLII_CENTER_WIDTH_DOWN("PLII Center Width Down", PRIMARY_CMD, (byte) 0x66),
    DDEX_TOGGLE("Dolby Digital EX Toggle", PRIMARY_CMD, (byte) 0x68),
    NEO6_TOGGLE("dts Neo:6 Music/Cinema Toggle", PRIMARY_CMD, (byte) 0x54),
    NEO6_MUSIC("dts Neo:6 Music", PRIMARY_CMD, (byte) 0x60, "neo6_music", "neo6_music"),
    NEO6_CINEMA("dts Neo:6 Cinema", PRIMARY_CMD, (byte) 0x61, "neo6_cinema", "neo6_cinema"),
    ATMOS("Dolby Atmos", "dolby_atmos", "dolby_atmos"),
    NEURAL_X("dts Neural:X", "dts_neural", "dts_neural"),
    BYPASS("Analog Bypass", "bypass", "bypass"),
    NEXT_MODE("Next Surround Mode", PRIMARY_CMD, (byte) 0x22, "surround_next", null),
    DSP_MODE("Request current DSP mode", "get_dsp_mode", null),
    TONE_MAX("Request Max tone level", "get_tone_max", null),
    TONE_CONTROL_SELECT("Tone Control Select", PRIMARY_CMD, (byte) 0x67),
    TREBLE_UP("Treble Up", PRIMARY_CMD, (byte) 0x0D, "treble_up", "treble_up"),
    TREBLE_DOWN("Treble Down", PRIMARY_CMD, (byte) 0x0E, "treble_down", "treble_down"),
    TREBLE_SET("Set Treble to level", "treble_", "treble_"),
    TREBLE("Request current treble level", "get_treble", "treble?"),
    BASS_UP("Bass Up", PRIMARY_CMD, (byte) 0x0F, "bass_up", "bass_up"),
    BASS_DOWN("Bass Down", PRIMARY_CMD, (byte) 0x10, "bass_down", "bass_down"),
    BASS_SET("Set Bass to level", "bass_", "bass_"),
    BASS("Request current bass level", "get_bass", "bass?"),
    ZONE1_TREBLE_UP("Zone 1 Treble Up", null, "z1:treble_up"),
    ZONE1_TREBLE_DOWN("Zone 1 Treble Down", null, "z1:treble_down"),
    ZONE1_TREBLE_SET("Set Zone 1 Treble to level", null, "z1:treble_"),
    ZONE1_BASS_UP("Zone 1 Bass Up", null, "z1:bass_up"),
    ZONE1_BASS_DOWN("Zone 1 Bass Down", null, "z1:bass_down"),
    ZONE1_BASS_SET("Set Zone 1 Bass to level", null, "z1:bass_"),
    ZONE2_TREBLE_UP("Zone 2 Treble Up", null, "z2:treble_up"),
    ZONE2_TREBLE_DOWN("Zone 2 Treble Down", null, "z2:treble_down"),
    ZONE2_TREBLE_SET("Set Zone 2 Treble to level", null, "z2:treble_"),
    ZONE2_BASS_UP("Zone 2 Bass Up", null, "z2:bass_up"),
    ZONE2_BASS_DOWN("Zone 2 Bass Down", null, "z2:bass_down"),
    ZONE2_BASS_SET("Set Zone 2 Bass to level", null, "z2:bass_"),
    ZONE3_TREBLE_UP("Zone 3 Treble Up", null, "z3:treble_up"),
    ZONE3_TREBLE_DOWN("Zone 3 Treble Down", null, "z3:treble_down"),
    ZONE3_TREBLE_SET("Set Zone 3 Treble to level", null, "z3:treble_"),
    ZONE3_BASS_UP("Zone 3 Bass Up", null, "z3:bass_up"),
    ZONE3_BASS_DOWN("Zone 3 Bass Down", null, "z3:bass_down"),
    ZONE3_BASS_SET("Set Zone 3 Bass to level", null, "z3:bass_"),
    ZONE4_TREBLE_UP("Zone 4 Treble Up", null, "z4:treble_up"),
    ZONE4_TREBLE_DOWN("Zone 4 Treble Down", null, "z4:treble_down"),
    ZONE4_TREBLE_SET("Set Zone 4 Treble to level", null, "z4:treble_"),
    ZONE4_BASS_UP("Zone 4 Bass Up", null, "z4:bass_up"),
    ZONE4_BASS_DOWN("Zone 4 Bass Down", null, "z4:bass_down"),
    ZONE4_BASS_SET("Set Zone 4 Bass to level", null, "z4:bass_"),
    RECORD_FONCTION_SELECT("Record Function Select", PRIMARY_CMD, (byte) 0x17),
    PLAY("Play Source", PRIMARY_CMD, (byte) 0x04, "play", "play"),
    STOP("Stop Source", PRIMARY_CMD, (byte) 0x06, "stop", "stop"),
    PAUSE("Pause Source", PRIMARY_CMD, (byte) 0x05, "pause", "pause"),
    CD_PLAY_STATUS("Request CD play status", "get_cd_play_status", null),
    PLAY_STATUS("Request source play status", "get_play_status", "status?"),
    RANDOM_TOGGLE("Random Play Mode Toggle", PRIMARY_CMD, (byte) 0x25, "random", "rnd"),
    RANDOM_MODE("Request current random play mode", null, "rnd?"),
    REPEAT_TOGGLE("Repeat Play Mode Toggle", PRIMARY_CMD, (byte) 0x26, "repeat", "rpt"),
    REPEAT_MODE("Request current repeat play mode", null, "rpt?"),
    TRACK_FWD("Track Forward/Tune Up", PRIMARY_CMD, (byte) 0x09, "track_fwd", "trkf"),
    TRACK_BACK("Track Backward/Tune Down", PRIMARY_CMD, (byte) 0x08, "track_back", "trkb"),
    FAST_FWD("Fast Forward/Search Forward", PRIMARY_CMD, (byte) 0x0B, "fast_fwd", "ff"),
    FAST_BACK("Fast Backward/Search Backward", PRIMARY_CMD, (byte) 0x0A, "fast_back", "fb"),
    TRACK("Request current CD track number", null, "track?"),
    EJECT("Eject CD", "eject", "eject"),
    TIME_TOGGLE("Toggle CD Time Display", "time", "time"),
    FREQUENCY("Request current frequency for digital source input", "get_current_freq", "freq?"),
    DISPLAY_REFRESH("Display Refresh", PRIMARY_CMD, (byte) 0xFF),
    DIMMER_LEVEL_GET("Request current front display dimmer level", "get_current_dimmer", "dimmer?"),
    DIMMER_LEVEL_SET("Set front display dimmer to level", "dimmer_", "dimmer_"),
    UPDATE_AUTO("Set Update to Auto", "display_update_auto", "rs232_update_on"),
    UPDATE_MANUAL("Set Update to Manual", "display_update_manual", "rs232_update_off"),
    TONE_CONTROLS_ON("Tone Controls On", "tone_on", null),
    TONE_CONTROLS_OFF("Tone Controls Off", "tone_off", null),
    TONE_CONTROLS("Request current tone control state", "get_tone", null),
    TCBYPASS_ON("Bypass On", null, "bypass_on"),
    TCBYPASS_OFF("Bypass Off", null, "bypass_off"),
    TCBYPASS("Request current tone bypass state", null, "bypass?"),
    BALANCE_RIGHT("Balance Right", "balance_right", "balance_r"),
    BALANCE_LEFT("Balance Left", "balance_left", "balance_l"),
    BALANCE_SET("Set Balance to level", "balance_", "balance_"),
    ZONE1_BALANCE_RIGHT("Zone 1 Balance Right", null, "z1:balance_r"),
    ZONE1_BALANCE_LEFT("Zone 1 Balance Left", null, "z1:balance_l"),
    ZONE1_BALANCE_SET("Set Zone 1 Balance to level", null, "z1:balance_"),
    ZONE2_BALANCE_RIGHT("Zone 2 Balance Right", null, "z2:balance_r"),
    ZONE2_BALANCE_LEFT("Zone 2 Balance Left", null, "z2:balance_l"),
    ZONE2_BALANCE_SET("Set Zone 2 Balance to level", null, "z2:balance_"),
    ZONE3_BALANCE_RIGHT("Zone 3 Balance Right", null, "z3:balance_r"),
    ZONE3_BALANCE_LEFT("Zone 3 Balance Left", null, "z3:balance_l"),
    ZONE3_BALANCE_SET("Set Zone 3 Balance to level", null, "z3:balance_"),
    ZONE4_BALANCE_RIGHT("Zone 4 Balance Right", null, "z4:balance_r"),
    ZONE4_BALANCE_LEFT("Zone 4 Balance Left", null, "z4:balance_l"),
    ZONE4_BALANCE_SET("Set Zone 4 Balance to level", null, "z4:balance_"),
    BALANCE("Request current balance setting", "get_balance", "balance?"),
    SPEAKER_A_TOGGLE("Toggle Speaker A Output", PRIMARY_CMD, (byte) 0x50, "speaker_a", "speaker_a"),
    SPEAKER_A_ON("Set Speaker A Output", "speaker_a_on", "speaker_a_on"),
    SPEAKER_A_OFF("Unset Speaker A Output", "speaker_a_off", "speaker_a_off"),
    SPEAKER_B_TOGGLE("Toggle Speaker B Output", PRIMARY_CMD, (byte) 0x51, "speaker_b", "speaker_b"),
    SPEAKER_B_ON("Set Speaker B Output", "speaker_b_on", "speaker_b_on"),
    SPEAKER_B_OFF("Unset Speaker B Output", "speaker_b_off", "speaker_b_off"),
    SPEAKER("Request current active speaker outputs", "get_current_speaker", "speaker?"),
    TUNE_UP("Tune Up", PRIMARY_CMD, (byte) 0x28),
    TUNE_DOWN("Tune Down", PRIMARY_CMD, (byte) 0x29),
    PRESET_UP("Preset Up", PRIMARY_CMD, (byte) 0x6F),
    PRESET_DOWN("Preset Down", PRIMARY_CMD, (byte) 0x70),
    FREQUENCY_UP("Frequency Up", PRIMARY_CMD, (byte) 0x72),
    FREQUENCY_DOWN("Frequency Down", PRIMARY_CMD, (byte) 0x73),
    MEMORY("Memory", PRIMARY_CMD, (byte) 0x27),
    BAND_TOGGLE("Band Toggle", PRIMARY_CMD, (byte) 0x24),
    AM("AM", PRIMARY_CMD, (byte) 0x56),
    FM("FM", PRIMARY_CMD, (byte) 0x55),
    TUNE_PRESET_TOGGLE("Tune / Preset", PRIMARY_CMD, (byte) 0x20),
    TUNING_MODE_SELECT("Tuning Mode Select", PRIMARY_CMD, (byte) 0x69),
    PRESET_MODE_SELECT("Preset Mode Select", PRIMARY_CMD, (byte) 0x6A),
    FREQUENCY_DIRECT("Frequency Direct", PRIMARY_CMD, (byte) 0x25),
    PRESET_SCAN("Preset Scan", PRIMARY_CMD, (byte) 0x21),
    TUNER_DISPLAY("Tuner Display", PRIMARY_CMD, (byte) 0x44),
    RDS_PTY("RDS PTY", PRIMARY_CMD, (byte) 0x45),
    RDS_TP("RDS TP", PRIMARY_CMD, (byte) 0x46),
    RDS_TA("RDS TA", PRIMARY_CMD, (byte) 0x47),
    FM_MONO_TOGGLE("FM Mono", PRIMARY_CMD, (byte) 0x26, "fm_mono", null),
    CALL_FM_PRESET("Recall FM Preset", "call_fm_preset_", "fm_"),
    CALL_DAB_PRESET("Recall DAB Preset", "call_dab_preset_", "dab_"),
    CALL_IRADIO_PRESET("Recall iRadio Preset", "call_iradio_preset_", null),
    PRESET("Request current preset", "get_current_preset", null),
    FM_PRESET("Request current FM preset number", null, "fm?"),
    DAB_PRESET("Request current DAB preset number", null, "dab?"),
    ZONE2_TUNE_UP("Zone 2 Tune Up", ZONE2_CMD, (byte) 0x28),
    ZONE2_TUNE_DOWN("Zone 2 Tune Down", ZONE2_CMD, (byte) 0x29),
    ZONE2_PRESET_UP("Zone 2 Preset Up", ZONE2_CMD, (byte) 0x6F),
    ZONE2_PRESET_DOWN("Zone 2 Preset Down", ZONE2_CMD, (byte) 0x70),
    ZONE2_FREQUENCY_UP("Zone 2 Frequency Up", ZONE2_CMD, (byte) 0x72),
    ZONE2_FREQUENCY_DOWN("Zone 2 Frequency Down", ZONE2_CMD, (byte) 0x73),
    ZONE2_BAND_TOGGLE("Zone 2 Band Toggle", ZONE2_CMD, (byte) 0x24),
    ZONE2_AM("Zone 2 AM", ZONE2_CMD, (byte) 0x56),
    ZONE2_FM("Zone 2 FM", ZONE2_CMD, (byte) 0x55),
    ZONE2_TUNE_PRESET_TOGGLE("Zone 2 Tune / Preset", ZONE2_CMD, (byte) 0x20),
    ZONE2_TUNING_MODE_SELECT("Zone 2 Tuning Mode Select", ZONE2_CMD, (byte) 0x69),
    ZONE2_PRESET_MODE_SELECT("Zone 2 Preset Mode Select", ZONE2_CMD, (byte) 0x6A),
    ZONE2_PRESET_SCAN("Zone 2 Preset Scan", ZONE2_CMD, (byte) 0x21),
    ZONE2_FM_MONO_TOGGLE("Zone 2 FM Mono", ZONE2_CMD, (byte) 0x26),
    ZONE3_TUNE_UP("Zone 3 Tune Up", ZONE3_CMD, (byte) 0x28),
    ZONE3_TUNE_DOWN("Zone 3 Tune Down", ZONE3_CMD, (byte) 0x29),
    ZONE3_PRESET_UP("Zone 3 Preset Up", ZONE3_CMD, (byte) 0x6F),
    ZONE3_PRESET_DOWN("Zone 3 Preset Down", ZONE3_CMD, (byte) 0x70),
    ZONE3_FREQUENCY_UP("Zone 3 Frequency Up", ZONE3_CMD, (byte) 0x72),
    ZONE3_FREQUENCY_DOWN("Zone 3 Frequency Down", ZONE3_CMD, (byte) 0x73),
    ZONE3_BAND_TOGGLE("Zone 3 Band Toggle", ZONE3_CMD, (byte) 0x24),
    ZONE3_AM("Zone 3 AM", ZONE3_CMD, (byte) 0x56),
    ZONE3_FM("Zone 3 FM", ZONE3_CMD, (byte) 0x55),
    ZONE3_TUNE_PRESET_TOGGLE("Zone 3 Tune / Preset", ZONE3_CMD, (byte) 0x20),
    ZONE3_TUNING_MODE_SELECT("Zone 3 Tuning Mode Select", ZONE3_CMD, (byte) 0x69),
    ZONE3_PRESET_MODE_SELECT("Zone 3 Preset Mode Select", ZONE3_CMD, (byte) 0x6A),
    ZONE3_PRESET_SCAN("Zone 3 Preset Scan", ZONE3_CMD, (byte) 0x21),
    ZONE3_FM_MONO_TOGGLE("Zone 3 FM Mono", ZONE3_CMD, (byte) 0x26),
    ZONE4_TUNE_UP("Zone 4 Tune Up", ZONE4_CMD, (byte) 0x28),
    ZONE4_TUNE_DOWN("Zone 4 Tune Down", ZONE4_CMD, (byte) 0x29),
    ZONE4_PRESET_UP("Zone 4 Preset Up", ZONE4_CMD, (byte) 0x6F),
    ZONE4_PRESET_DOWN("Zone 4 Preset Down", ZONE4_CMD, (byte) 0x70),
    ZONE4_FREQUENCY_UP("Zone 4 Frequency Up", ZONE4_CMD, (byte) 0x72),
    ZONE4_FREQUENCY_DOWN("Zone 4 Frequency Down", ZONE4_CMD, (byte) 0x73),
    ZONE4_BAND_TOGGLE("Zone 4 Band Toggle", ZONE4_CMD, (byte) 0x24),
    ZONE4_AM("Zone 4 AM", ZONE4_CMD, (byte) 0x56),
    ZONE4_FM("Zone 4 FM", ZONE4_CMD, (byte) 0x55),
    ZONE4_TUNE_PRESET_TOGGLE("Zone 4 Tune / Preset", ZONE4_CMD, (byte) 0x20),
    ZONE4_TUNING_MODE_SELECT("Zone 4 Tuning Mode Select", ZONE4_CMD, (byte) 0x69),
    ZONE4_PRESET_MODE_SELECT("Zone 4 Preset Mode Select", ZONE4_CMD, (byte) 0x6A),
    ZONE4_PRESET_SCAN("Zone 4 Preset Scan", ZONE4_CMD, (byte) 0x21),
    ZONE4_FM_MONO_TOGGLE("Zone 4 FM Mono", ZONE4_CMD, (byte) 0x26),
    MENU("Display the Menu", PRIMARY_CMD, (byte) 0x18, "menu", null),
    EXIT("Exit Key", PRIMARY_CMD, (byte) 0x90, "exit", null),
    UP("Cursor Up", PRIMARY_CMD, (byte) 0x1C, "up", null),
    UP_PRESSED("Cursor Up – Key Pressed", PRIMARY_CMD, (byte) 0x1C),
    UP_RELEASED("Cursor Up – Key Released", (byte) 0x11, (byte) 0x1C),
    DOWN("Cursor Down", PRIMARY_CMD, (byte) 0x1D, "down", null),
    DOWN_PRESSED("Cursor Down – Key Pressed", PRIMARY_CMD, (byte) 0x1D),
    DOWN_RELEASED("Cursor Down – Key Released", (byte) 0x11, (byte) 0x1D),
    LEFT("Cursor Left", PRIMARY_CMD, (byte) 0x1B, "left", null),
    LEFT_PRESSED("Cursor Left – Key Pressed", PRIMARY_CMD, (byte) 0x1B),
    LEFT_RELEASED("Cursor Left – Key Released", (byte) 0x11, (byte) 0x1B),
    RIGHT("Cursor Right", PRIMARY_CMD, (byte) 0x1A, "right", null),
    ENTER("Enter Key", PRIMARY_CMD, (byte) 0x19, "enter", null),
    RIGHT_PRESSED("Cursor Right – Key Pressed", PRIMARY_CMD, (byte) 0x1A),
    RIGHT_RELEASED("Cursor Right – Key Released", (byte) 0x11, (byte) 0x1A),
    KEY1("Number Key 1", PRIMARY_CMD, (byte) 0x2A, "1", "1"),
    KEY2("Number Key 2", PRIMARY_CMD, (byte) 0x2B, "2", "2"),
    KEY3("Number Key 3", PRIMARY_CMD, (byte) 0x2C, "3", "3"),
    KEY4("Number Key 4", PRIMARY_CMD, (byte) 0x2D, "4", "4"),
    KEY5("Number Key 5", PRIMARY_CMD, (byte) 0x2E, "5", "5"),
    KEY6("Number Key 6", PRIMARY_CMD, (byte) 0x2F, "6", "6"),
    KEY7("Number Key 7", PRIMARY_CMD, (byte) 0x30, "7", "7"),
    KEY8("Number Key 8", PRIMARY_CMD, (byte) 0x31, "8", "8"),
    KEY9("Number Key 9", PRIMARY_CMD, (byte) 0x32, "9", "9"),
    KEY0("Number Key 0", PRIMARY_CMD, (byte) 0x33, "0", "0"),
    ZONE2_KEY1("Zone 2 Number Key 1", ZONE2_CMD, (byte) 0x2A),
    ZONE2_KEY2("Zone 2 Number Key 2", ZONE2_CMD, (byte) 0x2B),
    ZONE2_KEY3("Zone 2 Number Key 3", ZONE2_CMD, (byte) 0x2C),
    ZONE2_KEY4("Zone 2 Number Key 4", ZONE2_CMD, (byte) 0x2D),
    ZONE2_KEY5("Zone 2 Number Key 5", ZONE2_CMD, (byte) 0x2E),
    ZONE2_KEY6("Zone 2 Number Key 6", ZONE2_CMD, (byte) 0x2F),
    ZONE2_KEY7("Zone 2 Number Key 7", ZONE2_CMD, (byte) 0x30),
    ZONE2_KEY8("Zone 2 Number Key 8", ZONE2_CMD, (byte) 0x31),
    ZONE2_KEY9("Zone 2 Number Key 9", ZONE2_CMD, (byte) 0x32),
    ZONE2_KEY0("Zone 2 Number Key 0", ZONE2_CMD, (byte) 0x33),
    ZONE3_KEY1("Zone 3 Number Key 1", ZONE3_CMD, (byte) 0x2A),
    ZONE3_KEY2("Zone 3 Number Key 2", ZONE3_CMD, (byte) 0x2B),
    ZONE3_KEY3("Zone 3 Number Key 3", ZONE3_CMD, (byte) 0x2C),
    ZONE3_KEY4("Zone 3 Number Key 4", ZONE3_CMD, (byte) 0x2D),
    ZONE3_KEY5("Zone 3 Number Key 5", ZONE3_CMD, (byte) 0x2E),
    ZONE3_KEY6("Zone 3 Number Key 6", ZONE3_CMD, (byte) 0x2F),
    ZONE3_KEY7("Zone 3 Number Key 7", ZONE3_CMD, (byte) 0x30),
    ZONE3_KEY8("Zone 3 Number Key 8", ZONE3_CMD, (byte) 0x31),
    ZONE3_KEY9("Zone 3 Number Key 9", ZONE3_CMD, (byte) 0x32),
    ZONE3_KEY0("Zone 3 Number Key 0", ZONE3_CMD, (byte) 0x33),
    ZONE4_KEY1("Zone 4 Number Key 1", ZONE4_CMD, (byte) 0x2A),
    ZONE4_KEY2("Zone 4 Number Key 2", ZONE4_CMD, (byte) 0x2B),
    ZONE4_KEY3("Zone 4 Number Key 3", ZONE4_CMD, (byte) 0x2C),
    ZONE4_KEY4("Zone 4 Number Key 4", ZONE4_CMD, (byte) 0x2D),
    ZONE4_KEY5("Zone 4 Number Key 5", ZONE4_CMD, (byte) 0x2E),
    ZONE4_KEY6("Zone 4 Number Key 6", ZONE4_CMD, (byte) 0x2F),
    ZONE4_KEY7("Zone 4 Number Key 7", ZONE4_CMD, (byte) 0x30),
    ZONE4_KEY8("Zone 4 Number Key 8", ZONE4_CMD, (byte) 0x31),
    ZONE4_KEY9("Zone 4 Number Key 9", ZONE4_CMD, (byte) 0x32),
    ZONE4_KEY0("Zone 4 Number Key 0", ZONE4_CMD, (byte) 0x33),
    PROGRAM("Program Key", "program", null),
    PCUSB_CLASS_1("Set PC-USB Audio Class to 1.0", "pcusb_class_1", "pcusb_class_1"),
    PCUSB_CLASS_2("Set PC-USB Audio Class to 2.0", "pcusb_class_2", "pcusb_class_2"),
    PCUSB_CLASS("Request current PC-USB class", "get_pcusb_class", "pcusb?"),
    RESET_FACTORY("Reset unit to factory defaults", PRIMARY_CMD, (byte) 0x93, "factory_default_on",
            "factory_default_on"),
    DYNAMIC_RANGE("Dynamic Range", PRIMARY_CMD, (byte) 0x16),
    DIGITAL_INPUT_SELECT("Digital Input Select", PRIMARY_CMD, (byte) 0x1F),
    ZONE_TOGGLE("Zone Toggle", PRIMARY_CMD, (byte) 0x23),
    CENTER_TRIM("Temporary Center Trim", PRIMARY_CMD, (byte) 0x4C),
    SUB_TRIM("Temporary Subwoofer  Trim", PRIMARY_CMD, (byte) 0x4D),
    SURROUND_TRIM("Temporary Surround  Trim", PRIMARY_CMD, (byte) 0x4E),
    SUB_LEVEL_UP("Temporary increase sub level", "subwoofer_up", null),
    SUB_LEVEL_DOWN("Temporary decrease sub level", "subwoofer_down", null),
    C_LEVEL_UP("Temporary increase center level", "center_up", null),
    C_LEVEL_DOWN("Temporary decrease center level", "center_down", null),
    SR_LEVEL_UP("Temporary increase surround right level", "surround_right_up", null),
    SR_LEVEL_DOWN("Temporary decrease surround right level", "surround_right_down", null),
    SL_LEVEL_UP("Temporary increase surround left level", "surround_left_up", null),
    SL_LEVEL_DOWN("Temporary decrease surround left level", "surround_left_down", null),
    CBR_LEVEL_UP("Temporary increase center back right level", "center_back_right_up", null),
    CBR_LEVEL_DOWN("Temporary decrease center back right level", "center_back_right_down", null),
    CBL_LEVEL_UP("Temporary increase center back left level", "center_back_left_up", null),
    CBL_LEVEL_DOWN("Temporary decrease center back left level", "center_back_left_down", null),
    CFR_LEVEL_UP("Temporary increase ceiling front right level", "ceiling_front_right_up", null),
    CFR_LEVEL_DOWN("Temporary decrease ceiling front right level", "ceiling_front_right_down", null),
    CFL_LEVEL_UP("Temporary increase ceiling front left level", "ceiling_front_left_up", null),
    CFL_LEVEL_DOWN("Temporary decrease ceiling front left level", "ceiling_front_left_down", null),
    CRR_LEVEL_UP("Temporary increase ceiling rear right level", "ceiling_rear_right_up", null),
    CRR_LEVEL_DOWN("Temporary decrease ceiling rear right level", "ceiling_rear_right_down", null),
    CRL_LEVEL_UP("Temporary increase ceiling rear left level", "ceiling_rear_left_up", null),
    CRL_LEVEL_DOWN("Temporary decrease ceiling rear left level", "ceiling_rear_left_down", null),
    CINEMA_EQ_TOGGLE("Cinema EQ Toggle", PRIMARY_CMD, (byte) 0x4F),
    DISPLAY_TOGGLE("Front Display On/Off", PRIMARY_CMD, (byte) 0x52),
    PARTY_MODE_TOGGLE("Party Mode Toggle", PRIMARY_CMD, (byte) 0x6E),
    ZONE2_PARTY_MODE_TOGGLE("Zone 2 Party Mode Toggle", ZONE2_CMD, (byte) 0x6E),
    ZONE3_PARTY_MODE_TOGGLE("Zone 3 Party Mode Toggle", ZONE3_CMD, (byte) 0x6E),
    ZONE4_PARTY_MODE_TOGGLE("Zone 4 Party Mode Toggle", ZONE4_CMD, (byte) 0x6E),
    OUTPUT_RESOLUTION("Output Resolution", PRIMARY_CMD, (byte) 0x75),
    HDMI_AMP_MODE("HDMI Amp Mode", PRIMARY_CMD, (byte) 0x78),
    HDMI_TV_MODE("HDMI TV Mode", PRIMARY_CMD, (byte) 0x79),
    ROOM_EQ_TOGGLE("Temporary Room EQ Toggle", PRIMARY_CMD, (byte) 0x67),
    SPEAKER_SETTING_TOGGLE("Speaker Level Setting Toggle", PRIMARY_CMD, (byte) 0xA1),
    MODEL("Request the model number", "get_product_type", "model?"),
    VERSION("Request the main CPU software version", "get_product_version", "version?");

    public static final List<RotelCommand> DSP_CMDS_SET1 = List.of(DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE,
            PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN,
            DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE);
    public static final List<RotelCommand> DSP_CMDS_SET2 = List.of(STEREO_BYPASS_TOGGLE);

    public static final List<RotelCommand> SRC_CTRL_CMDS_SET1 = List.of(PLAY, STOP, PAUSE, TRACK_FWD, TRACK_BACK);
    public static final List<RotelCommand> SRC_CTRL_CMDS_SET2 = List.of(FAST_FWD, FAST_BACK);
    public static final List<RotelCommand> SRC_CTRL_CMDS_SET3 = List.of(RANDOM_TOGGLE, REPEAT_TOGGLE);
    public static final List<RotelCommand> SRC_CTRL_CMDS_SET4 = List.of(EJECT, TIME_TOGGLE);

    public static final List<RotelCommand> TUNER_CMDS_SET1 = List.of(TUNE_UP, TUNE_DOWN, MEMORY, BAND_TOGGLE, AM, FM,
            TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY,
            RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE);
    public static final List<RotelCommand> TUNER_CMDS_SET2 = List.of(TUNE_UP, TUNE_DOWN, PRESET_UP, PRESET_DOWN,
            FREQUENCY_UP, FREQUENCY_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT,
            PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, FM_MONO_TOGGLE);
    public static final List<RotelCommand> TUNER_CMDS_SET3 = List.of(TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA);
    public static final List<RotelCommand> ZONE2_TUNER_CMDS_SET1 = List.of(ZONE2_TUNE_UP, ZONE2_TUNE_DOWN,
            ZONE2_BAND_TOGGLE, ZONE2_AM, ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT,
            ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN, ZONE2_FM_MONO_TOGGLE);
    public static final List<RotelCommand> ZONE2_TUNER_CMDS_SET2 = List.of(ZONE2_TUNE_UP, ZONE2_TUNE_DOWN,
            ZONE2_PRESET_UP, ZONE2_PRESET_DOWN, ZONE2_FREQUENCY_UP, ZONE2_FREQUENCY_DOWN, ZONE2_BAND_TOGGLE, ZONE2_AM,
            ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT, ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN,
            ZONE2_FM_MONO_TOGGLE);
    public static final List<RotelCommand> ZONE234_TUNER_CMDS_SET1 = List.of(ZONE2_TUNE_UP, ZONE2_TUNE_DOWN,
            ZONE2_PRESET_UP, ZONE2_PRESET_DOWN, ZONE2_FREQUENCY_UP, ZONE2_FREQUENCY_DOWN, ZONE2_BAND_TOGGLE, ZONE2_AM,
            ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT, ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN,
            ZONE2_FM_MONO_TOGGLE, ZONE3_TUNE_UP, ZONE3_TUNE_DOWN, ZONE3_PRESET_UP, ZONE3_PRESET_DOWN,
            ZONE3_FREQUENCY_UP, ZONE3_FREQUENCY_DOWN, ZONE3_BAND_TOGGLE, ZONE3_AM, ZONE3_FM, ZONE3_TUNE_PRESET_TOGGLE,
            ZONE3_TUNING_MODE_SELECT, ZONE3_PRESET_MODE_SELECT, ZONE3_PRESET_SCAN, ZONE3_FM_MONO_TOGGLE, ZONE4_TUNE_UP,
            ZONE4_TUNE_DOWN, ZONE4_PRESET_UP, ZONE4_PRESET_DOWN, ZONE4_FREQUENCY_UP, ZONE4_FREQUENCY_DOWN,
            ZONE4_BAND_TOGGLE, ZONE4_AM, ZONE4_FM, ZONE4_TUNE_PRESET_TOGGLE, ZONE4_TUNING_MODE_SELECT,
            ZONE4_PRESET_MODE_SELECT, ZONE4_PRESET_SCAN, ZONE4_FM_MONO_TOGGLE);

    public static final List<RotelCommand> MENU_CTRL_CMDS = List.of(MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER);
    public static final List<RotelCommand> MENU2_CTRL_CMDS = List.of(MENU, UP, DOWN, LEFT, RIGHT, ENTER);
    public static final List<RotelCommand> MENU3_CTRL_CMDS = List.of(MENU, EXIT, UP_PRESSED, UP_RELEASED, DOWN_PRESSED,
            DOWN_RELEASED, LEFT_PRESSED, LEFT_RELEASED, RIGHT_PRESSED, RIGHT_RELEASED, ENTER);

    public static final List<RotelCommand> NUMERIC_KEY_CMDS = List.of(KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8,
            KEY9, KEY0);
    public static final List<RotelCommand> ZONE2_NUMERIC_KEY_CMDS = List.of(ZONE2_KEY1, ZONE2_KEY2, ZONE2_KEY3,
            ZONE2_KEY4, ZONE2_KEY5, ZONE2_KEY6, ZONE2_KEY7, ZONE2_KEY8, ZONE2_KEY9, ZONE2_KEY0);
    public static final List<RotelCommand> ZONE234_NUMERIC_KEY_CMDS = List.of(ZONE2_KEY1, ZONE2_KEY2, ZONE2_KEY3,
            ZONE2_KEY4, ZONE2_KEY5, ZONE2_KEY6, ZONE2_KEY7, ZONE2_KEY8, ZONE2_KEY9, ZONE2_KEY0, ZONE3_KEY1, ZONE3_KEY2,
            ZONE3_KEY3, ZONE3_KEY4, ZONE3_KEY5, ZONE3_KEY6, ZONE3_KEY7, ZONE3_KEY8, ZONE3_KEY9, ZONE3_KEY0, ZONE4_KEY1,
            ZONE4_KEY2, ZONE4_KEY3, ZONE4_KEY4, ZONE4_KEY5, ZONE4_KEY6, ZONE4_KEY7, ZONE4_KEY8, ZONE4_KEY9, ZONE4_KEY0);

    public static final List<RotelCommand> PCUSB_CLASS_CMDS = List.of(PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2);

    public static final List<RotelCommand> LEVEL_TRIM_CMDS_SET1 = List.of(SUB_LEVEL_UP, SUB_LEVEL_DOWN, C_LEVEL_UP,
            C_LEVEL_DOWN, SR_LEVEL_UP, SR_LEVEL_DOWN, SL_LEVEL_UP, SL_LEVEL_DOWN, CBR_LEVEL_UP, CBR_LEVEL_DOWN,
            CBL_LEVEL_UP, CBL_LEVEL_DOWN);
    public static final List<RotelCommand> LEVEL_TRIM_CMDS_SET2 = List.of(CFR_LEVEL_UP, CFR_LEVEL_DOWN, CFL_LEVEL_UP,
            CFL_LEVEL_DOWN, CRR_LEVEL_UP, CRR_LEVEL_DOWN, CRL_LEVEL_UP, CRL_LEVEL_DOWN);

    public static final List<RotelCommand> OTHER_CMDS_SET1 = List.of(RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT,
            DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE);
    public static final List<RotelCommand> OTHER_CMDS_SET2 = List.of(POWER_OFF_ALL_ZONES, PARTY_MODE_TOGGLE,
            ZONE2_PARTY_MODE_TOGGLE, ZONE3_PARTY_MODE_TOGGLE, ZONE4_PARTY_MODE_TOGGLE);
    public static final List<RotelCommand> OTHER_CMDS_SET3 = List.of(RECORD_FONCTION_SELECT, DYNAMIC_RANGE,
            DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE);
    public static final List<RotelCommand> OTHER_CMDS_SET4 = List.of(OUTPUT_RESOLUTION, HDMI_AMP_MODE, HDMI_TV_MODE);
    public static final List<RotelCommand> OTHER_CMDS_SET5 = List.of(POWER_MODE, POWER_MODE_QUICK, POWER_MODE_NORMAL,
            RESET_FACTORY);
    public static final List<RotelCommand> OTHER_CMDS_SET6 = List.of(POWER_MODE, RESET_FACTORY);
    public static final List<RotelCommand> OTHER_CMDS_SET7 = List.of(NEXT_MODE, RESET_FACTORY);
    public static final List<RotelCommand> OTHER_CMDS_SET8 = List.of(ROOM_EQ_TOGGLE, SPEAKER_SETTING_TOGGLE,
            RESET_FACTORY);
    public static final List<RotelCommand> OTHER_CMDS_SET9 = List.of(RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT,
            ZONE_TOGGLE);

    public static final byte PRIMARY_COMMAND = (byte) 0x10;

    private String label;
    private byte hexType;
    private byte hexKey;
    private @Nullable String asciiCommandV1;
    private @Nullable String asciiCommandV2;

    /**
     * Constructor when the textual commands are undefined
     *
     * @param label the command label
     * @param hexType the the command type (HEX protocol)
     * @param hexKey the the command key (HEX protocol)
     */
    private RotelCommand(String label, byte hexType, byte hexKey) {
        this(label, hexType, hexKey, null, null);
    }

    /**
     * Constructor when the HEX command is undefined
     *
     * @param label the command label
     * @param asciiCommandV1 the textual command (ASCII protocol V1)
     * @param asciiCommandV2 the textual command (ASCII protocol V2)
     */
    private RotelCommand(String label, @Nullable String asciiCommandV1, @Nullable String asciiCommandV2) {
        this(label, (byte) 0, (byte) 0, asciiCommandV1, asciiCommandV2);
    }

    /**
     * Constructor
     *
     * @param label the command label
     * @param hexType the the command type (HEX protocol)
     * @param hexKey the the command key (HEX protocol)
     * @param asciiCommandV1 the textual command (ASCII protocol V1)
     * @param asciiCommandV2 the textual command (ASCII protocol V2)
     */
    private RotelCommand(String label, byte hexType, byte hexKey, @Nullable String asciiCommandV1,
            @Nullable String asciiCommandV2) {
        this.label = label;
        this.hexType = hexType;
        this.hexKey = hexKey;
        this.asciiCommandV1 = asciiCommandV1;
        this.asciiCommandV2 = asciiCommandV2;
    }

    /**
     * Get the command label
     *
     * @return the command label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the command type (HEX protocol)
     *
     * @return the command type
     */
    public byte getHexType() {
        return hexType;
    }

    /**
     * Get the command key (HEX protocol)
     *
     * @return the command key
     */
    public byte getHexKey() {
        return hexKey;
    }

    /**
     * Get the textual command (ASCII protocol V1)
     *
     * @return the textual command
     */
    public @Nullable String getAsciiCommandV1() {
        return asciiCommandV1;
    }

    /**
     * Get the textual command (ASCII protocol V2)
     *
     * @return the textual command
     */
    public @Nullable String getAsciiCommandV2() {
        return asciiCommandV2;
    }

    /**
     * Indicate if the command is relative to a particular zone
     *
     * @param numZone the zone number
     *
     * @return true if the command is relative to the zone
     */
    public boolean isCommandForZone(int numZone) {
        String prefix = String.format("ZONE%d", numZone);
        return name().startsWith(prefix);
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Get the command associated to a textual command
     *
     * @param text the textual command used to identify the command
     *
     * @return the command associated to the searched textual command
     *
     * @throws RotelException - If no command is associated to the searched textual command
     */
    public static RotelCommand getFromAsciiCommand(String text) throws RotelException {
        for (RotelCommand value : RotelCommand.values()) {
            if (text.equals(value.getAsciiCommandV1()) || text.equals(value.getAsciiCommandV2())) {
                return value;
            }
        }
        throw new RotelException("Invalid textual command: " + text);
    }

    /**
     * Get the command from its name
     *
     * @param name the command name used to identify the command
     *
     * @return the command associated to the searched name
     *
     * @throws RotelException - If no command is associated to the searched name
     */
    public static RotelCommand getFromName(String name) throws RotelException {
        for (RotelCommand value : RotelCommand.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        throw new RotelException("Invalid command: " + name);
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2) {
        return Stream.of(list1, list2).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3) {
        return Stream.of(list1, list2, list3).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3, List<RotelCommand> list4) {
        return Stream.of(list1, list2, list3, list4).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3, List<RotelCommand> list4, List<RotelCommand> list5) {
        return Stream.of(list1, list2, list3, list4, list5).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3, List<RotelCommand> list4, List<RotelCommand> list5, List<RotelCommand> list6) {
        return Stream.of(list1, list2, list3, list4, list5, list6).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3, List<RotelCommand> list4, List<RotelCommand> list5, List<RotelCommand> list6,
            List<RotelCommand> list7) {
        return Stream.of(list1, list2, list3, list4, list5, list6, list7).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3, List<RotelCommand> list4, List<RotelCommand> list5, List<RotelCommand> list6,
            List<RotelCommand> list7, List<RotelCommand> list8) {
        return Stream.of(list1, list2, list3, list4, list5, list6, list7, list8).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3, List<RotelCommand> list4, List<RotelCommand> list5, List<RotelCommand> list6,
            List<RotelCommand> list7, List<RotelCommand> list8, List<RotelCommand> list9) {
        return Stream.of(list1, list2, list3, list4, list5, list6, list7, list8, list9).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3, List<RotelCommand> list4, List<RotelCommand> list5, List<RotelCommand> list6,
            List<RotelCommand> list7, List<RotelCommand> list8, List<RotelCommand> list9, List<RotelCommand> list10) {
        return Stream.of(list1, list2, list3, list4, list5, list6, list7, list8, list9, list10)
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3, List<RotelCommand> list4, List<RotelCommand> list5, List<RotelCommand> list6,
            List<RotelCommand> list7, List<RotelCommand> list8, List<RotelCommand> list9, List<RotelCommand> list10,
            List<RotelCommand> list11) {
        return Stream.of(list1, list2, list3, list4, list5, list6, list7, list8, list9, list10, list11)
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static List<RotelCommand> concatenate(List<RotelCommand> list1, List<RotelCommand> list2,
            List<RotelCommand> list3, List<RotelCommand> list4, List<RotelCommand> list5, List<RotelCommand> list6,
            List<RotelCommand> list7, List<RotelCommand> list8, List<RotelCommand> list9, List<RotelCommand> list10,
            List<RotelCommand> list11, List<RotelCommand> list12) {
        return Stream.of(list1, list2, list3, list4, list5, list6, list7, list8, list9, list10, list11, list12)
                .flatMap(Collection::stream).collect(Collectors.toList());
    }
}
