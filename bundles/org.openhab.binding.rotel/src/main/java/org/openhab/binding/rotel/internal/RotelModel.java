/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.rotel.internal.communication.RotelCommand;
import org.openhab.binding.rotel.internal.communication.RotelConnector;
import org.openhab.binding.rotel.internal.communication.RotelDsp;
import org.openhab.binding.rotel.internal.communication.RotelFlagsMapping;
import org.openhab.binding.rotel.internal.communication.RotelSource;

/**
 * Represents the different supported models
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum RotelModel {

    RSP1066("RSP-1066", 19200, RotelCommand.DISPLAY_REFRESH, 3, 1, false, 90, false, 12, false,
            RotelCommand.ZONE_SELECT, 1, (byte) 0xC2, 13, 8, true, RotelFlagsMapping.MAPPING1,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSP1068("RSP-1068", 19200, RotelCommand.DISPLAY_REFRESH, 1, 1, true, 96, true, 6, false,
            RotelCommand.RECORD_FONCTION_SELECT, 2, (byte) 0xA1, 42, 5, true, RotelFlagsMapping.MAPPING2,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSP1069("RSP-1069", 38400, RotelCommand.DISPLAY_REFRESH, 1, 3, true, 96, true, 6, false,
            RotelCommand.RECORD_FONCTION_SELECT, 2, (byte) 0xA2, 42, 5, true, RotelFlagsMapping.MAPPING5,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSP1098("RSP-1098", 19200, RotelCommand.DISPLAY_REFRESH, 1, 1, true, 96, true, 6, false, RotelCommand.ZONE_SELECT,
            2, (byte) 0xA0, 13, 8, true, RotelFlagsMapping.MAPPING1, RotelConnector.NO_SPECIAL_CHARACTERS),
    RSP1570("RSP-1570", 115200, RotelCommand.DISPLAY_REFRESH, 1, 3, true, 96, true, 6, false,
            RotelCommand.RECORD_FONCTION_SELECT, 3, (byte) 0xA3, 42, 5, true, RotelFlagsMapping.MAPPING5,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSP1572("RSP-1572", 115200, RotelCommand.DISPLAY_REFRESH, 2, 3, true, 96, true, null, false,
            RotelCommand.RECORD_FONCTION_SELECT, 4, (byte) 0xA5, 42, 5, true, RotelFlagsMapping.MAPPING5,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSX1055("RSX-1055", 19200, RotelCommand.DISPLAY_REFRESH, 3, 1, false, 90, false, 12, false,
            RotelCommand.ZONE_SELECT, 1, (byte) 0xC3, 13, 8, true, RotelFlagsMapping.MAPPING1,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSX1056("RSX-1056", 19200, RotelCommand.DISPLAY_REFRESH, 1, 1, true, 96, true, 12, false, RotelCommand.ZONE_SELECT,
            2, (byte) 0xC5, 13, 8, true, RotelFlagsMapping.MAPPING1, RotelConnector.NO_SPECIAL_CHARACTERS),
    RSX1057("RSX-1057", 19200, RotelCommand.DISPLAY_REFRESH, 1, 1, true, 96, true, 6, false,
            RotelCommand.RECORD_FONCTION_SELECT, 2, (byte) 0xC7, 13, 8, true, RotelFlagsMapping.MAPPING1,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSX1058("RSX-1058", 38400, RotelCommand.DISPLAY_REFRESH, 1, 3, true, 96, true, 6, false,
            RotelCommand.RECORD_FONCTION_SELECT, 2, (byte) 0xC8, 13, 8, true, RotelFlagsMapping.MAPPING4,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSX1065("RSX-1065", 19200, RotelCommand.DISPLAY_REFRESH, 3, 1, false, 96, false, 12, false,
            RotelCommand.ZONE_SELECT, 1, (byte) 0xC1, 42, 5, true, RotelFlagsMapping.MAPPING2,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSX1067("RSX-1067", 19200, RotelCommand.DISPLAY_REFRESH, 1, 1, true, 96, true, 6, false,
            RotelCommand.RECORD_FONCTION_SELECT, 2, (byte) 0xC4, 42, 5, true, RotelFlagsMapping.MAPPING2,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSX1550("RSX-1550", 115200, RotelCommand.DISPLAY_REFRESH, 1, 3, true, 96, true, 6, false,
            RotelCommand.RECORD_FONCTION_SELECT, 3, (byte) 0xC9, 13, 8, true, RotelFlagsMapping.MAPPING3,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSX1560("RSX-1560", 115200, RotelCommand.DISPLAY_REFRESH, 1, 3, true, 96, true, 6, false,
            RotelCommand.RECORD_FONCTION_SELECT, 3, (byte) 0xCA, 42, 5, true, RotelFlagsMapping.MAPPING5,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    RSX1562("RSX-1562", 115200, RotelCommand.DISPLAY_REFRESH, 2, 3, true, 96, true, null, false,
            RotelCommand.RECORD_FONCTION_SELECT, 4, (byte) 0xCC, 42, 5, true, RotelFlagsMapping.MAPPING5,
            RotelConnector.NO_SPECIAL_CHARACTERS),
    A11("A11", 115200, RotelCommand.POWER, 4, 0, false, 96, true, 10, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    A12("A12", 115200, RotelCommand.POWER, 5, 0, false, 96, true, 10, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    A14("A14", 115200, RotelCommand.POWER, 5, 0, false, 96, true, 10, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    CD11("CD11", 57600, RotelCommand.POWER, 0, 0, false, null, false, null, true, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    CD14("CD14", 57600, RotelCommand.POWER, 0, 0, false, null, false, null, true, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    RA11("RA-11", 115200, RotelCommand.POWER, 6, 0, false, 96, true, 10, true, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RA12("RA-12", 115200, RotelCommand.POWER, 6, 0, false, 96, true, 10, true, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RA1570("RA-1570", 115200, RotelCommand.POWER, 7, 0, false, 96, true, 10, true, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RA1572("RA-1572", 115200, RotelCommand.POWER, 8, 0, false, 96, true, 10, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RA1592("RA-1592", 115200, RotelCommand.POWER, 9, 0, false, 96, true, 10, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RAP1580("RAP-1580", 115200, RotelCommand.POWER, 11, 0, false, 96, true, null, false, null, 5, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    RC1570("RC-1570", 115200, RotelCommand.POWER, 7, 0, false, 96, true, 10, true, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RC1572("RC-1572", 115200, RotelCommand.POWER, 8, 0, false, 96, true, 10, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RC1590("RC-1590", 115200, RotelCommand.POWER, 9, 0, false, 96, true, 10, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RCD1570("RCD-1570", 115200, RotelCommand.POWER, 0, 0, false, null, false, null, true, null, -1, (byte) 0, 0, 0,
            false, RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RCD1572("RCD-1572", 57600, RotelCommand.POWER, 0, 0, false, null, false, null, true, null, -1, (byte) 0, 0, 0,
            false, RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS_RCD1572),
    RCX1500("RCX-1500", 115200, RotelCommand.POWER, 17, 0, false, 86, true, null, true, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RDD1580("RDD-1580", 115200, RotelCommand.POWER, 15, 0, false, null, false, null, true, null, -1, (byte) 0, 0, 0,
            false, RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    RDG1520("RDG-1520", 115200, RotelCommand.POWER, 16, 0, false, null, false, null, true, null, -1, (byte) 0, 0, 0,
            false, RotelFlagsMapping.NO_MAPPING, RotelConnector.SPECIAL_CHARACTERS),
    RSP1576("RSP-1576", 115200, RotelCommand.POWER, 10, 0, false, 96, true, null, false, null, 5, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    RSP1582("RSP-1582", 115200, RotelCommand.POWER, 11, 0, false, 96, true, null, false, null, 6, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    RT11("RT-11", 115200, RotelCommand.POWER, 12, 0, false, null, false, null, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    RT1570("RT-1570", 115200, RotelCommand.POWER, 14, 0, false, null, false, null, false, null, -1, (byte) 0, 0, 0,
            false, RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    T11("T11", 115200, RotelCommand.POWER, 12, 0, false, null, false, null, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS),
    T14("T14", 115200, RotelCommand.POWER, 13, 0, false, null, false, null, false, null, -1, (byte) 0, 0, 0, false,
            RotelFlagsMapping.NO_MAPPING, RotelConnector.NO_SPECIAL_CHARACTERS);

    private String name;
    private int baudRate;
    private RotelCommand powerStateCmd;
    private int sourceCategory;
    private int nbAdditionalZones;
    private boolean additionalCommands;
    @Nullable
    private Integer volumeMax;
    private boolean directVolume;
    @Nullable
    private Integer toneLevelMax;
    private boolean playControl;
    @Nullable
    private RotelCommand zoneSelectCmd;
    private int dspCategory;
    private byte deviceId;
    private int respNbChars;
    private int respNbFlags;
    private boolean charsBeforeFlags;
    private RotelFlagsMapping flagsMapping;
    private byte[][] specialCharacters;

    /**
     * Constructor
     *
     * @param name the model name
     * @param baudRate the baud rate to be used for the RS232 communication
     * @param powerStateCmd the command to be used to check the power state of the device
     * @param sourceCategory the category from {@link RotelSource}
     * @param nbAdditionalZones the number of additional zones
     * @param additionalCommands true if other than primary commands are available
     * @param volumeMax the maximum volume or null if no volume management is available
     * @param directVolume true if a command to set the volume with a value is available
     * @param toneLevelMax the maximum tone level or null if no bass/treble management is available
     * @param playControl true if control of source playback is available
     * @param zoneSelectCmd the command to be used to select a zone
     * @param dspCategory the category from {@link RotelDsp}
     * @param deviceId the device id (value to be used in the messages)
     * @param respNbChars the number of bytes for the characters in the standard response
     * @param respNbFlags the number of bytes for the flags in the standard response
     * @param charsBeforeFlags true if the characters are before the flags in the standard response message
     * @param flagsMapping the mapping of the flags in the feedback message
     * @param specialCharacters the table of special characters that can be found in the standard response message
     */
    private RotelModel(String name, int baudRate, RotelCommand powerStateCmd, int sourceCategory, int nbAdditionalZones,
            boolean additionalCommands, @Nullable Integer volumeMax, boolean directVolume,
            @Nullable Integer toneLevelMax, boolean playControl, @Nullable RotelCommand zoneSelectCmd, int dspCategory,
            byte deviceId, int respNbChars, int respNbFlags, boolean charsBeforeFlags, RotelFlagsMapping flagsMapping,
            byte[][] specialCharacters) {
        this.name = name;
        this.baudRate = baudRate;
        this.powerStateCmd = powerStateCmd;
        this.sourceCategory = sourceCategory;
        this.nbAdditionalZones = nbAdditionalZones;
        this.additionalCommands = additionalCommands;
        this.volumeMax = volumeMax;
        this.directVolume = directVolume;
        this.toneLevelMax = toneLevelMax;
        this.playControl = playControl;
        this.zoneSelectCmd = zoneSelectCmd;
        this.dspCategory = dspCategory;
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
     * Get the number of additional zones
     *
     * @return the number of additional zones
     */
    public int getNbAdditionalZones() {
        return nbAdditionalZones;
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
     * Inform whether zone 2 commands are available
     *
     * @return true if zone 2 commands are available
     */
    public boolean hasZone2Commands() {
        return nbAdditionalZones >= 1 && additionalCommands;
    }

    /**
     * Inform whether zone 3 commands are available
     *
     * @return true if zone 3 commands are available
     */
    public boolean hasZone3Commands() {
        return nbAdditionalZones >= 2 && additionalCommands;
    }

    /**
     * Inform whether zone 4 commands are available
     *
     * @return true if zone 4 commands are available
     */
    public boolean hasZone4Commands() {
        return nbAdditionalZones >= 3 && additionalCommands;
    }

    /**
     * Inform whether source control is available in the zone 2
     *
     * @return true if source control is available
     */
    public boolean hasZone2SourceControl() {
        return sourceCategory >= 1 && nbAdditionalZones >= 1;
    }

    /**
     * Inform whether source control is available in the zone 3
     *
     * @return true if source control is available
     */
    public boolean hasZone3SourceControl() {
        return sourceCategory >= 1 && nbAdditionalZones >= 2;
    }

    /**
     * Inform whether source control is available in the zone 4
     *
     * @return true if source control is available
     */
    public boolean hasZone4SourceControl() {
        return sourceCategory >= 1 && nbAdditionalZones >= 3;
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
     * Get the maximum tone level
     *
     * @return the maximum tone level or 0 if bass/treble control is unavailable
     */
    public int getToneLevelMax() {
        return toneLevelMax != null ? toneLevelMax.intValue() : 0;
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
     * Set a source label
     *
     * @param name the source name
     * @param label the source label
     *
     * @throws RotelException - If no source with this name exists
     */
    public void setSourceLabel(String name, String label) throws RotelException {
        RotelSource.getFromName(sourceCategory, name).setLabel(label);
    }

    /**
     * Get the list of {@link StateOption} associated to the available sources
     *
     * @return the list of {@link StateOption} associated to the available sources
     */
    public List<StateOption> getSourceStateOptions() {
        return hasSourceControl() ? RotelSource.getStateOptions(sourceCategory, 0) : new ArrayList<>();
    }

    /**
     * Get the list of {@link StateOption} associated to the available sources in the main zone
     *
     * @return the list of {@link StateOption} associated to the available sources
     */
    public List<StateOption> getMainZoneSourceStateOptions() {
        return hasZone2SourceControl() ? RotelSource.getStateOptions(sourceCategory, 1) : new ArrayList<>();
    }

    /**
     * Get the list of {@link StateOption} associated to the available sources in the zone 2
     *
     * @return the list of {@link StateOption} associated to the available sources
     */
    public List<StateOption> getZone2SourceStateOptions() {
        return hasZone2SourceControl() ? RotelSource.getStateOptions(sourceCategory, 2) : new ArrayList<>();
    }

    /**
     * Get the list of {@link StateOption} associated to the available sources in the zone 3
     *
     * @return the list of {@link StateOption} associated to the available sources
     */
    public List<StateOption> getZone3SourceStateOptions() {
        return hasZone3SourceControl() ? RotelSource.getStateOptions(sourceCategory, 3) : new ArrayList<>();
    }

    /**
     * Get the list of {@link StateOption} associated to the available sources in the zone 4
     *
     * @return the list of {@link StateOption} associated to the available sources
     */
    public List<StateOption> getZone4SourceStateOptions() {
        return hasZone4SourceControl() ? RotelSource.getStateOptions(sourceCategory, 4) : new ArrayList<>();
    }

    /**
     * Get the list of {@link StateOption} associated to the available sources for recording
     *
     * @return the list of {@link StateOption} associated to the available sources
     */
    public List<StateOption> getRecordSourceStateOptions() {
        return hasSourceControl() ? RotelSource.getStateOptions(sourceCategory, 5) : new ArrayList<>();
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
     * Get the main zone source associated to a command
     *
     * @param command the command used to identify the main zone source
     *
     * @return the main zone source associated to the searched command
     *
     * @throws RotelException - If no main zone source is associated to the searched command
     */
    public RotelSource getMainZoneSourceFromCommand(RotelCommand command) throws RotelException {
        return RotelSource.getFromCommand(sourceCategory, command, 1);
    }

    /**
     * Get the zone 2 source associated to a command
     *
     * @param command the command used to identify the zone 2 source
     *
     * @return the zone 2 source associated to the searched command
     *
     * @throws RotelException - If no zone 2 source is associated to the searched command
     */
    public RotelSource getZone2SourceFromCommand(RotelCommand command) throws RotelException {
        return RotelSource.getFromCommand(sourceCategory, command, 2);
    }

    /**
     * Get the zone 3 source associated to a command
     *
     * @param command the command used to identify the zone 3 source
     *
     * @return the zone 3 source associated to the searched command
     *
     * @throws RotelException - If no zone 3 source is associated to the searched command
     */
    public RotelSource getZone3SourceFromCommand(RotelCommand command) throws RotelException {
        return RotelSource.getFromCommand(sourceCategory, command, 3);
    }

    /**
     * Get the zone 4 source associated to a command
     *
     * @param command the command used to identify the zone 4 source
     *
     * @return the zone 4 source associated to the searched command
     *
     * @throws RotelException - If no zone 4 source is associated to the searched command
     */
    public RotelSource getZone4SourceFromCommand(RotelCommand command) throws RotelException {
        return RotelSource.getFromCommand(sourceCategory, command, 4);
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
     * Get the source associated to a label displayed on the Rotel front panel
     *
     * @param label the label used to identify the source
     *
     * @return the source associated to the searched label
     *
     * @throws RotelException - If no source is associated to the searched label
     */
    public RotelSource getSourceFromLabel(String label) throws RotelException {
        return RotelSource.getFromLabel(sourceCategory, label);
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
