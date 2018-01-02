package org.openhab.binding.knx.internal.channel;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class ListenSpec {

    private final String dpt;
    private final List<GroupAddress> listenAddresses;

    public ListenSpec(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT) {
        this(channelConfiguration != null && channelConfiguration.getDPT() != null ? channelConfiguration.getDPT()
                : defaultDPT,
                channelConfiguration != null
                        ? channelConfiguration.getListenGAs().stream().map(ListenSpec::toGroupAddress).collect(toList())
                        : Collections.emptyList());
    }

    private ListenSpec(String dpt, List<GroupAddress> listenAddresses) {
        this.dpt = dpt;
        this.listenAddresses = listenAddresses;
    }

    public String getDPT() {
        return dpt;
    }

    public List<GroupAddress> getListenAddresses() {
        return listenAddresses;
    }

    private static GroupAddress toGroupAddress(GroupAddressConfiguration ga) {
        try {
            return new GroupAddress(ga.getGA());
        } catch (KNXFormatException e) {
            return null;
        }
    }

}
