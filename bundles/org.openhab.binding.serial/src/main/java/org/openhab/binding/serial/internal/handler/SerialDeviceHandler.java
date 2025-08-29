/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.serial.internal.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.serial.internal.channel.ChannelConfig;
import org.openhab.binding.serial.internal.channel.DeviceChannel;
import org.openhab.binding.serial.internal.channel.DeviceChannelFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SerialDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mike Major - Initial contribution
 * @author Roland Tapken - Added channel refresh
 */
@NonNullByDefault
public class SerialDeviceHandler extends BaseThingHandler {

    private @Nullable Pattern devicePattern;

    private @Nullable String lastValue;

    private final Map<ChannelUID, DeviceChannel> channels = new HashMap<>();

    private final Map<ChannelUID, ScheduledFuture<?>> futures = new HashMap<>();

    public SerialDeviceHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            final DeviceChannel channel = channels.get(channelUID);
            if (channel != null) {
                refresh(channelUID, channel);
            }
        } else {
            final DeviceChannel channel = channels.get(channelUID);
            if (channel != null) {
                if (getBridge() instanceof Bridge bridge
                        && bridge.getHandler() instanceof CommonBridgeHandler handler) {
                    channel.mapCommand(command).ifPresent(handler::writeString);
                }
            }
        }
    }

    @Override
    public void initialize() {
        final SerialDeviceConfiguration config = getConfigAs(SerialDeviceConfiguration.class);

        try {
            devicePattern = Pattern.compile(config.patternMatch);
        } catch (final PatternSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid device pattern: " + e.getMessage());
            return;
        }

        for (final Channel c : getThing().getChannels()) {
            final ChannelTypeUID type = c.getChannelTypeUID();
            if (type != null) {
                final ChannelConfig channelConfig = c.getConfiguration().as(ChannelConfig.class);
                try {
                    final DeviceChannel deviceChannel = DeviceChannelFactory.createDeviceChannel(channelConfig,
                            type.getId());
                    if (deviceChannel != null) {
                        channels.put(c.getUID(), deviceChannel);

                        int delay = deviceChannel.getRefreshInterval();
                        if (delay > 0) {
                            ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                                    () -> refresh(c.getUID(), deviceChannel), delay, delay, TimeUnit.SECONDS);
                            futures.put(c.getUID(), future);
                        }
                    }
                } catch (final IllegalArgumentException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Configuration error for channel " + c.getUID().getId() + ": " + e.getMessage());
                    return;
                }
            }
        }

        if (getBridgeStatus().getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        for (ScheduledFuture<?> future : this.futures.values()) {
            future.cancel(true);
        }
        channels.clear();
        lastValue = null;
        super.dispose();
    }

    /**
     * Handle a line of data received from the bridge
     *
     * @param data the line of data
     */
    public void handleData(final String data) {
        final Pattern devicePattern = this.devicePattern;

        if (devicePattern != null && devicePattern.matcher(data).matches()) {
            channels.forEach((channelUID, channel) -> {
                if (isLinked(channelUID)) {
                    channel.transformData(data).ifPresent(value -> updateState(channelUID, new StringType(value)));
                }
            });
            this.lastValue = data;
        }
    }

    /**
     * Return the bridge status.
     */
    private ThingStatusInfo getBridgeStatus() {
        final Bridge b = getBridge();
        if (b != null) {
            return b.getStatusInfo();
        } else {
            return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
        }
    }

    /**
     * Refreshes the channel with the last received data
     *
     * @param channelUID the channel to refresh
     */
    private void refresh(final ChannelUID channelUID, final DeviceChannel channel) {
        final String data = this.lastValue;
        if (!isLinked(channelUID)) {
            return;
        }

        if (!channel.getRefreshValue().isBlank()) {
            if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof CommonBridgeHandler handler) {
                Optional<String> value = channel.transformCommand(channel.getRefreshValue());
                if (value.isPresent()) {
                    handler.writeString(value.get());
                    return;
                }
            }
        }

        // Use last result line
        if (data != null) {
            channel.transformData(data).ifPresent(value -> updateState(channelUID, new StringType(value)));
        }
    }
}
