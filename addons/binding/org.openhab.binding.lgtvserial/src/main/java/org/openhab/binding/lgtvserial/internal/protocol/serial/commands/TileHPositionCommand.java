package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class TileHPositionCommand extends BasePercentCommand {

    protected TileHPositionCommand(int setId) {
        super('d', 'e', setId);
    }

}
