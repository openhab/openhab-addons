package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class SpeakerCommand extends BaseOnOffCommand {

    protected SpeakerCommand(int setId) {
        super('d', 'v', setId);
    }

}
