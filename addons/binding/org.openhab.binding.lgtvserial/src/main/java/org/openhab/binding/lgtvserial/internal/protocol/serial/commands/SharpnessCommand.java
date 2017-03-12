package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class SharpnessCommand extends BasePercentCommand {

    protected SharpnessCommand(int setId) {
        super('k', 'k', setId);
    }

}
