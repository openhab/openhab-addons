package org.openhab.binding.rf24.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.handler.channel.Channel;
import org.openhab.binding.rf24.handler.channel.ChannelGuard;
import org.openhab.binding.rf24.handler.channel.Dht11Channel;
import org.openhab.binding.rf24.handler.channel.OnOffChannel;
import org.openhab.binding.rf24.wifi.WiFi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import pl.grzeslowski.smarthome.rpi.wifi.help.Pipe;

public class rf24BaseHandler extends BaseThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(rf24BaseHandler.class);
    private static final int DEVICE_ID = 1;
    private static final AtomicInteger MESSAGE_ID_SUPPLIER = new AtomicInteger();

    private final List<Channel> channels = new ArrayList<>();

    private static final Pipe findPipe(Thing thing) {
        Configuration conf = thing.getConfiguration();
        if (conf.containsKey(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION)) {
            return new Pipe((String) thing.getConfiguration().get(rf24BindingConstants.RECIVER_PIPE_CONFIGURATION));
        } else {
            throw new RuntimeException("Thing does not have recive pipe in configuration!");
        }
    }

    public rf24BaseHandler(Thing thing, WiFi wifi) {
        super(thing);
        Pipe pipe = findPipe(thing);
        channels.add(ChannelGuard.of(new OnOffChannel(wifi, MESSAGE_ID_SUPPLIER, DEVICE_ID, pipe)));
        channels.add(ChannelGuard.of(new Dht11Channel(wifi, MESSAGE_ID_SUPPLIER, DEVICE_ID, pipe)));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        for (Channel channel : channels) {
            Set<String> supertedChannelIds = channel.whatChannelIdCanProcess();
            for (String channelId : supertedChannelIds) {
                if (Objects.equal(channelId, channelUID.getId())) {
                    logger.debug(String.format("Running on Channel %s command %s.", channel, command));
                    channel.process(channelUID, command).ifPresent(consumer -> consumer
                            .accept((channelUID1, state) -> rf24BaseHandler.this.updateState(channelUID1, state)));
                }
            }

        }
    }
}
