package org.openhab.binding.knx.internal.channel;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Type;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class CommandSpec {

    private final Type command;
    private final String dpt;
    private final @Nullable GroupAddress groupAddress;

    private CommandSpec(String dpt, GroupAddress groupAddress, Type command) {
        this.dpt = dpt;
        this.groupAddress = groupAddress;
        this.command = command;
    }

    public CommandSpec(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT, Type command)
            throws KNXFormatException {
        this(channelConfiguration != null && channelConfiguration.getDPT() != null ? channelConfiguration.getDPT()
                : defaultDPT,
                channelConfiguration != null ? new GroupAddress(channelConfiguration.getMainGA().getGA()) : null,
                command);
    }

    public Type getCommand() {
        return command;
    }

    public String getDpt() {
        return dpt;
    }

    public @Nullable GroupAddress getGroupAddress() {
        return groupAddress;
    }

}
