package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class TileIdSetCommand extends BaseStringCommand {

    protected TileIdSetCommand(int setId) {
        super('d', 'i', setId);
    }

}
