package org.openhab.binding.knx.internal.channel;

import org.eclipse.jdt.annotation.Nullable;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class ResponseSpec {

    private final String dpt;
    private final @Nullable GroupAddress groupAddress;

    public ResponseSpec(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT) {
        this(channelConfiguration != null && channelConfiguration.getDPT() != null ? channelConfiguration.getDPT()
                : defaultDPT, channelConfiguration != null ? toGroupAddress(channelConfiguration.getMainGA()) : null);
    }

    private ResponseSpec(String dpt, GroupAddress groupAddress) {
        this.dpt = dpt;
        this.groupAddress = groupAddress;
    }

    public String getDpt() {
        return dpt;
    }

    public @Nullable GroupAddress getGroupAddress() {
        return groupAddress;
    }

    private static GroupAddress toGroupAddress(GroupAddressConfiguration ga) {
        try {
            return new GroupAddress(ga.getGA());
        } catch (KNXFormatException e) {
            return null;
        }
    }

}
