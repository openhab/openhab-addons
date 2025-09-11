/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;
import org.openhab.binding.lgtvserial.internal.protocol.serial.responses.OnOffResponse;
import org.openhab.core.library.types.OnOffType;

/**
 * This command is the base command for the On/Off type command where the state is inverted.
 * I.e. The volume mute command that reports on mute on as 00, mute off as 01.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public abstract class BaseOnOffInvertCommand extends BaseLGSerialCommand {

    protected BaseOnOffInvertCommand(char command1, char command2, int setId) {
        super(command1, command2, setId, true);
    }

    @Override
    protected String computeSerialDataFrom(Object data) {
        return data == OnOffType.ON ? "00" : "01";
    }

    @Override
    protected LGSerialResponse createResponse(int set, boolean success, String data) {
        return new OnOffResponse(set, success, data, true);
    }
}
