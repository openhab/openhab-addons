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
import org.openhab.binding.lgtvserial.internal.protocol.serial.responses.DecimalResponse;
import org.openhab.core.library.types.DecimalType;

/**
 * This command is the base command to handle decimal type commands in hex format on the wire.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public abstract class BaseDecimalCommand extends BaseLGSerialCommand {

    /**
     * Create a command.
     *
     * @param command1 Command category
     * @param command2 Command key
     * @param setId TV Set id this command is tied to
     * @param updatable Define if this command is one that can update the TV or can only ever be a read status command.
     */
    protected BaseDecimalCommand(char command1, char command2, int setId, boolean updatable) {
        super(command1, command2, setId, updatable);
    }

    /**
     * Create a command that can update the TV.
     *
     * @param command1 Command category
     * @param command2 Command key
     * @param setId TV Set id this command is tied to
     */

    protected BaseDecimalCommand(char command1, char command2, int setId) {
        super(command1, command2, setId, true);
    }

    @Override
    protected String computeSerialDataFrom(Object data) {
        return String.format("%02x", ((DecimalType) data).intValue());
    }

    @Override
    protected LGSerialResponse createResponse(int set, boolean success, String data) {
        String decimalValue = Integer.toString(Integer.parseInt(data, 16));
        return new DecimalResponse(set, success, DecimalType.valueOf(decimalValue));
    }
}
