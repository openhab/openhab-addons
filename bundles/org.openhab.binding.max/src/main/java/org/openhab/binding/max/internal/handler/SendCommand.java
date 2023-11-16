/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.max.internal.command.CubeCommand;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;

/**
 * Class for sending a command.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public final class SendCommand {

    private int id;
    private static int commandId = -1;

    private @Nullable ChannelUID channelUID;
    private @Nullable Command command;
    private @Nullable CubeCommand cubeCommand;
    private String serialNumber;
    private String key;
    private String commandText;

    public SendCommand(String serialNumber, ChannelUID channelUID, Command command) {
        commandId++;
        id = commandId;
        this.serialNumber = serialNumber;
        this.channelUID = channelUID;
        this.command = command;
        key = getKey(serialNumber, channelUID);
        this.commandText = command.toString();
    }

    public SendCommand(String serialNumber, CubeCommand cubeCommand, String commandText) {
        commandId++;
        id = commandId;
        this.serialNumber = serialNumber;
        this.cubeCommand = cubeCommand;
        key = getKey(serialNumber, cubeCommand);
        this.commandText = commandText;
    }

    /**
     * Get the key based on the serial and channel
     * This is can be used to find duplicated commands in the queue
     */
    private static String getKey(String serialNumber, ChannelUID channelUID) {
        return serialNumber + "-" + channelUID.getId();
    }

    /**
     * Get the key based on the serial and channel
     * This is can be used to find duplicated commands in the queue
     */
    private static String getKey(String serialNumber, CubeCommand cubeCommand) {
        return serialNumber + "-" + cubeCommand.getClass().getSimpleName();
    }

    /**
     * @return the key based on the serial and channel
     *         This is can be used to find duplicated commands in the queue
     */
    public String getKey() {
        return key;
    }

    public int getId() {
        return id;
    }

    public @Nullable ChannelUID getChannelUID() {
        return channelUID;
    }

    public void setChannelUID(ChannelUID channelUID) {
        this.channelUID = channelUID;
        key = getKey(serialNumber, channelUID);
    }

    public @Nullable Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public @Nullable CubeCommand getCubeCommand() {
        return cubeCommand;
    }

    public String getDeviceSerial() {
        return serialNumber;
    }

    public void setDeviceSerial(String device) {
        this.serialNumber = device;
        final ChannelUID channelUID = this.channelUID;
        if (channelUID != null) {
            key = getKey(serialNumber, channelUID);
        }
    }

    public String getCommandText() {
        return commandText;
    }

    public void setCommandText(String commandText) {
        this.commandText = commandText;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("id: ").append(id).append(", channelUID: ").append(channelUID).append(", command: ")
                .append(command).append(", cubeCommand: ").append(cubeCommand).append(", serialNumber: ")
                .append(serialNumber).append(", key: ").append(key).append(", commandText: ").append(commandText)
                .toString();
    }
}
