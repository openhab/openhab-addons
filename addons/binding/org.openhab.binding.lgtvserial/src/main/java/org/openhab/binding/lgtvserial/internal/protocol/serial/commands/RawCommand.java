package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommand;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommunicator;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

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
