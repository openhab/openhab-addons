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

import java.io.IOException;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommand;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommunicator;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;
import org.openhab.binding.lgtvserial.internal.protocol.serial.responses.StringResponse;
import org.openhab.core.thing.ChannelUID;

/**
 * This class represents the base for most, if not all, LG commands.
 *
 * It consists of :
 * - the command1 character which represents the command group
 * - the command2 character which represents the command in the group
 * - the setid which represents the device to target defined by the protocol, 0 means it will target all devices
 * - updatable which represents if this command can actually change the device state, or can only retrieve it's state
 * from the device.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public abstract class BaseLGSerialCommand implements LGSerialCommand {

    private char command1;
    private char command2;
    private int setId;
    private boolean updatable;

    public BaseLGSerialCommand(char command1, char command2, int setId, boolean updatable) {
        this.command1 = command1;
        this.command2 = command2;
        this.setId = setId;
        this.updatable = true;
    }

    @Override
    public void execute(ChannelUID channel, LGSerialCommunicator comm, Object data) throws IOException {
        if (!updatable && data != null) {
            throw new IllegalArgumentException("This command cannot set any data on the TV, " //
                    + "the only allowed value is null, got : " + data);
        }
        comm.write(this, getCommand(data), channel);
    }

    @Override
    public LGSerialResponse parseResponse(String response) {
        int set = Integer.parseInt(response.substring(2, 4), 16);
        boolean success = 'O' == response.charAt(5) && 'K' == response.charAt(6);

        String data = response.substring(7);

        if (!success) {
            return new StringResponse(set, success, data);
        }

        return createResponse(set, success, data);
    }

    protected abstract LGSerialResponse createResponse(int set, boolean success, String data);

    protected String getCommand(Object data) {
        return command1 + "" + command2 + " " + String.format("%02x", setId) + " "
                + (data == null ? "FF" : computeSerialDataFrom(data));
    }

    protected abstract String computeSerialDataFrom(Object data);
}
