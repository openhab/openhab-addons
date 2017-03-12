package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class VolumeCommand extends BasePercentCommand {

    protected VolumeCommand(int setId) {
        super('k', 'f', setId);
    }

}
