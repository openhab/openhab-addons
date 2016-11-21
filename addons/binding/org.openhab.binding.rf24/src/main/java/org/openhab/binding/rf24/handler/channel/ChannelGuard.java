package org.openhab.binding.rf24.handler.channel;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ChannelGuard implements Channel {
    private final Channel channel;

    public static Channel of(Channel channel) {
        return new ChannelGuard(channel);
    }

    private ChannelGuard(Channel channel) {
        this.channel = Preconditions.checkNotNull(channel);
    }

    @Override
    public Set<String> whatChannelIdCanProcess() {
        return channel.whatChannelIdCanProcess();
    }

    @Override
    public void process(ChannelUID channelUID, Command command) {
        Preconditions.checkNotNull(channelUID);
        Preconditions.checkNotNull(command);
        if (isGoodChannel(channelUID)) {
            channel.process(channelUID, command);
        }
    }

    private boolean isGoodChannel(ChannelUID channelUID) {
        return whatChannelIdCanProcess().stream()
                .filter(supportedChannel -> Objects.equal(channelUID.getId(), supportedChannel)).findAny().isPresent();
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", ChannelGuard.class.getSimpleName(), channel.toString());
    }
}
