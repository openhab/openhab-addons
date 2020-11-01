/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * The {@link SerialDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class SerialDeviceHandler extends BaseThingHandler {

    private @NonNullByDefault({}) Pattern devicePattern;

    private @Nullable String data;

    private final Map<ChannelUID, DeviceChannel> channels = new HashMap<>();

    public SerialDeviceHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            refresh(channelUID);
        } else {
            if (channels.containsKey(channelUID)) {
                final Bridge bridge = getBridge();
                if (bridge != null) {
                    final SerialBridgeHandler handler = (SerialBridgeHandler) bridge.getHandler();
                    if (handler != null) {
                        final String data = channels.get(channelUID).mapCommand(command);
                        if (data != null) {
                            handler.writeString(data);
                        }
                    }
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

        final BundleContext bundleContext = FrameworkUtil.getBundle(SerialDeviceHandler.class).getBundleContext();
        for (final Channel c : getThing().getChannels()) {
            final ChannelTypeUID type = c.getChannelTypeUID();
            if (type != null) {
                final ChannelConfig channelConfig = c.getConfiguration().as(ChannelConfig.class);
                try {
                    DeviceChannel deviceChannel = DeviceChannelFactory.createDeviceChannel(bundleContext, channelConfig,
                            type.getId());
                    if (deviceChannel != null) {
                        channels.put(c.getUID(), deviceChannel);
                    }
                } catch (final IllegalArgumentException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Configuration error for channel " + c.getUID().getId() + ": " + e.getMessage());
                    return;
                }
            }
        }

        updateStatus(ThingStatus.UNKNOWN);
        bridgeStatusChanged(getBridgeStatus());
    }

    @Override
    public void dispose() {
        channels.clear();
        data = null;
        super.dispose();
    }

    /**
     * Handle a line of data received from the bridge
     *
     * @param data the line of data
     */
    public void handleData(final String data) {
        if (devicePattern.matcher(data).matches()) {
            this.data = data;
            channels.keySet().forEach(this::refresh);
        }
    }

    @Override
    public void bridgeStatusChanged(final ThingStatusInfo bridgeStatusInfo) {
        if (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
            return;
        }

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE && getThing().getStatus() == ThingStatus.UNKNOWN) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            return;
        }

        super.bridgeStatusChanged(bridgeStatusInfo);
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
     * @param channelId the channel to refresh
     */
    private void refresh(final ChannelUID channelUID) {
        final String data = this.data;

        if (data == null || !isLinked(channelUID)) {
            return;
        }

        if (channels.containsKey(channelUID)) {
            final String state = channels.get(channelUID).transformData(data);

            if (state != null) {
                updateState(channelUID, new StringType(state));
            }
        }
    }
}
