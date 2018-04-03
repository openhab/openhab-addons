/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.handler;

import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.CHANNEL_RSSI;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.device.DeviceState;
import org.openhab.binding.tplinksmarthome.internal.device.SmartHomeDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TP-Link Smart Home devices.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Rewrite to generic TP-Link Smart Home Handler
 */
public class SmartHomeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartHomeHandler.class);

    private final SmartHomeDevice smartHomeDevice;

    private TPLinkSmartHomeConfiguration configuration;
    private Connection connection;
    private ScheduledFuture<?> refreshJob;
    private ExpiringCache<DeviceState> cache;

    /**
     * Constructor
     *
     * @param thing the thing to handle
     * @param smartHomeDevice Specific Smart Home device handler
     */
    public SmartHomeHandler(@NonNull Thing thing, @NonNull SmartHomeDevice smartHomeDevice) {
        super(thing);
        this.smartHomeDevice = smartHomeDevice;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                updateChannelState(channelUID, cache.getValue());
            } else if (!smartHomeDevice.handleCommand(channelUID.getId(), connection, command, configuration)) {
                logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(TPLinkSmartHomeConfiguration.class);
        logger.debug("Initializing TP-Link Smart device on ip {}", configuration.ipAddress);
        connection = createConnection(configuration);
        cache = new ExpiringCache<DeviceState>(TimeUnit.SECONDS.toMillis(configuration.refresh), this::refreshCache);
        updateStatus(ThingStatus.UNKNOWN);
        startAutomaticRefresh(configuration);
    }

    /**
     * Creates new Connection. Methods makes mocking of the connection in tests possible.
     *
     * @param config configuration to be used by the connection
     * @return new Connection object
     */
    Connection createConnection(TPLinkSmartHomeConfiguration config) {
        return new Connection(config.ipAddress);
    }

    private DeviceState refreshCache() {
        try {
            DeviceState deviceState = new DeviceState(connection.sendCommand(smartHomeDevice.getUpdateCommand()));
            updateStatus(ThingStatus.ONLINE);

            return deviceState;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return null;
        }
    }

    /**
     * Starts the background refresh thread.
     */
    private void startAutomaticRefresh(TPLinkSmartHomeConfiguration config) {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                logger.trace("Update Channels for:{}", thing.getUID());
                DeviceState value = cache.getValue();
                for (Channel channel : getThing().getChannels()) {
                    updateChannelState(channel.getUID(), value);
                }
            };

            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, config.refresh.intValue(), TimeUnit.SECONDS);
        }
    }

    /**
     * Updates the state from the device data for the channel given the data..
     *
     * @param channelUID channel to update
     * @param deviceState the state object containing the value to set of the channel
     *
     */
    private void updateChannelState(ChannelUID channelUID, DeviceState deviceState) {
        String channelId = channelUID.getId();
        final State state;

        if (deviceState == null) {
            state = UnDefType.UNDEF;
        } else if (CHANNEL_RSSI.equals(channelId)) {
            state = new DecimalType(deviceState.getSysinfo().getRssi());
        } else {
            state = smartHomeDevice.updateChannel(channelId, deviceState);
        }
        updateState(channelUID, state);
    }
}
