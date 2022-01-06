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
import org.openhab.binding.lgtvserial.internal.protocol.serial.responses.PercentResponse;
import org.openhab.core.library.types.PercentType;

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
