package org.openhab.binding.onecta.internal.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openhab.core.thing.Thing;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
public class ChannelsRefreshDelay {
    protected class ChannelDelay {
        private String channel;
        private Long endDelay;

        public ChannelDelay(String channel, Long delay) {
            this.channel = channel;
            this.endDelay = new Date().getTime() + delay;
        }

        public String getChannel() {
            return channel;
        }

        public Long getEndDelay() {
            return endDelay;
        }
    }

    private List<ChannelDelay> channelRefreshDelay = new ArrayList<>();

    private String channel;
    private long delay;
    private Thing thing;

    public ChannelsRefreshDelay(Long delay) {
        this.delay = delay;
    }

    public void add(String channel) {
        ChannelDelay channelDelay = findChannel(channel);
        if (channelDelay != null) {
            channelRefreshDelay.remove(channelDelay);
        }
        channelRefreshDelay.add(new ChannelDelay(channel, this.delay));
    }

    private ChannelDelay findChannel(String channel) {
        return channelRefreshDelay.stream().filter(channelDelay -> channel.equals(channelDelay.getChannel().toString()))
                .findFirst().orElse(null);
    }

    public Boolean isDelayPassed(String channel) {
        ChannelDelay channelDelay = findChannel(channel);
        if (channelDelay == null || channelDelay.getEndDelay() < new Date().getTime()) {
            channelRefreshDelay.remove(channelDelay);
            return true;
        } else {
            return false;
        }
    }
}
