package org.openhab.binding.knx.internal.channel;

import org.eclipse.jdt.annotation.Nullable;

import tuwien.auto.calimero.GroupAddress;

/**
 * Response meta-data
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class ResponseSpec extends AbstractSpec {

    private final @Nullable GroupAddress groupAddress;

    public ResponseSpec(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT) {
        super(channelConfiguration, defaultDPT);
        if (channelConfiguration != null) {
            this.groupAddress = toGroupAddress(channelConfiguration.getMainGA());
        } else {
            this.groupAddress = null;
        }
    }

    public @Nullable GroupAddress getGroupAddress() {
        return groupAddress;
    }

}
