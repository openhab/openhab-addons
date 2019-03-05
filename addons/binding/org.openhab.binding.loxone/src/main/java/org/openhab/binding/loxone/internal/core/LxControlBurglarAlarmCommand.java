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
package org.openhab.binding.loxone.internal.core;

import java.io.IOException;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

/**
 * Loxone Control that performs a command on the Burglar Alarm
 *
 * @author Michael Mattan - Initial contribution
 *
 */
public class LxControlBurglarAlarmCommand extends LxControlSwitch {

    private final String command;
    private final String commandName;
    private final LxControlBurglarAlarm alarm;

    public LxControlBurglarAlarmCommand(LxUuid uuid, LxJsonControl jsonControl, LxContainer room, LxCategory category,
            String command, String commandName, LxControlBurglarAlarm alarm) {

        super(null, uuid, jsonControl, room, category);
        this.command = command;
        this.commandName = commandName;
        this.alarm = alarm;
    }

    /**
     * executes the command
     */
    @Override
    public void on() throws IOException {
        this.alarm.executeCommand(this.command);
    }

    /**
     * turns the alarm off
     */
    @Override
    public void off() throws IOException {
        this.alarm.off();
    }

    public String getCommandName() {
        return this.commandName;
    }

}
