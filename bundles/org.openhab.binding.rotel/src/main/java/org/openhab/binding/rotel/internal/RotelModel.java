/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal;

import static org.openhab.binding.rotel.internal.RotelBindingConstants.MAX_NUMBER_OF_ZONES;
import static org.openhab.binding.rotel.internal.communication.RotelCommand.*;
import static org.openhab.binding.rotel.internal.protocol.ascii.RotelAbstractAsciiProtocolHandler.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.communication.RotelCommand;
import org.openhab.binding.rotel.internal.communication.RotelDsp;
import org.openhab.binding.rotel.internal.communication.RotelFlagsMapping;
import org.openhab.binding.rotel.internal.communication.RotelSource;
import org.openhab.binding.rotel.internal.protocol.RotelProtocol;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateOption;

/**
 * Represents the different supported models
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum RotelModel {

    RSP1066("RSP-1066", 19200, 3, 1, false, 90, false, 12, false, ZONE_SELECT, 1,
            concatenate(DSP_CMDS_SET1, MENU2_CTRL_CMDS, OTHER_CMDS_SET1), (byte) 0xC2, 13, 8, true,
            RotelFlagsMapping.MAPPING1),
    RSP1068("RSP-1068", 19200, 1, 1, true, 96, true, 6, false, RECORD_FONCTION_SELECT, 2,
            concatenate(DSP_CMDS_SET1, MENU2_CTRL_CMDS, OTHER_CMDS_SET1), (byte) 0xA1, 42, 5, true,
            RotelFlagsMapping.MAPPING2),
    RSP1069("RSP-1069", 38400, 1, 3, true, 96, true, 6, false, RECORD_FONCTION_SELECT, 2,
            concatenate(DSP_CMDS_SET1, MENU2_CTRL_CMDS, OTHER_CMDS_SET1, OTHER_CMDS_SET2), (byte) 0xA2, 42, 5, true,
            RotelFlagsMapping.MAPPING5),
    RSP1098("RSP-1098", 19200, 1, 1, true, 96, true, 6, false, ZONE_SELECT, 2,
            concatenate(DSP_CMDS_SET1, MENU2_CTRL_CMDS, OTHER_CMDS_SET1, List.of(REMOTE_VOLUME_UP, REMOTE_VOLUME_DOWN)),
            (byte) 0xA0, 13, 8, true, RotelFlagsMapping.MAPPING1),
    RSP1570("RSP-1570", 115200, 1, 3, true, 96, true, 6, false, RECORD_FONCTION_SELECT, 3,
            concatenate(DSP_CMDS_SET2, DSP_CMDS_SET1, MENU2_CTRL_CMDS, OTHER_CMDS_SET1, OTHER_CMDS_SET2,
                    List.of(RESET_FACTORY)),
            (byte) 0xA3, 42, 5, true, RotelFlagsMapping.MAPPING5),
    RSP1572("RSP-1572", 115200, 2, 3, true, 96, true, null, false, RECORD_FONCTION_SELECT, 4,
            concatenate(DSP_CMDS_SET2, DSP_CMDS_SET1, NUMERIC_KEY_CMDS, MENU3_CTRL_CMDS, OTHER_CMDS_SET1,
                    OTHER_CMDS_SET4),
            (byte) 0xA5, 42, 5, true, RotelFlagsMapping.MAPPING5),
    RSX1055("RSX-1055", 19200, 3, 1, false, 90, false, 12, false, ZONE_SELECT, 1,
            concatenate(DSP_CMDS_SET1, TUNER_CMDS_SET1, NUMERIC_KEY_CMDS, MENU2_CTRL_CMDS, OTHER_CMDS_SET1),
            (byte) 0xC3, 13, 8, true, RotelFlagsMapping.MAPPING1),
    RSX1056("RSX-1056", 19200, 1, 1, true, 96, true, 12, false, ZONE_SELECT, 2,
            concatenate(DSP_CMDS_SET1, TUNER_CMDS_SET1, ZONE2_TUNER_CMDS_SET1, NUMERIC_KEY_CMDS, ZONE2_NUMERIC_KEY_CMDS,
                    MENU2_CTRL_CMDS, OTHER_CMDS_SET1),
            (byte) 0xC5, 13, 8, true, RotelFlagsMapping.MAPPING1),
    RSX1057("RSX-1057", 19200, 1, 1, true, 96, true, 6, false, RECORD_FONCTION_SELECT, 2,
            concatenate(DSP_CMDS_SET1, TUNER_CMDS_SET2, ZONE2_TUNER_CMDS_SET2, NUMERIC_KEY_CMDS, ZONE2_NUMERIC_KEY_CMDS,
                    MENU2_CTRL_CMDS, OTHER_CMDS_SET1),
            (byte) 0xC7, 13, 8, true, RotelFlagsMapping.MAPPING1),
    RSX1058("RSX-1058", 38400, 1, 3, true, 96, true, 6, false, RECORD_FONCTION_SELECT, 2,
            concatenate(DSP_CMDS_SET1, TUNER_CMDS_SET2, ZONE234_TUNER_CMDS_SET1, NUMERIC_KEY_CMDS,
                    ZONE234_NUMERIC_KEY_CMDS, MENU2_CTRL_CMDS, OTHER_CMDS_SET1, OTHER_CMDS_SET2),
            (byte) 0xC8, 13, 8, true, RotelFlagsMapping.MAPPING4),
    RSX1065("RSX-1065", 19200, 3, 1, false, 96, false, 12, false, ZONE_SELECT, 1,
            concatenate(DSP_CMDS_SET1, TUNER_CMDS_SET1, NUMERIC_KEY_CMDS, MENU2_CTRL_CMDS, OTHER_CMDS_SET3),
            (byte) 0xC1, 42, 5, true, RotelFlagsMapping.MAPPING2),
    RSX1067("RSX-1067", 19200, 1, 1, true, 96, true, 6, false, RECORD_FONCTION_SELECT, 2,
            concatenate(DSP_CMDS_SET1, TUNER_CMDS_SET1, ZONE2_TUNER_CMDS_SET1, NUMERIC_KEY_CMDS, ZONE2_NUMERIC_KEY_CMDS,
                    MENU2_CTRL_CMDS, OTHER_CMDS_SET1),
            (byte) 0xC4, 42, 5, true, RotelFlagsMapping.MAPPING2),
    RSX1550("RSX-1550", 115200, 1, 3, true, 96, true, 6, false, RECORD_FONCTION_SELECT, 3,
            concatenate(DSP_CMDS_SET1, TUNER_CMDS_SET2, ZONE234_TUNER_CMDS_SET1, NUMERIC_KEY_CMDS,
                    ZONE234_NUMERIC_KEY_CMDS, MENU2_CTRL_CMDS, OTHER_CMDS_SET1, OTHER_CMDS_SET2,
                    List.of(RESET_FACTORY)),
            (byte) 0xC9, 13, 8, true, RotelFlagsMapping.MAPPING3),
    RSX1560("RSX-1560", 115200, 1, 3, true, 96, true, 6, false, RECORD_FONCTION_SELECT, 3,
            concatenate(DSP_CMDS_SET1, TUNER_CMDS_SET2, ZONE234_TUNER_CMDS_SET1, NUMERIC_KEY_CMDS,
                    ZONE234_NUMERIC_KEY_CMDS, MENU2_CTRL_CMDS, OTHER_CMDS_SET1, OTHER_CMDS_SET2,
                    List.of(RESET_FACTORY)),
            (byte) 0xCA, 42, 5, true, RotelFlagsMapping.MAPPING5),
    RSX1562("RSX-1562", 115200, 2, 3, true, 96, true, null, false, RECORD_FONCTION_SELECT, 4,
            concatenate(DSP_CMDS_SET2, DSP_CMDS_SET1, TUNER_CMDS_SET2, ZONE234_TUNER_CMDS_SET1, NUMERIC_KEY_CMDS,
                    ZONE234_NUMERIC_KEY_CMDS, MENU3_CTRL_CMDS, OTHER_CMDS_SET1, OTHER_CMDS_SET4),
            (byte) 0xCC, 42, 5, true, RotelFlagsMapping.MAPPING5),
    A11("A11", 115200, 4, 96, true, 10, 15, false, -1, false, true, true, 6, 0, SRC_CTRL_CMDS_SET1,
            NO_SPECIAL_CHARACTERS),
    A12("A12", 115200, 5, 96, true, 10, 15, false, -1, true, true, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET1, List.of(PCUSB_CLASS)), NO_SPECIAL_CHARACTERS),
    A14("A14", 115200, 5, 96, true, 10, 15, false, -1, true, true, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET1, List.of(PCUSB_CLASS)), NO_SPECIAL_CHARACTERS),
    CD11("CD11", 57600, 0, null, false, null, true, -1, false, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET2, SRC_CTRL_CMDS_SET4, NUMERIC_KEY_CMDS), NO_SPECIAL_CHARACTERS),
    CD14("CD14", 57600, 0, null, false, null, true, -1, false, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET2, SRC_CTRL_CMDS_SET4, NUMERIC_KEY_CMDS), NO_SPECIAL_CHARACTERS),
    RA11("RA-11", 115200, 6, 96, true, 10, 15, true, -1, true, false, false, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET2, SRC_CTRL_CMDS_SET3, MENU_CTRL_CMDS, NUMERIC_KEY_CMDS), SPECIAL_CHARACTERS),
    RA12("RA-12", 115200, 6, 96, true, 10, 15, true, -1, true, false, false, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET2, SRC_CTRL_CMDS_SET3, MENU_CTRL_CMDS, NUMERIC_KEY_CMDS), SPECIAL_CHARACTERS),
    RA1570("RA-1570", 115200, 7, 96, true, 10, 15, true, -1, true, true, false, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET2, SRC_CTRL_CMDS_SET3, MENU_CTRL_CMDS, NUMERIC_KEY_CMDS, PCUSB_CLASS_CMDS),
            SPECIAL_CHARACTERS),
    RA1572("RA-1572", 115200, 8, 96, true, 10, 15, false, -1, true, true, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET1, MENU_CTRL_CMDS, PCUSB_CLASS_CMDS, OTHER_CMDS_SET6), SPECIAL_CHARACTERS),
    RA1592_V1("RA-1592", 115200, 9, 96, true, 10, 15, false, -1, true, true, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET1, SRC_CTRL_CMDS_SET2, MENU_CTRL_CMDS, NUMERIC_KEY_CMDS, PCUSB_CLASS_CMDS,
                    OTHER_CMDS_SET5),
            SPECIAL_CHARACTERS),
    RA1592_V2("RA-1592", 115200, 9, 96, true, 10, 15, false, -1, true, true, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET1, PCUSB_CLASS_CMDS), SPECIAL_CHARACTERS),
    RAP1580("RAP-1580", 115200, 11, 96, true, null, false, 5, false, false, -10, 10,
            concatenate(SRC_CTRL_CMDS_SET1, MENU_CTRL_CMDS, LEVEL_TRIM_CMDS_SET1, LEVEL_TRIM_CMDS_SET2,
                    OTHER_CMDS_SET7),
            NO_SPECIAL_CHARACTERS),
    RC1570("RC-1570", 115200, 7, 96, true, 10, 15, true, -1, true, false, false, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET2, SRC_CTRL_CMDS_SET3, MENU_CTRL_CMDS, NUMERIC_KEY_CMDS, PCUSB_CLASS_CMDS),
            SPECIAL_CHARACTERS),
    RC1572("RC-1572", 115200, 8, 96, true, 10, 15, false, -1, true, false, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET1, MENU_CTRL_CMDS, PCUSB_CLASS_CMDS, OTHER_CMDS_SET6), SPECIAL_CHARACTERS),
    RC1590_V1("RC-1590", 115200, 9, 96, true, 10, 15, false, -1, true, false, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET1, SRC_CTRL_CMDS_SET2, MENU_CTRL_CMDS, NUMERIC_KEY_CMDS, PCUSB_CLASS_CMDS,
                    OTHER_CMDS_SET5),
            SPECIAL_CHARACTERS),
    RC1590_V2("RC-1590", 115200, 9, 96, true, 10, 15, false, -1, true, false, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET1, PCUSB_CLASS_CMDS), SPECIAL_CHARACTERS),
    RCD1570("RCD-1570", 115200, 0, null, false, null, true, -1, false, true, 6, 0, List.of(), SPECIAL_CHARACTERS),
    RCD1572("RCD-1572", 57600, 0, null, false, null, true, -1, false, true, 6, 0,
            concatenate(SRC_CTRL_CMDS_SET2, SRC_CTRL_CMDS_SET4, List.of(PROGRAM), MENU_CTRL_CMDS, NUMERIC_KEY_CMDS),
            SPECIAL_CHARACTERS_RCD1572),
    RCX1500("RCX-1500", 115200, 17, 86, true, null, true, -1, false, false, null, null, List.of(), SPECIAL_CHARACTERS),
    RDD1580("RDD-1580", 115200, 15, null, false, null, true, -1, true, false, null, null,
            concatenate(SRC_CTRL_CMDS_SET3, PCUSB_CLASS_CMDS), NO_SPECIAL_CHARACTERS),
    RDG1520("RDG-1520", 115200, 16, null, false, null, true, -1, false, false, null, null, List.of(),
            SPECIAL_CHARACTERS),
    RSP1576("RSP-1576", 115200, 10, 96, true, null, false, 5, false, false, -10, 10,
            concatenate(SRC_CTRL_CMDS_SET1, MENU_CTRL_CMDS, LEVEL_TRIM_CMDS_SET1, LEVEL_TRIM_CMDS_SET2,
                    OTHER_CMDS_SET7),
            NO_SPECIAL_CHARACTERS),
    RSP1582("RSP-1582", 115200, 11, 96, true, null, false, 6, false, false, -10, 10,
            concatenate(SRC_CTRL_CMDS_SET1, MENU_CTRL_CMDS, LEVEL_TRIM_CMDS_SET1, OTHER_CMDS_SET7),
            NO_SPECIAL_CHARACTERS),
    RT11("RT-11", 115200, 12, null, false, null, false, -1, false, true, 6, 0, List.of(), NO_SPECIAL_CHARACTERS),
    RT1570("RT-1570", 115200, 14, null, false, null, false, -1, false, true, 6, 0, List.of(), NO_SPECIAL_CHARACTERS),
    T11("T11", 115200, 12, null, false, null, false, -1, false, true, 6, 0, List.of(), NO_SPECIAL_CHARACTERS),
    T14("T14", 115200, 13, null, false, null, false, -1, false, true, 6, 0, List.of(), NO_SPECIAL_CHARACTERS),
    C8("C8", 115200, POWER, 21, 3, true, false, 96, true, 10, false, 10, false, null, -1, true, false, true, 4, 0,
            List.of(), (byte) 0, 0, 0, false, RotelFlagsMapping.NO_MAPPING, NO_SPECIAL_CHARACTERS),
    M8("M8", 115200, 0, null, false, null, false, -1, false, true, 4, 0, List.of(), NO_SPECIAL_CHARACTERS),
    P5("P5", 115200, 20, 96, true, 10, 10, false, -1, true, false, true, 4, 0, SRC_CTRL_CMDS_SET1,
            NO_SPECIAL_CHARACTERS),
    S5("S5", 115200, 0, null, false, null, false, -1, false, true, 4, 0, List.of(), NO_SPECIAL_CHARACTERS),
    X3("X3", 115200, 18, 96, true, 10, 10, false, -1, true, false, true, 4, 0, SRC_CTRL_CMDS_SET1,
            NO_SPECIAL_CHARACTERS),
    X5("X5", 115200, 19, 96, true, 10, 10, false, -1, true, false, true, 4, 0, SRC_CTRL_CMDS_SET1,
            NO_SPECIAL_CHARACTERS);

    private String name;
    private int baudRate;
    private RotelCommand powerStateCmd;
    private int sourceCategory;
    private int nbAdditionalZones;
    private boolean additionalCommands;
    private boolean powerControlPerZone;
    private @Nullable Integer volumeMax;
    private boolean directVolume;
    private @Nullable Integer toneLevelMax;
    private boolean getBypassStatusAvailable;
    private boolean playControl;
    private @Nullable RotelCommand zoneSelectCmd;
    private int dspCategory;
    private boolean getFrequencyAvailable;
    private boolean getDimmerLevelAvailable;
    private @Nullable Integer diummerLevelMin;
    private @Nullable Integer diummerLevelMax;
    private List<RotelCommand> otherCommands;
    private byte deviceId;
    private int respNbChars;
    private int respNbFlags;
    private boolean charsBeforeFlags;
    private RotelFlagsMapping flagsMapping;
    private byte[][] specialCharacters;
    private @Nullable Integer balanceLevelMax;
    private boolean getSpeakerGroupsAvailable;

    /**
     * Constructor
     *
     * @param name the model name
     * @param baudRate the baud rate to be used for the RS232 communication
     * @param sourceCategory the category from {@link RotelSource}
     * @param nbAdditionalZones the number of additional zones
     * @param additionalCommands true if other than primary commands are available
     * @param volumeMax the maximum volume or null if no volume management is available
     * @param directVolume true if a command to set the volume with a value is available
     * @param toneLevelMax the maximum tone level or null if no bass/treble management is available
     * @param playControl true if control of source playback is available
     * @param zoneSelectCmd the command to be used to select a zone
     * @param dspCategory the category from {@link RotelDsp}
     * @param otherCommands the list of other available commands to expose
     * @param deviceId the device id (value to be used in the messages)
     * @param respNbChars the number of bytes for the characters in the standard response
     * @param respNbFlags the number of bytes for the flags in the standard response
     * @param charsBeforeFlags true if the characters are before the flags in the standard response message
     * @param flagsMapping the mapping of the flags in the feedback message
     */
    private RotelModel(String name, int baudRate, int sourceCategory, int nbAdditionalZones, boolean additionalCommands,
            @Nullable Integer volumeMax, boolean directVolume, @Nullable Integer toneLevelMax, boolean playControl,
            @Nullable RotelCommand zoneSelectCmd, int dspCategory, List<RotelCommand> otherCommands, byte deviceId,
            int respNbChars, int respNbFlags, boolean charsBeforeFlags, RotelFlagsMapping flagsMapping) {
        this(name, baudRate, DISPLAY_REFRESH, sourceCategory, nbAdditionalZones, additionalCommands, true, volumeMax,
                directVolume, toneLevelMax, false, null, playControl, zoneSelectCmd, dspCategory, false, false, false,
                null, null, otherCommands, deviceId, respNbChars, respNbFlags, charsBeforeFlags, flagsMapping,
                NO_SPECIAL_CHARACTERS);
    }

    /**
     * Constructor
     *
     * @param name the model name
     * @param baudRate the baud rate to be used for the RS232 communication
     * @param sourceCategory the category from {@link RotelSource}
     * @param volumeMax the maximum volume or null if no volume management is available
     * @param directVolume true if a command to set the volume with a value is available
     * @param toneLevelMax the maximum tone level or null if no bass/treble management is available
     * @param playControl true if control of source playback is available
     * @param dspCategory the category from {@link RotelDsp}
     * @param getFrequencyAvailable true if the command to get the frequency for digital source input is available
     * @param getDimmerLevelAvailable true if the command to get the front display dimmer level is available
     * @param diummerLevelMin the minimum front display dimmer level or null if dimmer control is unavailable
     * @param diummerLevelMax the maximum front display dimmer level or null if dimmer control is unavailable
     * @param otherCommands the list of other available commands to expose
     * @param specialCharacters the table of special characters that can be found in the standard response message
     */
    private RotelModel(String name, int baudRate, int sourceCategory, @Nullable Integer volumeMax, boolean directVolume,
            @Nullable Integer toneLevelMax, boolean playControl, int dspCategory, boolean getFrequencyAvailable,
            boolean getDimmerLevelAvailable, @Nullable Integer diummerLevelMin, @Nullable Integer diummerLevelMax,
            List<RotelCommand> otherCommands, byte[][] specialCharacters) {
        this(name, baudRate, POWER, sourceCategory, 0, false, false, volumeMax, directVolume, toneLevelMax,
                toneLevelMax != null, null, playControl, null, dspCategory, getFrequencyAvailable, false,
                getDimmerLevelAvailable, diummerLevelMin, diummerLevelMax, otherCommands, (byte) 0, 0, 0, false,
                RotelFlagsMapping.NO_MAPPING, specialCharacters);
    }

    /**
     * Constructor
     *
     * @param name the model name
     * @param baudRate the baud rate to be used for the RS232 communication
     * @param sourceCategory the category from {@link RotelSource}
     * @param volumeMax the maximum volume or null if no volume management is available
     * @param directVolume true if a command to set the volume with a value is available
     * @param toneLevelMax the maximum tone level or null if no bass/treble management is available
     * @param balanceLevelMax the maximum balance level or null if no balance management is available
     * @param playControl true if control of source playback is available
     * @param dspCategory the category from {@link RotelDsp}
     * @param getFrequencyAvailable true if the command to get the frequency for digital source input is available
     * @param getSpeakerGroupsAvailable true if the command to switch speaker groups is available
     * @param getDimmerLevelAvailable true if the command to get the front display dimmer level is available
     * @param diummerLevelMin the minimum front display dimmer level or null if dimmer control is unavailable
     * @param diummerLevelMax the maximum front display dimmer level or null if dimmer control is unavailable
     * @param otherCommands the list of other available commands to expose
     * @param specialCharacters the table of special characters that can be found in the standard response message
     */
    private RotelModel(String name, int baudRate, int sourceCategory, @Nullable Integer volumeMax, boolean directVolume,
            @Nullable Integer toneLevelMax, @Nullable Integer balanceLevelMax, boolean playControl, int dspCategory,
            boolean getFrequencyAvailable, boolean getSpeakerGroupsAvailable, boolean getDimmerLevelAvailable,
            @Nullable Integer diummerLevelMin, @Nullable Integer diummerLevelMax, List<RotelCommand> otherCommands,
            byte[][] specialCharacters) {
        this(name, baudRate, POWER, sourceCategory, 0, false, false, volumeMax, directVolume, toneLevelMax,
                toneLevelMax != null, balanceLevelMax, playControl, null, dspCategory, getFrequencyAvailable,
                getSpeakerGroupsAvailable, getDimmerLevelAvailable, diummerLevelMin, diummerLevelMax, otherCommands,
                (byte) 0, 0, 0, false, RotelFlagsMapping.NO_MAPPING, specialCharacters);
    }

    /**
     * Constructor
     *
     * @param name the model name
     * @param baudRate the baud rate to be used for the RS232 communication
     * @param powerStateCmd the command to be used to check the power state of the device
     * @param sourceCategory the category from {@link RotelSource}
     * @param nbAdditionalZones the number of additional zones
     * @param additionalCommands true if other than primary commands are available
     * @param powerControlPerZone true if device supports power control per zone
     * @param volumeMax the maximum volume or null if no volume management is available
     * @param directVolume true if a command to set the volume with a value is available
     * @param toneLevelMax the maximum tone level or null if no bass/treble management is available
     * @param getBypassStatusAvailable true if the command to get the bypass status for tone control is available
     * @param balanceLevelMax the maximum balance level or null if no balance management is available
     * @param playControl true if control of source playback is available
     * @param zoneSelectCmd the command to be used to select a zone
     * @param dspCategory the category from {@link RotelDsp}
     * @param getFrequencyAvailable true if the command to get the frequency for digital source input is available
     * @param getSpeakerGroupsAvailable true if the command to switch speaker groups is available
     * @param getDimmerLevelAvailable true if the command to get the front display dimmer level is available
     * @param diummerLevelMin the minimum front display dimmer level or null if dimmer control is unavailable
     * @param diummerLevelMax the maximum front display dimmer level or null if dimmer control is unavailable
     * @param otherCommands the list of other available commands to expose
     * @param deviceId the device id (value to be used in the messages)
     * @param respNbChars the number of bytes for the characters in the standard response
     * @param respNbFlags the number of bytes for the flags in the standard response
     * @param charsBeforeFlags true if the characters are before the flags in the standard response message
     * @param flagsMapping the mapping of the flags in the feedback message
     * @param specialCharacters the table of special characters that can be found in the standard response message
     */
    private RotelModel(String name, int baudRate, RotelCommand powerStateCmd, int sourceCategory, int nbAdditionalZones,
            boolean additionalCommands, boolean powerControlPerZone, @Nullable Integer volumeMax, boolean directVolume,
            @Nullable Integer toneLevelMax, boolean getBypassStatusAvailable, @Nullable Integer balanceLevelMax,
            boolean playControl, @Nullable RotelCommand zoneSelectCmd, int dspCategory, boolean getFrequencyAvailable,
            boolean getSpeakerGroupsAvailable, boolean getDimmerLevelAvailable, @Nullable Integer diummerLevelMin,
            @Nullable Integer diummerLevelMax, List<RotelCommand> otherCommands, byte deviceId, int respNbChars,
            int respNbFlags, boolean charsBeforeFlags, RotelFlagsMapping flagsMapping, byte[][] specialCharacters) {
        this.name = name;
        this.baudRate = baudRate;
        this.powerStateCmd = powerStateCmd;
        this.sourceCategory = sourceCategory;
        this.nbAdditionalZones = nbAdditionalZones;
        this.additionalCommands = additionalCommands;
        this.powerControlPerZone = powerControlPerZone;
        this.volumeMax = volumeMax;
        this.directVolume = directVolume;
        this.toneLevelMax = toneLevelMax;
        this.getBypassStatusAvailable = getBypassStatusAvailable;
        this.balanceLevelMax = balanceLevelMax;
        this.playControl = playControl;
        this.zoneSelectCmd = zoneSelectCmd;
        this.dspCategory = dspCategory;
        this.getFrequencyAvailable = getFrequencyAvailable;
        this.getSpeakerGroupsAvailable = getSpeakerGroupsAvailable;
        this.getDimmerLevelAvailable = getDimmerLevelAvailable;
        this.diummerLevelMin = diummerLevelMin;
        this.diummerLevelMax = diummerLevelMax;
        this.otherCommands = otherCommands;
        this.deviceId = deviceId;
        this.respNbChars = respNbChars;
        this.respNbFlags = respNbFlags;
        this.charsBeforeFlags = charsBeforeFlags;
        this.flagsMapping = flagsMapping;
        this.specialCharacters = specialCharacters;
    }

    /**
     * Get the model name
     *
     * @return the model name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the baud rate to be used for the RS232 communication
     *
     * @return the baud rate
     */
    public int getBaudRate() {
        return baudRate;
    }

    /**
     * Get the command to be used to check the power state of the device
     *
     * @return the command
     */
    public RotelCommand getPowerStateCmd() {
        return powerStateCmd;
    }

    /**
     * Inform whether source control is available
     *
     * @return true if source control is available
     */
    public boolean hasSourceControl() {
        return sourceCategory >= 1;
    }

    /**
     * Get the number of zones
     *
     * @return the number of zones
     */
    public int getNumberOfZones() {
        return nbAdditionalZones + 1;
    }

    private boolean isZoneAvailable(int numZone) {
        return numZone >= 1 && numZone <= getNumberOfZones();
    }

    /**
     * Inform whether other than primary commands are available
     *
     * @return true if other than primary commands are available
     */
    public boolean hasOtherThanPrimaryCommands() {
        return additionalCommands;
    }

    /**
     * Inform whether zone N commands are available
     *
     * @param numZone the zone number, 1 for for zone 1 until 4 for zone 4
     *
     * @return true if zone N commands are available
     */
    public boolean hasZoneCommands(int numZone) {
        if (numZone < 1 || numZone > MAX_NUMBER_OF_ZONES) {
            throw new IllegalArgumentException("numZone must be in range 1-" + MAX_NUMBER_OF_ZONES);
        }
        return additionalCommands && isZoneAvailable(numZone);
    }

    /**
     * Inform whether source control is available in a zone
     *
     * @param numZone the zone number, 1 for zone 1 until 4 for zone 4
     *
     * @return true if source control is available
     */
    public boolean hasZoneSourceControl(int numZone) {
        if (numZone < 1 || numZone > MAX_NUMBER_OF_ZONES) {
            throw new IllegalArgumentException("numZone must be in range 1-" + MAX_NUMBER_OF_ZONES);
        }
        return hasSourceControl() && isZoneAvailable(numZone);
    }

    /**
     * Inform whether device supports power control per zone
     *
     * @return true if device supports power control per zone
     */
    public boolean hasPowerControlPerZone() {
        return powerControlPerZone;
    }

    /**
     * Inform whether volume control is available
     *
     * @return true if volume control is available
     */
    public boolean hasVolumeControl() {
        return volumeMax != null;
    }

    /**
     * Get the maximum volume
     *
     * @return the maximum volume or 0 if volume control is unavailable
     */
    public int getVolumeMax() {
        Integer volumeMax = this.volumeMax;
        return volumeMax != null ? volumeMax.intValue() : 0;
    }

    /**
     * Inform whether a command to set the volume with a value is available
     *
     * @return true if a command to set the volume with a value is available
     */
    public boolean hasDirectVolumeControl() {
        return directVolume;
    }

    /**
     * Inform whether bass/treble control is available
     *
     * @return true if bass/treble control is available
     */
    public boolean hasToneControl() {
        return toneLevelMax != null;
    }

    /**
     * Inform whether the command to get the current bypass status for tone control is available
     *
     * @return true if the command is available
     */
    public boolean canGetBypassStatus() {
        return getBypassStatusAvailable;
    }

    /**
     * Get the maximum tone level
     *
     * @return the maximum tone level or 0 if bass/treble control is unavailable
     */
    public int getToneLevelMax() {
        Integer toneLevelMax = this.toneLevelMax;
        return toneLevelMax != null ? toneLevelMax.intValue() : 0;
    }

    /**
     * Inform whether balance control is available
     *
     * @return true if balance control is available
     */
    public boolean hasBalanceControl() {
        return balanceLevelMax != null;
    }

    /**
     * Get the maximum balance level
     *
     * @return the maximum balance level or 0
     */
    public int getBalanceLevelMax() {
        Integer balanceLevelMax = this.balanceLevelMax;
        return balanceLevelMax != null ? balanceLevelMax.intValue() : 0;
    }

    /**
     * Inform whether the command to switch speaker groups is available
     *
     * @return true if the command is available
     */
    public boolean hasSpeakerGroups() {
        return getSpeakerGroupsAvailable;
    }

    /**
     * Inform whether the command to get the current frequency for digital source input is available
     *
     * @return true if the command is available
     */
    public boolean canGetFrequency() {
        return getFrequencyAvailable;
    }

    /**
     * Inform whether dimmer control is available
     *
     * @return true if dimmer control is available
     */
    public boolean hasDimmerControl() {
        return diummerLevelMin != null && diummerLevelMax != null;
    }

    /**
     * Get the minimum front display dimmer level
     *
     * @return the minimum front display dimmer level or 0 if dimmer control is unavailable
     */
    public int getDimmerLevelMin() {
        Integer diummerLevelMin = this.diummerLevelMin;
        return diummerLevelMin != null ? diummerLevelMin.intValue() : 0;
    }

    /**
     * Get the maximum front display dimmer level
     *
     * @return the maximum front display dimmer level or 0 if dimmer control is unavailable
     */
    public int getDimmerLevelMax() {
        Integer diummerLevelMax = this.diummerLevelMax;
        return diummerLevelMax != null ? diummerLevelMax.intValue() : 0;
    }

    /**
     * Inform whether the command to get the front display dimmer level is available
     *
     * @return true if the command is available
     */
    public boolean canGetDimmerLevel() {
        return getDimmerLevelAvailable;
    }

    /**
     * Inform whether control of source playback is available
     *
     * @return true if control of source playback is available
     */
    public boolean hasPlayControl() {
        return playControl;
    }

    /**
     * Get the command to be used to select a zone
     *
     * @return the command
     */
    public @Nullable RotelCommand getZoneSelectCmd() {
        return zoneSelectCmd;
    }

    /**
     * Inform whether DSP control is available
     *
     * @return true if DSP control is available
     */
    public boolean hasDspControl() {
        return dspCategory >= 1;
    }

    /**
     * Get the device id (value to be used in the messages)
     *
     * @return the device id
     */
    public byte getDeviceId() {
        return deviceId;
    }

    /**
     * Get the number of bytes for the characters in the standard response
     *
     * @return the number of bytes
     */
    public int getRespNbChars() {
        return respNbChars;
    }

    /**
     * Get the number of bytes for the flags in the standard response
     *
     * @return the number of bytes
     */
    public int getRespNbFlags() {
        return respNbFlags;
    }

    /**
     * Inform whether the characters are before or after the flags in the standard response message
     *
     * @return true if the characters are before the flags in the standard response message
     */
    public boolean isCharsBeforeFlags() {
        return charsBeforeFlags;
    }

    /**
     * Get the table of special characters that can be found in the standard response message
     *
     * @return the table of bytes sequence representing the special characters
     */
    public byte[][] getSpecialCharacters() {
        return specialCharacters;
    }

    /**
     * Get the list of available {@link RotelSource}
     *
     * @return the list of available {@link RotelSource}
     */
    public List<RotelSource> getSources() {
        return hasSourceControl() ? RotelSource.getSources(sourceCategory, 0) : new ArrayList<>();
    }

    /**
     * Get the list of available {@link RotelSource} in a zone
     *
     * @param numZone the zone number, 1 for zone 1 until 4 for zone 4
     *
     * @return the list of available {@link RotelSource} in the zone 2
     */
    public List<RotelSource> getZoneSources(int numZone) {
        if (numZone < 1 || numZone > MAX_NUMBER_OF_ZONES) {
            throw new IllegalArgumentException("numZone must be in range 1-" + MAX_NUMBER_OF_ZONES);
        }
        return hasZoneSourceControl(numZone) ? RotelSource.getSources(sourceCategory, numZone) : new ArrayList<>();
    }

    /**
     * Get the list of available {@link RotelSource} for recording
     *
     * @return the list of available {@link RotelSource} for recording
     */
    public List<RotelSource> getRecordSources() {
        return hasSourceControl() ? RotelSource.getSources(sourceCategory, 5) : new ArrayList<>();
    }

    /**
     * Get the source associated to a name
     *
     * @param name the name used to identify the source
     *
     * @return the source associated to the searched name
     *
     * @throws RotelException - If no source is associated to the searched name
     */
    public RotelSource getSourceFromName(String name) throws RotelException {
        return RotelSource.getFromName(sourceCategory, name);
    }

    /**
     * Get the source associated to a command
     *
     * @param command the command used to identify the source
     *
     * @return the source associated to the searched command
     *
     * @throws RotelException - If no source is associated to the searched command
     */
    public RotelSource getSourceFromCommand(RotelCommand command) throws RotelException {
        return RotelSource.getFromCommand(sourceCategory, command, 0);
    }

    /**
     * Get the zone N source associated to a command
     *
     * @param command the command used to identify the zone N source
     * @param numZone the zone number, 1 for zone 1 until 4 for zone 4
     *
     * @return the zone N source associated to the searched command
     *
     * @throws RotelException - If no zone N source is associated to the searched command
     */
    public RotelSource getZoneSourceFromCommand(RotelCommand command, int numZone) throws RotelException {
        if (numZone < 1 || numZone > MAX_NUMBER_OF_ZONES) {
            throw new IllegalArgumentException("numZone must be in range 1-" + MAX_NUMBER_OF_ZONES);
        }
        return RotelSource.getFromCommand(sourceCategory, command, numZone);
    }

    /**
     * Get the record source associated to a command
     *
     * @param command the command used to identify the record source
     *
     * @return the record source associated to the searched command
     *
     * @throws RotelException - If no record source is associated to the searched command
     */
    public RotelSource getRecordSourceFromCommand(RotelCommand command) throws RotelException {
        return RotelSource.getFromCommand(sourceCategory, command, 5);
    }

    /**
     * Inform whether the multiple input source is set to ON in the flags
     *
     * @param flags the flag from the standard response message
     *
     * @return true if the multiple input source is ON
     *
     * @throws RotelException - If this information is not present in the flags for this model
     */
    public boolean isMultiInputOn(byte[] flags) throws RotelException {
        return flagsMapping.isMultiInputOn(flags);
    }

    /**
     * Set the multiple input source to ON or OFF in the flags
     *
     * @param flags the flag from the standard response message
     * @param on true for ON and false for OFF
     *
     * @throws RotelException - If this information is not present in the flags for this model
     */
    public void setMultiInput(byte[] flags, boolean on) throws RotelException {
        flagsMapping.setMultiInput(flags, on);
    }

    /**
     * Inform whether the zone 2 is set to ON in the flags
     *
     * @param flags the flag from the standard response message
     *
     * @return true if the zone 2 is ON
     *
     * @throws RotelException - If this information is not present in the flags for this model
     */
    public boolean isZone2On(byte[] flags) throws RotelException {
        return flagsMapping.isZone2On(flags);
    }

    /**
     * Set the zone 2 to ON or OFF in the flags
     *
     * @param flags the flag from the standard response message
     * @param on true for ON and false for OFF
     *
     * @throws RotelException - If this information is not present in the flags for this model
     */
    public void setZone2(byte[] flags, boolean on) throws RotelException {
        flagsMapping.setZone2(flags, on);
    }

    /**
     * Inform whether the zone 3 is set to ON in the flags
     *
     * @param flags the flag from the standard response message
     *
     * @return true if the zone 3 is ON
     *
     * @throws RotelException - If this information is not present in the flags for this model
     */
    public boolean isZone3On(byte[] flags) throws RotelException {
        return flagsMapping.isZone3On(flags);
    }

    /**
     * Set the zone 3 to ON or OFF in the flags
     *
     * @param flags the flag from the standard response message
     * @param on true for ON and false for OFF
     *
     * @throws RotelException - If this information is not present in the flags for this model
     */
    public void setZone3(byte[] flags, boolean on) throws RotelException {
        flagsMapping.setZone3(flags, on);
    }

    /**
     * Inform whether the zone 4 is set to ON in the flags
     *
     * @param flags the flag from the standard response message
     *
     * @return true if the zone 4 is ON
     *
     * @throws RotelException - If this information is not present in the flags for this model
     */
    public boolean isZone4On(byte[] flags) throws RotelException {
        return flagsMapping.isZone4On(flags);
    }

    /**
     * Set the zone 4 to ON or OFF in the flags
     *
     * @param flags the flag from the standard response message
     * @param on true for ON and false for OFF
     *
     * @throws RotelException - If this information is not present in the flags for this model
     */
    public void setZone4(byte[] flags, boolean on) throws RotelException {
        flagsMapping.setZone4(flags, on);
    }

    /**
     * Inform whether more than front left and front right channels are set as active in the flags
     *
     * @param flags the flag from the standard response message
     *
     * @return true if more than front left and front right channels are active
     *
     * @throws RotelException - If this information is not present in the flags for this model
     */
    public boolean isMoreThan2Channels(byte[] flags) throws RotelException {
        return flagsMapping.isMoreThan2Channels(flags);
    }

    /**
     * Get the list of {@link StateOption} associated to the available DSP modes
     *
     * @return the list of {@link StateOption} associated to the available DSP modes
     */
    public List<StateOption> getDspStateOptions() {
        return RotelDsp.getStateOptions(dspCategory);
    }

    /**
     * Get the command used to select a DSP mode
     *
     * @param name the name used to identify the DSP mode
     *
     * @return the select command associated to the searched name
     *
     * @throws RotelException - If no DSP mode is associated to the searched name
     */
    public RotelCommand getCommandFromDspName(String name) throws RotelException {
        return RotelDsp.getFromName(dspCategory, name).getCommand();
    }

    /**
     * Get the DSP mode identified by a feedback message
     *
     * @param feedback the feedback message used to identify the DSP mode
     *
     * @return the DSP mode associated to the searched feedback message
     *
     * @throws RotelException - If no DSP mode is associated to the searched feedback message
     */
    public RotelDsp getDspFromFeedback(String feedback) throws RotelException {
        return RotelDsp.getFromFeedback(dspCategory, feedback);
    }

    /**
     * Get the list of {@link CommandOption} associated to the available other commands
     *
     * @param protocol the used protocol for the model
     *
     * @return the list of {@link CommandOption} associated to the available other commands
     */
    public List<CommandOption> getOtherCommandsOptions(RotelProtocol protocol) {
        List<CommandOption> options = new ArrayList<>();
        for (RotelCommand cmd : otherCommands) {
            switch (protocol) {
                case HEX:
                    if (cmd.getHexType() != 0) {
                        options.add(new CommandOption(cmd.name(), cmd.getLabel()));
                    }
                    break;
                case ASCII_V1:
                    if (cmd.getAsciiCommandV1() != null) {
                        options.add(new CommandOption(cmd.name(), cmd.getLabel()));
                    }
                    break;
                case ASCII_V2:
                    if (cmd.getAsciiCommandV2() != null) {
                        options.add(new CommandOption(cmd.name(), cmd.getLabel()));
                    }
                    break;
            }
        }
        return options;
    }

    /**
     * Get the model associated to a name
     *
     * @param name the name used to identify the model
     *
     * @return the model associated to the searched name
     *
     * @throws RotelException - If no model is associated to the searched name
     */
    public static RotelModel getFromName(String name) throws RotelException {
        for (RotelModel value : RotelModel.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        throw new RotelException("Invalid model name: " + name);
    }
}
