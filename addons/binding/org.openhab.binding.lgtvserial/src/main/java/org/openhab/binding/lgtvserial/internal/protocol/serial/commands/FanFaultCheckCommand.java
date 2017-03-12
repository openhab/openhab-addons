package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class FanFaultCheckCommand extends BaseStringCommand {

    protected FanFaultCheckCommand(int setId) {
        super('d', 'w', setId, false);
    }

}
