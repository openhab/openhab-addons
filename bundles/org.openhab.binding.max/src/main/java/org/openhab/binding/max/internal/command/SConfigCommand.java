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
package org.openhab.binding.max.internal.command;

import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.max.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SConfigCommand} for setting MAX! thermostat configuration.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class SConfigCommand extends CubeCommand {

    private String baseString = "";
    private final String rfAddress;
    private final int roomId;

    private byte[] commandBytes = new byte[0];

    private final Logger logger = LoggerFactory.getLogger(SConfigCommand.class);

    public enum ConfigCommandType {
        Temperature,
        Valve,
        SetRoom,
        RemoveRoom,
        ProgramData
    }

    private final ConfigCommandType configCommandType;

    /**
     * Creates a base command with rfAddress and roomID.
     *
     * @param rfAddress
     *            the RF address the command is for
     * @param roomId
     *            the room ID the RF address is mapped to
     * @param configCommandType
     *            the Type of config command to be send
     */
    public SConfigCommand(String rfAddress, int roomId, ConfigCommandType configCommandType) {
        this.rfAddress = rfAddress;
        this.roomId = roomId;
        this.configCommandType = configCommandType;
        if (configCommandType == ConfigCommandType.Temperature) {
            setTempConfigDefault();
        } else if (configCommandType == ConfigCommandType.SetRoom) {
            baseString = "000022000000";
            commandBytes = new byte[] { 0 };
        } else if (configCommandType == ConfigCommandType.RemoveRoom) {
            baseString = "000023000000";
            commandBytes = new byte[] { 0 };
        } else {
            logger.debug("Config Command {} not implemented", configCommandType);
        }
    }

    /**
     * Set the Thermostat temperature configuration
     */
    public SConfigCommand(String rfAddress, int roomId, double tempComfort, double tempEco, double tempSetpointMax,
            double tempSetpointMin, double tempOffset, double tempOpenWindow, int durationOpenWindow) {
        this.rfAddress = rfAddress;
        this.roomId = roomId;
        this.configCommandType = ConfigCommandType.Temperature;
        setTempConfig(tempComfort, tempEco, tempSetpointMax, tempSetpointMin, tempOffset, tempOpenWindow,
                durationOpenWindow);
    }

    /**
     * Set the thermostat default temperature config.
     */
    public void setTempConfigDefault() {
        double tempComfort = 21.0; // 0x2a
        double tempEco = 17.0; // 0x28
        double tempSetpointMax = 30.0; // 0x3d
        double tempSetpointMin = 4.5; // 0x09
        double tempOffset = 3.5; // 0x07
        double tempOpenWindow = 12.0; // 0x18;
        int durationOpenWindow = 3; // 0x03;
        setTempConfig(tempComfort, tempEco, tempSetpointMax, tempSetpointMin, tempOffset, tempOpenWindow,
                durationOpenWindow);
    }

    /**
     * Set the thermostat temperature config.
     *
     * @param tempComfort
     *            the Comfort temperature
     * @param tempEco
     *            the ECO temperature
     * @param tempSetpointMax
     *            the Max temperature
     * @param tempSetpointMin
     *            the min temperature
     * @param tempOffset
     *            the offset temperature
     * @param tempOpenWindow
     *            the window open temperature
     * @param durationOpenWindow
     *            the window open duration in minutes
     */
    public void setTempConfig(double tempComfort, double tempEco, double tempSetpointMax, double tempSetpointMin,
            double tempOffset, double tempOpenWindow, int durationOpenWindow) {
        baseString = "000011000000";

        byte tempComfortByte = (byte) (tempComfort * 2);
        byte tempEcoByte = (byte) (tempEco * 2);
        byte tempSetpointMaxByte = (byte) (tempSetpointMax * 2);
        byte tempSetpointMinByte = (byte) (tempSetpointMin * 2);
        byte tempOffsetByte = (byte) ((tempOffset + 3.5) * 2);
        byte tempOpenWindowByte = (byte) (tempOpenWindow * 2);
        byte durationOpenWindowByte = (byte) (durationOpenWindow / 5);
        commandBytes = new byte[] { tempComfortByte, tempEcoByte, tempSetpointMaxByte, tempSetpointMinByte,
                tempOffsetByte, tempOpenWindowByte, durationOpenWindowByte };
        logger.debug(
                "Thermostat Config Command:  confTemp: {}, ecoTemp: {}, setMax: {}, setMin: {}, offset: {}, windowTemp: {}, windowDur:{}",
                tempComfort, tempEco, tempSetpointMax, tempSetpointMin, tempOffset, tempOpenWindow, durationOpenWindow);
    }

    /**
     * Returns the Base64 encoded command string to be sent via the MAX! Cube.
     *
     * @return the string representing the command
     */
    @Override
    public String getCommandString() {
        final StringBuilder commandConfigString = new StringBuilder();
        for (byte b : commandBytes) {
            commandConfigString.append(String.format("%02X", b));
        }

        String commandString = baseString + rfAddress;
        if (configCommandType == ConfigCommandType.SetRoom || configCommandType == ConfigCommandType.RemoveRoom) {
            commandString = commandString + commandConfigString + Utils.toHex(roomId);
        } else {
            commandString = commandString + Utils.toHex(roomId) + commandConfigString;
        }

        String encodedString = Base64.getEncoder().encodeToString(Utils.hexStringToByteArray(commandString));
        return "s:" + encodedString + "\r\n";
    }

    @Override
    public String getReturnStrings() {
        return "S:";
    }
}
