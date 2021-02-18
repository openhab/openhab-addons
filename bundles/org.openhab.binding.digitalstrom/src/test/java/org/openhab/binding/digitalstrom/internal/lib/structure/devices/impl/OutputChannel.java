package org.openhab.binding.digitalstrom.internal.lib.structure.devices.impl;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;

public class OutputChannel {
    public OutputChannel(OutputChannelEnum outputChannel) {
        super();
        this.channelID = outputChannel.getChannel();
        this.name = outputChannel.getName();
        this.id = outputChannel.getName();
        this.index = outputChannel.getChannel();
    }

    int channelID;
    String name;
    String id;
    int index;
}
