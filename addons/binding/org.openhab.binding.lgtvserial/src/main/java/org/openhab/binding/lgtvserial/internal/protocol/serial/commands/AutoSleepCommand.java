package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class AutoSleepCommand extends BaseDecimalCommand {

    protected AutoSleepCommand(int setId) {
        super('f', 'g', setId);
    }

}
