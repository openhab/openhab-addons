package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

// TODO Have no clue yet how to implement this
public class TimeCommand extends BaseStringCommand {

    protected TimeCommand(int setId) {
        super('f', 'a', setId);
    }

}
