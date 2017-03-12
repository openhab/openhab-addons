package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class ScreenMuteCommand extends BaseStringCommand {

    protected ScreenMuteCommand(int setId) {
        super('k', 'd', setId);
    }

}
