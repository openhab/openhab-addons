package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

// TODO
public class TimeCommand extends BaseStringCommand {

    protected TimeCommand(int setId) {
        super('f', 'a', setId);
    }

}
