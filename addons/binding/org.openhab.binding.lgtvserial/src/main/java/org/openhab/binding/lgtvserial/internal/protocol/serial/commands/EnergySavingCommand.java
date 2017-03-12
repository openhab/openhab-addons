package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommand;

public class EnergySavingCommand extends BaseStringCommand implements LGSerialCommand {

    protected EnergySavingCommand(int setId) {
        super('j', 'q', setId);
    }

}
