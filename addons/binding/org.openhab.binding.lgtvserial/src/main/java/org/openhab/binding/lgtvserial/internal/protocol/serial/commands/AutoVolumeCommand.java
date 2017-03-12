package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class AutoVolumeCommand extends BaseOnOffCommand {

    protected AutoVolumeCommand(int setId) {
        super('d', 'u', setId);
    }

}
