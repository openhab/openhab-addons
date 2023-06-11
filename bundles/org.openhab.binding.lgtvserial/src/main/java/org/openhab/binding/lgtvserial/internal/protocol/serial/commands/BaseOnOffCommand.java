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
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;
import org.openhab.binding.lgtvserial.internal.protocol.serial.responses.OnOffResponse;
import org.openhab.core.library.types.OnOffType;

/**
 * This command is the base command for the On/Off type command which translates to 00/01 on the wire.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public abstract class BaseOnOffCommand extends BaseLGSerialCommand {

    protected BaseOnOffCommand(char command1, char command2, int setId) {
        super(command1, command2, setId, true);
    }

    @Override
    protected String computeSerialDataFrom(Object data) {
        return data == OnOffType.ON ? "01" : "00";
    }

    @Override
    protected LGSerialResponse createResponse(int set, boolean success, String data) {
        return new OnOffResponse(set, success, data);
    }
}
