package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class LampFaultCheckCommand extends BaseStringCommand {

    protected LampFaultCheckCommand(int setId) {
        super('d', 'p', setId, false);
    }

}
