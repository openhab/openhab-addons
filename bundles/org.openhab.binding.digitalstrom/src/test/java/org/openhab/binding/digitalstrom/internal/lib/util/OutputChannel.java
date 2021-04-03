package org.openhab.binding.digitalstrom.internal.lib.util;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;

public class OutputChannel {
    public OutputChannel(OutputChannelEnum outputChannel) {
        super();
        this.channelID = outputChannel.getChannelId();
        this.name = outputChannel.getName();
        this.id = outputChannel.getName();
        this.index = outputChannel.getChannelId();
    }

    int channelID;
    String name;
    String id;
    int index;
}
