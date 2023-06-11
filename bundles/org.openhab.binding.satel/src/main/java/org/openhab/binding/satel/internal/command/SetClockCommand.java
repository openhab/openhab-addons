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
package org.openhab.binding.satel.internal.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Command class for command to set RTC clock.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SetClockCommand extends ControlCommand {

    public static final byte COMMAND_CODE = (byte) 0x8e;

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Creates new command class instance.
     *
     * @param dateTime date and time to set
     * @param userCode code of the user on behalf the control is made
     */
    public SetClockCommand(LocalDateTime dateTime, String userCode) {
        super(COMMAND_CODE, getDateTimeBytes(dateTime), userCode);
    }

    private static byte[] getDateTimeBytes(LocalDateTime dateTime) {
        return DATETIME_FORMAT.format(dateTime).getBytes();
    }
}
