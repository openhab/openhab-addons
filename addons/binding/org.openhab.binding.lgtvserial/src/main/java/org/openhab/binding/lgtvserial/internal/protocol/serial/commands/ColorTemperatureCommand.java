package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class ColorTemperatureCommand extends BasePercentCommand {

    protected ColorTemperatureCommand(int setId) {
        super('x', 'u', setId);
    }

}
