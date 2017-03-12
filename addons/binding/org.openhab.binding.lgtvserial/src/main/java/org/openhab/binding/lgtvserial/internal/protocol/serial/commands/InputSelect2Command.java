package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class InputSelect2Command extends BaseStringCommand {

    protected InputSelect2Command(int setId) {
        super('x', 'b', setId);
    }

}
