package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class ColorTemperatureCommand extends BaseStringCommand {

    protected ColorTemperatureCommand(int setId) {
        super('k', 'u', setId);
    }

}
