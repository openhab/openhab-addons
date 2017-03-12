package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class TileVSizeCommand extends BasePercentCommand {

    protected TileVSizeCommand(int setId) {
        super('d', 'h', setId);
    }

}
