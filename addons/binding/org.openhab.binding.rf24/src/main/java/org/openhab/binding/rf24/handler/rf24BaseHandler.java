package org.openhab.binding.rf24.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rf24.handler.channel.AbstractChannel;
import org.openhab.binding.rf24.handler.channel.Channel;
import org.openhab.binding.rf24.handler.channel.Channel.Updatable;
import org.openhab.binding.rf24.internal.serial.ArduinoSerial;
import org.openhab.binding.rf24.handler.channel.ChannelGuard;
import org.openhab.binding.rf24.handler.channel.Dht11Channel;
import org.openhab.binding.rf24.handler.channel.OnOffChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import pl.grzeslowski.smarthome.common.io.id.HardwareId;
import pl.grzeslowski.smarthome.common.io.id.IdUtils;

public class rf24BaseHandler extends BaseThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(rf24BaseHandler.class);
    private static final Supplier<Integer> MESSAGE_ID_SUPPLIER = new Supplier<Integer>() {
        private final AtomicInteger id = new AtomicInteger(1);

        @Override
        public Integer get() {
            return id.getAndIncrement();
        }
    };

    private final List<Channel> channels = new ArrayList<>();

    public rf24BaseHandler(Thing thing, IdUtils idUtils, ArduinoSerial arduinoSerial, HardwareId hardwareId) {
        super(thing);

        Updatable updatable = new Updatable() {

            @Override
            public void updateState(ChannelUID channelUID, State state) {
                rf24BaseHandler.this.updateState(channelUID, state);
            }
        };

        addChannel(arduinoSerial, new OnOffChannel(idUtils, arduinoSerial, updatable, MESSAGE_ID_SUPPLIER, hardwareId));
        addChannel(arduinoSerial, new Dht11Channel(idUtils, arduinoSerial, updatable, MESSAGE_ID_SUPPLIER, hardwareId));
    }

    private void addChannel(ArduinoSerial arduinoSerial, AbstractChannel channel) {
        arduinoSerial.addListener(channel);
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

    @Override
    public void dispose() {
        super.dispose();
        channels.stream().forEach(channel -> channel.close());
    }
}
