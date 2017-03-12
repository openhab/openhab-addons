package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class TileVPositionCommand extends BasePercentCommand {

    protected TileVPositionCommand(int setId) {
        super('d', 'v', setId);
    }

}
