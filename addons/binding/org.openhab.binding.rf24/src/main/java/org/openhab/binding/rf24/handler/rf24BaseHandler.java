package org.openhab.binding.rf24.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rf24.handler.channel.AbstractChannel;
import org.openhab.binding.rf24.handler.channel.Channel;
import org.openhab.binding.rf24.handler.channel.Channel.Updatable;
import org.openhab.binding.rf24.handler.channel.ChannelGuard;
import org.openhab.binding.rf24.handler.channel.Dht11Channel;
import org.openhab.binding.rf24.handler.channel.OnOffChannel;
import org.openhab.binding.rf24.wifi.WifiOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

public class rf24BaseHandler extends BaseThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(rf24BaseHandler.class);
    private static final AtomicInteger MESSAGE_ID_SUPPLIER = new AtomicInteger();

    private final List<Channel> channels = new ArrayList<>();

    public rf24BaseHandler(Thing thing, IdUtils idUtils, WifiOperator wifiOperator, Pipe pipe) {
        super(thing);

        Updatable updatable = new Updatable() {

            @Override
            public void updateState(ChannelUID channelUID, State state) {
                rf24BaseHandler.this.updateState(channelUID, state);
            }
        };

        addChannel(wifiOperator, new OnOffChannel(idUtils, wifiOperator, updatable, MESSAGE_ID_SUPPLIER, pipe));
        addChannel(wifiOperator, new Dht11Channel(idUtils, wifiOperator, updatable, MESSAGE_ID_SUPPLIER, pipe));
    }

    private void addChannel(WifiOperator wifiOperator, AbstractChannel channel) {
        wifiOperator.addToNotify(channel);
        channels.add(ChannelGuard.of(channel));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // @formatter:off
        for (Channel channel : channels) {
            Set<String> supertedChannelIds = channel.whatChannelIdCanProcess();
            for (String channelId : supertedChannelIds) {
                if (Objects.equal(channelId, channelUID.getId())) {
                    logger.debug(String.format("Running on Channel %s command %s.", channel, command));
                    channel.process(channelUID, command);
                }
            }

        }
        // @formatter:on
    }
}
