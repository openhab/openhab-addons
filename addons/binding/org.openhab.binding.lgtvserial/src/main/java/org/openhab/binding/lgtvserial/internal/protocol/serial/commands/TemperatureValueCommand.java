package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class TemperatureValueCommand extends BaseDecimalCommand {

    protected TemperatureValueCommand(int setId) {
        super('d', 'n', setId, false);
    }

}
