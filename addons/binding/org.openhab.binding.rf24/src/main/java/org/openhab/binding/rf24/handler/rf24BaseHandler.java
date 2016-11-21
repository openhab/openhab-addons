package org.openhab.binding.rf24.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.rf24.handler.channel.Channel;
import org.openhab.binding.rf24.handler.channel.ChannelGuard;
import org.openhab.binding.rf24.handler.channel.Dht11Channel;
import org.openhab.binding.rf24.handler.channel.OnOffChannel;
import org.openhab.binding.rf24.wifi.X;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

public class rf24BaseHandler extends BaseThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(rf24BaseHandler.class);
    private static final AtomicInteger MESSAGE_ID_SUPPLIER = new AtomicInteger();

    private final List<Channel> channels = new ArrayList<>();

    public rf24BaseHandler(Thing thing, X x, Pipe pipe) {
        super(thing);
        channels.add(ChannelGuard.of(new OnOffChannel(x, MESSAGE_ID_SUPPLIER, pipe)));
        channels.add(ChannelGuard.of(new Dht11Channel(x, MESSAGE_ID_SUPPLIER, pipe)));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // @formatter:off
        for (Channel channel : channels) {
            Set<String> supertedChannelIds = channel.whatChannelIdCanProcess();
            for (String channelId : supertedChannelIds) {
                if (Objects.equal(channelId, channelUID.getId())) {
                    logger.debug(String.format("Running on Channel %s command %s.", channel, command));
                    channel
                        .process(channelUID, command)
                        .ifPresent(consumer ->
                            consumer.accept((channelUID1, state) ->
                                rf24BaseHandler.this.updateState(channelUID1, state)
                            )
                         );
                }
            }

        }
        // @formatter:on
    }
}
