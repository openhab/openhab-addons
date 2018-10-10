/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jsupla.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.javatuples.Pair;
import org.openhab.binding.jsupla.internal.ChannelCallback;
import org.openhab.binding.jsupla.internal.ChannelValueToState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.ChannelValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.ChannelValueSwitch;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.DecimalValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.OnOff;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.PercentValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.RgbValue;
import pl.grzeslowski.jsupla.protocoljava.api.entities.ds.DeviceChannel;
import pl.grzeslowski.jsupla.protocoljava.api.entities.ds.DeviceChannels;
import pl.grzeslowski.jsupla.protocoljava.api.entities.sd.ChannelNewValue;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.BRIDGE_UNINITIALIZED;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.NONE;
import static reactor.core.publisher.Flux.just;

/**
 * The {@link SuplaDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class SuplaDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SuplaDeviceHandler.class);

    private pl.grzeslowski.jsupla.server.api.@Nullable Channel suplaChannel;
    private final Object channelLock = new Object();

    private final Map<ChannelUID, Integer> channelUIDS = new HashMap<>();

    public SuplaDeviceHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings({"unused", "null"})
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final Integer channelNumber = channelUIDS.get(channelUID);
        if (channelNumber == null) {
            logger.debug("There is no channel number for channelUID={}", channelUID);
            return;
        }
        final ChannelValue channelValue = buildChannelValue(command);
        if (channelValue == null) {
            logger.debug("Don't know how to handle command {} sent to {}", command, channelUID);
            return;
        }
        final ChannelNewValue channelNewValue = new ChannelNewValue(
                1,
                channelNumber,
                100,
                channelValue
        );
        suplaChannel.write(just(channelNewValue))
                .subscribe(
                        date -> logger.debug("Changed value of channel for {} command {}, {}",
                                channelUID, command, date.format(ISO_DATE_TIME)),
                        ex -> logger.debug("Couldn't Change value of channel for {} command {}",
                                channelUID, command, ex)
                );
    }

    private @Nullable ChannelValue buildChannelValue(final Command command) {
        if (command instanceof OnOffType) {
            final OnOffType onOff = (OnOffType) command;
            if (onOff == OnOffType.ON) {
                return OnOff.ON;
            } else {
                return OnOff.OFF;
            }
        } else if (command instanceof HSBType) {
            final HSBType hsb = (HSBType) command;
            return new RgbValue(
                    hsb.getBrightness().intValue(),
                    255, // TODO I don't know if this is correct
                    hsb.getRed().intValue(),
                    hsb.getGreen().intValue(),
                    hsb.getBlue().intValue());
        } else if (command instanceof OpenClosedType) {
            final OpenClosedType onOff = (OpenClosedType) command;
            if (onOff == OpenClosedType.OPEN) {
                return OnOff.ON;
            } else {
                return OnOff.OFF;
            }
        } else if (command instanceof PercentType) {
            final PercentType percent = (PercentType) command;
            return new PercentValue(percent.intValue());
        } else if (command instanceof DecimalType) {
            final DecimalType decimal = (DecimalType) command;
            return new DecimalValue(decimal.toBigDecimal());
        }

        logger.debug("Don't know how to handle this command {}!", command);
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        if (getBridge() == null) {
            logger.debug("No bridge for thing with UID {}", thing.getUID());
            updateStatus(
                    OFFLINE,
                    BRIDGE_UNINITIALIZED,
                    "There is no bridge for this thing. Remove it and add it again.");
            return;
        }

        synchronized (channelLock) {
            if (suplaChannel == null) {
                updateStatus(OFFLINE, NONE, "Channel in server is not yet opened");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    public void setSuplaChannel(final pl.grzeslowski.jsupla.server.api.Channel suplaChannel) {
        synchronized (channelLock) {
            this.suplaChannel = suplaChannel;
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @SuppressWarnings("deprecation")
    public void setChannels(final DeviceChannels deviceChannels) {
        logger.debug("Registering channels {}", deviceChannels);
        final List<Channel> channels = deviceChannels.getChannels()
                                               .stream()
                                               .sorted(Comparator.comparingInt(DeviceChannel::getNumber))
                                               .map(this::createChannel)
                                               .collect(Collectors.toList());
        updateChannels(channels);
        deviceChannels.getChannels()
                .stream()
                .map(this::channelForUpdate)
                .forEach(pair -> updateState(pair.getValue0(), pair.getValue1()));
    }

    @SuppressWarnings("deprecation")
    private Pair<ChannelUID, State> channelForUpdate(final DeviceChannel deviceChannel) {
        return Pair.with(
                createChannelUid(deviceChannel.getNumber()),
                findState(deviceChannel.getValue())
        );
    }

    private ChannelUID createChannelUid(final int channelNumber) {
        return new ChannelUID(getThing().getUID(), valueOf(channelNumber));
    }

    private State findState(ChannelValue value) {
        final ChannelValueSwitch<State> valueSwitch = new ChannelValueSwitch<>(new ChannelValueToState());
        return valueSwitch.doSwitch(value);
    }

    public void updateStatus(final int channelNumber, final ChannelValue channelValue) {
        final ChannelUID channelUid = createChannelUid(channelNumber);
        final State state = findState(channelValue);
        updateState(channelUid, state);
    }

    @SuppressWarnings("deprecation")
    private Channel createChannel(final DeviceChannel deviceChannel) {
        final ChannelCallback channelCallback = new ChannelCallback(getThing().getUID(), deviceChannel.getNumber());
        final ChannelValueSwitch<Channel> channelValueSwitch = new ChannelValueSwitch<>(channelCallback);
        final Channel channel = channelValueSwitch.doSwitch(deviceChannel.getValue());
        channelUIDS.put(channel.getUID(), deviceChannel.getNumber());
        return channel;
    }

    private void updateChannels(final List<Channel> channels) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());
    }

    @Override
    public void updateStatus(final ThingStatus status, final ThingStatusDetail statusDetail,
                             @Nullable final String description) {
        super.updateStatus(status, statusDetail, description);
    }
}
