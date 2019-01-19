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
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommunicator;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

/**
 * This command handles the IR key code M/C command.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class IRKeyCodeCommand extends BaseStringCommand {

    protected IRKeyCodeCommand(int setId) {
        super('m', 'c', setId);
    }

    @Override
    public void execute(ChannelUID channel, LGSerialCommunicator comm, Object data) throws IOException {
        if (data != null) {
            super.execute(channel, comm, data);
        }
    }

    @Override
    public LGSerialResponse parseResponse(String response) {
        return null;
    }

}
