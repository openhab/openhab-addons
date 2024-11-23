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

import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.CYCLE;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.MENU_CONTROL;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.MODE;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.NONE;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.NUMBER;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.SET;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.SOURCE_MAIN_ZONE;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.SOURCE_USER;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.SOURCE_ZONE2;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.SPEAKER_PRESET;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.TOGGLE;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.UP_DOWN_HALF;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.UP_DOWN_SINGLE;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.DIMENSIONLESS_DECIBEL;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.DIMENSIONLESS_PERCENT;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.NOT_IMPLEMENTED;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.ON_OFF;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.STRING;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.UNKNOWN;

import java.util.EnumMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Emotiva command name with corresponding command type and UoM data type.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum EmotivaControlCommands {
    none("", NONE, UNKNOWN),
    standby("", TOGGLE, ON_OFF),
    source_tuner("Tuner", SOURCE_USER, STRING),
    source_1("Input 1", SOURCE_USER, STRING),
    source_2("Input 2", SOURCE_USER, STRING),
    source_3("Input 3", SOURCE_USER, STRING),
    source_4("Input 4", SOURCE_USER, STRING),
    source_5("Input 5", SOURCE_USER, STRING),
    source_6("Input 6", SOURCE_USER, STRING),
    source_7("Input 7", SOURCE_USER, STRING),
    source_8("Input 8", SOURCE_USER, STRING),
    menu("", SET, STRING),
    menu_control("", MENU_CONTROL, STRING), // Not an Emotiva command, just a placeholder
    up("", SET, STRING),
    down("", SET, STRING),
    left("", SET, STRING),
    right("", SET, STRING),
    enter("", SET, STRING),
    dim("", CYCLE, DIMENSIONLESS_PERCENT),
    mode("", MODE, STRING),
    info("", SET, UNKNOWN),
    mute("", SET, ON_OFF),
    mute_off("", SET, ON_OFF),
    mute_on("", SET, ON_OFF),
    music("", SET, STRING),
    movie("", SET, STRING),
    center("", SET, DIMENSIONLESS_DECIBEL),
    subwoofer("", SET, DIMENSIONLESS_DECIBEL),
    surround("", SET, DIMENSIONLESS_DECIBEL),
    back("", SET, DIMENSIONLESS_DECIBEL),
    input("", NONE, STRING),
    input_up("", SET, STRING),
    input_down("", SET, STRING),
    power("", TOGGLE, ON_OFF), // Not an Emotiva command, just a placeholder
    power_on("", SET, ON_OFF),
    power_off("", SET, ON_OFF),
    volume("", SET, DIMENSIONLESS_DECIBEL),
    set_volume("", NUMBER, DIMENSIONLESS_DECIBEL),
    loudness_on("", SET, ON_OFF),
    loudness_off("", SET, ON_OFF),
    loudness("", TOGGLE, ON_OFF),
    speaker_preset("", SPEAKER_PRESET, STRING),
    mode_up("", SET, STRING),
    mode_down("", SET, STRING),
    bass("", UP_DOWN_HALF, DIMENSIONLESS_DECIBEL), // Not an Emotiva command, just a placeholder
    bass_up("", UP_DOWN_HALF, DIMENSIONLESS_DECIBEL),
    bass_down("", UP_DOWN_HALF, DIMENSIONLESS_DECIBEL),
    treble("", UP_DOWN_HALF, DIMENSIONLESS_DECIBEL), // Not an Emotiva command, just a placeholder
    treble_up("", UP_DOWN_HALF, DIMENSIONLESS_DECIBEL),
    treble_down("", UP_DOWN_HALF, DIMENSIONLESS_DECIBEL),
    zone2_power("", TOGGLE, ON_OFF),
    zone2_power_off("", SET, ON_OFF),
    zone2_power_on("", SET, ON_OFF),
    zone2_volume("", SET, DIMENSIONLESS_DECIBEL),
    zone2_set_volume("", NUMBER, STRING),
    zone2_input("", NONE, STRING),
    zone1_band("", TOGGLE, STRING),
    band_am("", SET, STRING),
    band_fm("", SET, STRING),
    zone2_mute("", TOGGLE, ON_OFF),
    zone2_mute_off("", SET, ON_OFF),
    zone2_mute_on("", SET, ON_OFF),
    zone2_band("", SET, NOT_IMPLEMENTED),
    frequency("", UP_DOWN_SINGLE, ON_OFF),
    seek("", UP_DOWN_SINGLE, ON_OFF),
    channel("", UP_DOWN_SINGLE, ON_OFF),
    stereo("", SET, STRING),
    direct("", SET, STRING),
    dolby("", SET, STRING),
    dts("", SET, STRING),
    all_stereo("", SET, STRING),
    auto("", SET, STRING),
    reference_stereo("", SET, STRING),
    surround_mode("", SET, STRING),
    preset1("Preset 1", SET, STRING),
    preset2("Preset 2", SET, STRING),
    dirac("Dirac", SET, STRING),
    hdmi1("HDMI 1", SOURCE_MAIN_ZONE, STRING),
    hdmi2("HDMI 2", SOURCE_MAIN_ZONE, STRING),
    hdmi3("HDMI 3", SOURCE_MAIN_ZONE, STRING),
    hdmi4("HDMI 4", SOURCE_MAIN_ZONE, STRING),
    hdmi5("HDMI 5", SOURCE_MAIN_ZONE, STRING),
    hdmi6("HDMI 6", SOURCE_MAIN_ZONE, STRING),
    hdmi7("HDMI 7", SOURCE_MAIN_ZONE, STRING),
    hdmi8("HDMI 8", SOURCE_MAIN_ZONE, STRING),
    analog1("Analog 1", SOURCE_MAIN_ZONE, STRING),
    analog2("Analog 2", SOURCE_MAIN_ZONE, STRING),
    analog3("Analog 3", SOURCE_MAIN_ZONE, STRING),
    analog4("Analog 4", SOURCE_MAIN_ZONE, STRING),
    analog5("Analog 5", SOURCE_MAIN_ZONE, STRING),
    analog71("Analog 7.1", SOURCE_MAIN_ZONE, STRING),
    ARC("Audio Return Channel", SOURCE_MAIN_ZONE, STRING),
    coax1("Coax 1", SOURCE_MAIN_ZONE, STRING),
    coax2("Coax 2", SOURCE_MAIN_ZONE, STRING),
    coax3("Coax 3", SOURCE_MAIN_ZONE, STRING),
    coax4("Coax 4", SOURCE_MAIN_ZONE, STRING),
    front_in("Front In", SOURCE_MAIN_ZONE, STRING),
    optical1("Optical 1", SOURCE_MAIN_ZONE, STRING),
    optical2("Optical 2", SOURCE_MAIN_ZONE, STRING),
    optical3("Optical 3", SOURCE_MAIN_ZONE, STRING),
    optical4("Optical 4", SOURCE_MAIN_ZONE, STRING),
    tuner("Tuner 1", SOURCE_MAIN_ZONE, STRING),
    usb_stream("USB Stream", SOURCE_MAIN_ZONE, STRING),
    center_trim_set("", NUMBER, DIMENSIONLESS_DECIBEL),
    subwoofer_trim_set("", NUMBER, DIMENSIONLESS_DECIBEL),
    surround_trim_set("", NUMBER, DIMENSIONLESS_DECIBEL),
    back_trim_set("", NUMBER, DIMENSIONLESS_DECIBEL),
    width_trim_set("", NUMBER, DIMENSIONLESS_DECIBEL),
    height_trim_set("", NUMBER, DIMENSIONLESS_DECIBEL),
    zone2_analog1("Analog 1", SOURCE_ZONE2, STRING),
    zone2_analog2("Analog 2", SOURCE_ZONE2, STRING),
    zone2_analog3("Analog 3", SOURCE_ZONE2, STRING),
    zone2_analog4("Analog 4", SOURCE_ZONE2, STRING),
    zone2_analog5("Analog 5", SOURCE_ZONE2, STRING),
    zone2_analog71("Analog 7.1", SOURCE_ZONE2, STRING),
    zone2_analog8("Analog 8", SOURCE_ZONE2, STRING),
    zone2_ARC("Audio Return Channel", SOURCE_ZONE2, STRING),
    zone2_coax1("Coax 1", SOURCE_ZONE2, STRING),
    zone2_coax2("Coax 2", SOURCE_ZONE2, STRING),
    zone2_coax3("Coax 3", SOURCE_ZONE2, STRING),
    zone2_coax4("Coax 4", SOURCE_ZONE2, STRING),
    zone2_ethernet("Ethernet", SOURCE_ZONE2, STRING),
    zone2_follow_main("Follow Main", SOURCE_ZONE2, STRING),
    zone2_front_in("Front In", SOURCE_ZONE2, STRING),
    zone2_optical1("Optical 1", SOURCE_ZONE2, STRING),
    zone2_optical2("Optical 2", SOURCE_ZONE2, STRING),
    zone2_optical3("Optical 3", SOURCE_ZONE2, STRING),
    zone2_optical4("Optical 4", SOURCE_ZONE2, STRING),
    channel_1("Channel 1", SET, STRING),
    channel_2("Channel 2", SET, STRING),
    channel_3("Channel 3", SET, STRING),
    channel_4("Channel 4", SET, STRING),
    channel_5("Channel 5", SET, STRING),
    channel_6("Channel 6", SET, STRING),
    channel_7("Channel 7", SET, STRING),
    channel_8("Channel 8", SET, STRING),
    channel_9("Channel 9", SET, STRING),
    channel_10("Channel 10", SET, STRING),
    channel_11("Channel 11", SET, STRING),
    channel_12("Channel 12", SET, STRING),
    channel_13("Channel 13", SET, STRING),
    channel_14("Channel 14", SET, STRING),
    channel_15("Channel 15", SET, STRING),
    channel_16("Channel 16", SET, STRING),
    channel_17("Channel 17", SET, STRING),
    channel_18("Channel 18", SET, STRING),
    channel_19("Channel 19", SET, STRING),
    channel_20("Channel 20", SET, STRING);

    private final String label;
    private final EmotivaCommandType commandType;
    private final EmotivaDataType dataType;

    EmotivaControlCommands(String label, EmotivaCommandType commandType, EmotivaDataType dataType) {
        this.label = label;
        this.commandType = commandType;
        this.dataType = dataType;
    }

    public static EmotivaControlCommands matchToInput(String inputName) {
        for (EmotivaControlCommands value : values()) {
            if (inputName.toLowerCase().equals(value.name())) {
                return value;
            }
        }
        if (inputName.startsWith("input_")) {
            return valueOf(inputName.replace("input_", "source_"));
        }
        return none;
    }

    public String getLabel() {
        return label;
    }

    public EmotivaCommandType getCommandType() {
        return commandType;
    }

    public EmotivaDataType getDataType() {
        return dataType;
    }

    public static EnumMap<EmotivaControlCommands, String> getCommandsFromType(EmotivaCommandType filter) {
        EnumMap<EmotivaControlCommands, String> commands = new EnumMap<>(EmotivaControlCommands.class);
        for (EmotivaControlCommands value : values()) {
            if (value.getCommandType().equals(filter)) {
                var sb = new StringBuilder(value.name());
                sb.setCharAt(0, Character.toUpperCase(value.name().charAt(0)));
                commands.put(value, sb.toString());
            }
        }
        return commands;
    }
}
