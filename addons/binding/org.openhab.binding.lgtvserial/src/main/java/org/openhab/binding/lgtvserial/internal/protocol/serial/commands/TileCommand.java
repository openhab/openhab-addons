package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class TileCommand extends BaseStringCommand {

    protected TileCommand(int setId) {
        super('d', 'd', setId);
    }

}
