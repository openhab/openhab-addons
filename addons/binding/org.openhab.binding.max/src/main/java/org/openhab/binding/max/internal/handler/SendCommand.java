/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.max.internal.command.CubeCommand;

/**
 * Class for sending a command.
 *
 * @author Marcel Verpaalen
 *
 */
public final class SendCommand {

    private int id;
    private static int commandId = -1;

    private ChannelUID channelUID;
    private Command command;
    private CubeCommand cubeCommand = null;
    private String serialNumber;
    private String key;
    private String commandText;

    public SendCommand(String serialNumber, ChannelUID channelUID, Command command) {
        commandId += 1;
        id = commandId;
        this.serialNumber = serialNumber;
        this.channelUID = channelUID;
        this.command = command;
        key = getKey(serialNumber, channelUID);
        this.setCommandText(command.toString());
    }

    public SendCommand(String serialNumber, CubeCommand cubeCommand, String commandText) {
        commandId += 1;
        id = commandId;
        this.serialNumber = serialNumber;
        this.cubeCommand = cubeCommand;
        key = getKey(serialNumber, cubeCommand);
        this.setCommandText(commandText);
    }

    /**
     * Get the key based on the serial and channel
     * This is can be used to find duplicated commands in the queue
     */
    private static String getKey(String serialNumber, ChannelUID channelUID) {
        String key = serialNumber + "-" + channelUID.getId();
        return key;
    }

    /**
     * Get the key based on the serial and channel
     * This is can be used to find duplicated commands in the queue
     */
    private static String getKey(String serialNumber, CubeCommand cubeCommand) {
        String key = serialNumber + "-" + cubeCommand.getClass().getSimpleName();
        return key;
    }

    /**
     * @return the key based on the serial and channel
     *         This is can be used to find duplicated commands in the queue
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the channelUID
     */
    public ChannelUID getChannelUID() {
        return channelUID;
    }

    /**
     * @param channelUID the channelUID to set
     */
    public void setChannelUID(ChannelUID channelUID) {
        this.channelUID = channelUID;
        key = getKey(serialNumber, channelUID);
    }

    /**
     * @return the command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public void setCommand(Command command) {
        this.command = command;
    }

    /**
     * @return the {@link CubeCommand}
     */
    public CubeCommand getCubeCommand() {
        return cubeCommand;
    }

    /**
     * @return the device
     */
    public String getDeviceSerial() {
        return serialNumber;
    }

    /**
     * @param device the device to set
     */
    public void setDeviceSerial(String device) {
        this.serialNumber = device;
        key = getKey(serialNumber, channelUID);
    }

    /**
     * @return the commandText
     */
    public String getCommandText() {
        return commandText;
    }

    /**
     * @param commandText the commandText to set
     */
    public void setCommandText(String commandText) {
        this.commandText = commandText;
    }

}
