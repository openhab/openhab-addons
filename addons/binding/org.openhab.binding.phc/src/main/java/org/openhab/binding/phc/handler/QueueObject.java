/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.phc.handler;

import org.eclipse.smarthome.core.types.Command;

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

    private int counter;
    private short upDownTime;

    public QueueObject(String moduleType, String moduleAddress, String channel, Command command) {
        this.moduleType = moduleType;
        this.moduleAddress = Byte.parseByte(moduleAddress, 2);
        this.channel = Byte.parseByte(channel);
        this.command = command;
    }

    public QueueObject(String moduleType, String moduleAddress, String channel, Command command, int counter,
            short upDownTime) {
        this.moduleType = moduleType;
        this.moduleAddress = Byte.parseByte(moduleAddress, 2);
        this.channel = Byte.parseByte(channel);
        this.command = command;
        this.counter = counter;
        this.upDownTime = upDownTime;
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

    public void increaseCounter() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }

    public short getUpDownTime() {
        return upDownTime;
    }
}
