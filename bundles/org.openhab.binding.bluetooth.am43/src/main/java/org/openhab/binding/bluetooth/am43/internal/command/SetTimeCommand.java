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
package org.openhab.binding.bluetooth.am43.internal.command;

import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SetTimeCommand} set the current time to the motor, which it uses for scheduled activation.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class SetTimeCommand extends AM43Command {

    private static final byte COMMAND = (byte) 0x14;

    public SetTimeCommand() {
        super(COMMAND, createContent());
    }

    private static byte[] createContent() {
        Calendar instance = Calendar.getInstance();
        int hour = instance.get(Calendar.HOUR);
        if (instance.get(Calendar.AM_PM) != 0) {
            hour += 12;
        }
        int minute = instance.get(Calendar.MINUTE);
        int second = instance.get(Calendar.SECOND);
        int dayOfWeek = instance.get(Calendar.DAY_OF_WEEK) - 1;
        return new byte[] { (byte) dayOfWeek, (byte) hour, (byte) minute, (byte) second };
    }
}
