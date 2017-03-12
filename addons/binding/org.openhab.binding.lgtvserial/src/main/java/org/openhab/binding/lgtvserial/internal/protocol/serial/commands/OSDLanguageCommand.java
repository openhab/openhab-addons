package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class OSDLanguageCommand extends BaseStringCommand {

    protected OSDLanguageCommand(int setId) {
        super('f', 'i', setId);
    }

}
