/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link S_ConfigCommand} for setting MAX! thermostat configuration.
 *
 * @author Marcel Verpaalen - Initial version
 * @since 2.0
 */
public class S_ConfigCommand extends CubeCommand {

    private String baseString = null;
    private String rfAddress = null;
    private int roomId = -1;

    private byte[] commandBytes;

    private Logger logger = LoggerFactory.getLogger(S_ConfigCommand.class);

    public enum ConfigCommandType {
        Temperature,
        Valve,
        SetRoom,
        RemoveRoom,
        ProgramData
    }

    private ConfigCommandType configCommandType;

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
    public S_ConfigCommand(String rfAddress, int roomId, ConfigCommandType configCommandType) {
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
     *
     * Set the Thermostat temperature configuration
     *
     * @param rfAddress
     * @param roomId
     * @param tempComfort
     * @param tempEco
     * @param tempSetpointMax
     * @param tempSetpointMin
     * @param tempOffset
     * @param tempOpenWindow
     * @param durationOpenWindow
     */
    public S_ConfigCommand(String rfAddress, int roomId, double tempComfort, double tempEco, double tempSetpointMax,
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

        Byte tempComfortByte = (byte) (tempComfort * 2);
        Byte tempEcoByte = (byte) (tempEco * 2);
        Byte tempSetpointMaxByte = (byte) (tempSetpointMax * 2);
        Byte tempSetpointMinByte = (byte) (tempSetpointMin * 2);
        Byte tempOffsetByte = (byte) ((tempOffset + 3.5) * 2);
        Byte tempOpenWindowByte = (byte) (tempOpenWindow * 2);
        Byte durationOpenWindowByte = (byte) (durationOpenWindow / 5);
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

        StringBuilder commandConfigString = new StringBuilder();
        for (byte b : commandBytes) {
            commandConfigString.append(String.format("%02X", b));
        }

        String commandString = baseString + rfAddress;
        if (configCommandType == ConfigCommandType.SetRoom || configCommandType == ConfigCommandType.RemoveRoom) {
            commandString = commandString + commandConfigString + Utils.toHex(roomId);
        } else {
            commandString = commandString + Utils.toHex(roomId) + commandConfigString;
        }

        String encodedString = Base64.encodeBase64String(Utils.hexStringToByteArray(commandString));
        return "s:" + encodedString;
    }

    @Override
    public String getReturnStrings() {
        return "S:";
    }
}
