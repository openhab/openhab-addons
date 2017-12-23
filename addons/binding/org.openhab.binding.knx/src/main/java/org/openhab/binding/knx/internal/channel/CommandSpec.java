package org.openhab.binding.knx.internal.channel;

import org.eclipse.smarthome.core.types.Command;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class CommandSpec {

    private final Command command;
    private final String dpt;
    private final GroupAddress groupAddress;

    private CommandSpec(String dpt, GroupAddress groupAddress, Command command) {
        super();
        this.dpt = dpt;
        this.groupAddress = groupAddress;
        this.command = command;
    }

    public CommandSpec(ChannelConfiguration channelConfiguration, String defaultDPT, Command command)
            throws KNXFormatException {
        this(channelConfiguration.getDPT() != null ? channelConfiguration.getDPT() : defaultDPT,
                new GroupAddress(channelConfiguration.getMainGA().getGA()), command);
    }

    public Command getCommand() {
        return command;
    }

    public String getDpt() {
        return dpt;
    }

    public GroupAddress getGroupAddress() {
        return groupAddress;
    }

}
