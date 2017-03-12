package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class TileHSizeCommand extends BasePercentCommand {

    protected TileHSizeCommand(int setId) {
        super('d', 'g', setId);
    }

}
