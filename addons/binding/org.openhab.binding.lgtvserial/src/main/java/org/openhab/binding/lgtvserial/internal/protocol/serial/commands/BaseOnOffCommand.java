/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;
import org.openhab.binding.lgtvserial.internal.protocol.serial.responses.OnOffResponse;

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
