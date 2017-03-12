package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

// TODO
public class SleepTimeCommand extends BaseDecimalCommand {

    protected SleepTimeCommand(int setId) {
        super('f', 'f', setId);
    }

}
