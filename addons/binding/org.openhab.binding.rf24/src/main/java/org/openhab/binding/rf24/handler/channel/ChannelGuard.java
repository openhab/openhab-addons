package org.openhab.binding.rf24.handler.channel;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

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
    public Optional<Consumer<Updatable>> process(ChannelUID channelUID, Command command) {
        Preconditions.checkNotNull(channelUID);
        Preconditions.checkNotNull(command);
        if (isGoodChannel(channelUID)) {
            return channel.process(channelUID, command);
        } else {
            return Optional.empty();
        }
    }

    private boolean isGoodChannel(ChannelUID channelUID) {
        for (String supportedChannel : whatChannelIdCanProcess()) {
            if (Objects.equal(channelUID.getId(), supportedChannel)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", ChannelGuard.class.getSimpleName(), channel.toString());
    }
}
