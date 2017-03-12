package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class PowerIndicatorCommand extends BaseOnOffCommand {

    protected PowerIndicatorCommand(int setId) {
        super('f', 'o', setId);
    }

}
