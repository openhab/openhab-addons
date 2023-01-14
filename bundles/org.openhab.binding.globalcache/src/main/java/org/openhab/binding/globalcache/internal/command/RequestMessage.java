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
package org.openhab.binding.globalcache.internal.command;

import java.util.concurrent.LinkedBlockingQueue;

import org.openhab.binding.globalcache.internal.GlobalCacheBindingConstants.CommandType;

/**
 * The {@link RequestMessage} class is responsible for storing the command to be sent to the GlobalCache
 * device and for storing whether the command is serial or not.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class RequestMessage {
    private LinkedBlockingQueue<ResponseMessage> rcvQueue;
    private String deviceCommand;
    private CommandType commandType;
    private String commandName;

    public RequestMessage(String commandName, CommandType commandType, String deviceCommand,
            LinkedBlockingQueue<ResponseMessage> rcvQueue) {
        this.commandName = commandName;
        this.commandType = commandType;
        this.deviceCommand = deviceCommand;
        this.rcvQueue = rcvQueue;
    }

    public String getDeviceCommand() {
        return deviceCommand;
    }

    public String getCommandName() {
        return commandName;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public boolean isCommand() {
        return commandType == CommandType.COMMAND;
    }

    public boolean isSerial() {
        return commandType == CommandType.SERIAL1 || commandType == CommandType.SERIAL2;
    }

    public boolean isSerial1() {
        return commandType == CommandType.SERIAL1;
    }

    public boolean isSerial2() {
        return commandType == CommandType.SERIAL2;
    }

    public LinkedBlockingQueue<ResponseMessage> getReceiveQueue() {
        return rcvQueue;
    }
}
