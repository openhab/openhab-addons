package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommunicator;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

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
