package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class ColorTemperature2Command extends BasePercentCommand {

    protected ColorTemperature2Command(int setId) {
        super('x', 'u', setId);
    }

}
