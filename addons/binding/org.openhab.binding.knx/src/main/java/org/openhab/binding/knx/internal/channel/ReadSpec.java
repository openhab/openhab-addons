package org.openhab.binding.knx.internal.channel;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class ReadSpec {

    private final String dpt;
    private final List<GroupAddress> readAddresses;

    public ReadSpec(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT) {
        this(channelConfiguration != null && channelConfiguration.getDPT() != null ? channelConfiguration.getDPT()
                : defaultDPT,
                channelConfiguration != null
                        ? channelConfiguration.getReadGAs().stream().map(ReadSpec::toGroupAddress).collect(toList())
                        : Collections.emptyList());
    }

    private ReadSpec(String dpt, List<GroupAddress> readAddresses) {
        this.dpt = dpt;
        this.readAddresses = readAddresses;
    }

    public String getDPT() {
        return dpt;
    }

    public List<GroupAddress> getReadAddresses() {
        return readAddresses;
    }

    private static GroupAddress toGroupAddress(GroupAddressConfiguration ga) {
        try {
            return new GroupAddress(ga.getGA());
        } catch (KNXFormatException e) {
            return null;
        }
    }

}
