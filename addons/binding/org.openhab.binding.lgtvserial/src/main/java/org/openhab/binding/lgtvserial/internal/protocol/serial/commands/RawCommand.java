/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommand;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommunicator;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

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
