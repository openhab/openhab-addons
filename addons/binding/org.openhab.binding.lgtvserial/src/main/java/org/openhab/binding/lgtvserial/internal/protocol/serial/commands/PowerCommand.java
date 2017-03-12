package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class PowerCommand extends BaseOnOffCommand {

    protected PowerCommand(int setId) {
        super('k', 'a', setId);
    }

}
