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
package org.openhab.binding.phc.internal.handler;

import org.openhab.core.types.Command;

/**
 * Object to save a whole message.
 *
 * @author Jonas Hohaus - Initial contribution
 */
class QueueObject {
    private final String moduleType;
    private final byte moduleAddress;
    private final byte channel;
    private final Command command;

    private short time;

    public QueueObject(String moduleType, byte moduleAddress, byte channel, Command command) {
        this.moduleType = moduleType;
        this.moduleAddress = moduleAddress;
        this.channel = channel;
        this.command = command;
    }

    public QueueObject(String moduleType, int moduleAddress, String channel, Command command) {
        this.moduleType = moduleType;
        this.moduleAddress = (byte) moduleAddress;
        this.channel = Byte.parseByte(channel);
        this.command = command;
    }

    public QueueObject(String moduleType, int moduleAddress, String channel, Command command, short time) {
        this(moduleType, moduleAddress, channel, command);
        this.time = time;
    }

    public String getModuleType() {
        return moduleType;
    }

    public byte getModuleAddress() {
        return moduleAddress;
    }

    public byte getChannel() {
        return channel;
    }

    public Command getCommand() {
        return command;
    }

    public short getTime() {
        return time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("moduleType: ");
        sb.append(moduleType);
        sb.append(", moduleAddress: ");
        sb.append(moduleAddress);
        sb.append(", channel: ");
        sb.append(channel);

        return sb.toString();
    }
}
