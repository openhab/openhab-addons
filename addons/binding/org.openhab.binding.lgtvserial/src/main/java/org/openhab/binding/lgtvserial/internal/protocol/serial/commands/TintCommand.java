package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class TintCommand extends BasePercentCommand {

    protected TintCommand(int setId) {
        super('k', 'j', setId);
    }

}
