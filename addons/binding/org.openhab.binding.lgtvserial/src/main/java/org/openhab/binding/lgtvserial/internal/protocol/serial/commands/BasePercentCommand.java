/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

/**
 * This command is the base command to handle percent type commands (0-100) in hex format on the wire.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public abstract class BasePercentCommand extends BaseLGSerialCommand {

    protected BasePercentCommand(char command1, char command2, int setId) {
        super(command1, command2, setId, true);
    }

    @Override
    protected String computeSerialDataFrom(Object data) {
        return Integer.toHexString(((PercentType) data).intValue());
    }

    @Override
    protected LGSerialResponse createResponse(int set, boolean success, String data) {
        String decimalValue = Integer.toString(Integer.parseInt(data, 16));
        return new PercentResponse(set, success, PercentType.valueOf(decimalValue));
    }

}
