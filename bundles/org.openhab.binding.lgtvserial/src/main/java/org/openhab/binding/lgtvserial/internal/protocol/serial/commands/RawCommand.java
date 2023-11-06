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
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import java.io.IOException;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommand;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommunicator;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;
import org.openhab.core.thing.ChannelUID;

/**
 * This command handles a raw command to be sent to the device.
 *
 * This proves useful when a command is not implemented yet to be able to send commands yourselves with the proper
 * format.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class RawCommand implements LGSerialCommand {

    @Override
    public LGSerialResponse parseResponse(String response) {
        return null;
    }

    @Override
    public void execute(ChannelUID channel, LGSerialCommunicator comm, Object data) throws IOException {
        // No read request possible
        if (data != null) {
            comm.write(this, data.toString(), channel);
        }
    }
}
