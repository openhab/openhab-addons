/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal.handler;

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.StringUtil;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkIpAddressService;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType.DeviceType;
import org.openhab.binding.tplinksmarthome.internal.TPLinkStateDescriptionProvider;
import org.openhab.binding.tplinksmarthome.internal.device.BulbDevice;
import org.openhab.binding.tplinksmarthome.internal.device.DeviceState;
import org.openhab.binding.tplinksmarthome.internal.device.SmartHomeDevice;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TP-Link Smart Home devices.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Rewrite to generic TP-Link Smart Home Handler
 */
@NonNullByDefault
public class SmartHomeHandler extends BaseThingHandler {

    private static final Duration ONE_SECOND = Duration.ofSeconds(1);
    private static final int CONNECTION_IO_RETRIES = 5;

    private final Logger logger = LoggerFactory.getLogger(SmartHomeHandler.class);

    private final SmartHomeDevice smartHomeDevice;
    private final TPLinkIpAddressService ipAddressService;
    private final int forceRefreshThreshold;
    private final TPLinkStateDescriptionProvider stateDescriptionProvider;

    private @NonNullByDefault({}) TPLinkSmartHomeConfiguration configuration;
    private @NonNullByDefault({}) Connection connection;
    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) ExpiringCache<@Nullable DeviceState> cache;
    /**
     * Cache to avoid refresh is called multiple time in 1 second.
     */
    private @NonNullByDefault({}) ExpiringCache<@Nullable DeviceState> fastCache;

    /**
     * Constructor.
     *
     * @param thing The thing to handle
     * @param smartHomeDevice Specific Smart Home device handler
     * @param type The device type
     * @param ipAddressService Cache keeping track of ip addresses of tp link devices
     * @param stateDescriptionProvider
     */
    public SmartHomeHandler(final Thing thing, final SmartHomeDevice smartHomeDevice,
            final TPLinkSmartHomeThingType type, final TPLinkIpAddressService ipAddressService,
            final TPLinkStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.smartHomeDevice = smartHomeDevice;
        this.ipAddressService = ipAddressService;
        this.forceRefreshThreshold = type.getDeviceType() == DeviceType.SWITCH
                || type.getDeviceType() == DeviceType.DIMMER ? FORCED_REFRESH_BOUNDERY_SWITCHED_SECONDS
                        : FORCED_REFRESH_BOUNDERY_SECONDS;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(TPLinkSmartHomeActions.class);
    }

    Connection getConnection() {
        return connection;
    }

    @Override
    public void handleCommand(final ChannelUID channelUid, final Command command) {
        try {
            if (command instanceof RefreshType) {
                updateChannelState(channelUid, fastCache.getValue());
            } else if (smartHomeDevice.handleCommand(channelUid, command)) {
                // After a command always refresh the cache to make sure the cache has the latest data
                updateChannelState(channelUid, forceCacheUpdate());
            } else {
                logger.debug("Command {} is not supported for channel: {}", command, channelUid.getId());
            }
        } catch (final IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
        if (StringUtil.isBlank(configuration.ipAddress) && StringUtil.isBlank(configuration.deviceId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No ip address or the device id configured.");
            return;
        }
        logger.debug("Initializing TP-Link Smart device on ip '{}' or deviceId '{}' ", configuration.ipAddress,
                configuration.deviceId);
        connection = createConnection(configuration);
        smartHomeDevice.initialize(connection, configuration);
        cache = new ExpiringCache<>(Duration.ofSeconds(configuration.refresh), this::refreshCache);
        // If refresh > threshold fast cache invalidates after 1 second, else it behaves just as the 'normal' cache
        fastCache = configuration.refresh > forceRefreshThreshold
                ? new ExpiringCache<>(ONE_SECOND, this::forceCacheUpdate)
                : cache;
        updateStatus(ThingStatus.UNKNOWN);
        if (smartHomeDevice instanceof BulbDevice bulb) {
            stateDescriptionProvider.setMinMaxKelvin(new ChannelUID(thing.getUID(), CHANNEL_COLOR_TEMPERATURE_ABS),
                    bulb.getColorTempMin(), bulb.getColorTempMax());
        }
        // While config.xml defines refresh as min 1, this check is used to run a test that doesn't start refresh.
        if (configuration.refresh > 0) {
            startAutomaticRefresh(configuration);
        }
    }

    /**
     * Creates new Connection. Methods makes mocking of the connection in tests possible.
     *
     * @param config configuration to be used by the connection
     * @return new Connection object
     */
    Connection createConnection(final TPLinkSmartHomeConfiguration config) {
        return new Connection(config.ipAddress);
    }

    /**
     * Invalidates the cache to force an update. It returns the refreshed cached value.
     *
     * @return the refreshed value
     */
    private @Nullable DeviceState forceCacheUpdate() {
        cache.invalidateValue();
        return cache.getValue();
    }

    private @Nullable DeviceState refreshCache() {
        int retry = 1;

        while (true) {
            try {
                updateIpAddress();
                final DeviceState deviceState = new DeviceState(
                        connection.sendCommand(smartHomeDevice.getUpdateCommand()));
                updateDeviceId(deviceState.getSysinfo().getDeviceId());
                smartHomeDevice.refreshedDeviceState(deviceState);
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
                return deviceState;
            } catch (final IOException e) {
                // If there is a connection problem retry before throwing an exception
                if (retry < CONNECTION_IO_RETRIES) {
                    logger.trace("Communication error, retry {}", retry, e);
                    retry++;
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    return null;
                }
            } catch (final RuntimeException e) {
                logger.debug("Obtaining new device data unexpectedly crashed. If this keeps happening please report: ",
                        e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, e.getMessage());
                return null;
            }
        }
    }

    /**
     * Checks if the current configured ip addres is still the same as by which the device is registered on the network.
     * If there is a different ip address for this device it will update the configuration with this ip and start using
     * this ip address.
     */
    private void updateIpAddress() {
        if (configuration.deviceId == null) {
            // The device id is needed to get the ip address so if not known no need to continue.
            return;
        }
        final String lastKnownIpAddress = ipAddressService.getLastKnownIpAddress(configuration.deviceId);

        if (lastKnownIpAddress != null && !lastKnownIpAddress.equals(configuration.ipAddress)) {
            final Configuration editConfig = editConfiguration();
            editConfig.put(CONFIG_IP, lastKnownIpAddress);
            updateConfiguration(editConfig);
            configuration.ipAddress = lastKnownIpAddress;
            connection.setIpAddress(lastKnownIpAddress);
        }
    }

    /**
     * Updates the device id configuration if it's not set or throws an {@link IllegalArgumentException} if the
     * configured device id doesn't match with the id reported by the device.
     *
     * @param actualDeviceId The id of the device as actual returned by the device.
     * @throws IllegalArgumentException if the configured device id doesn't match with the id reported by the device
     *             itself.
     */
    private void updateDeviceId(final String actualDeviceId) {
        if (StringUtil.isBlank(configuration.deviceId)) {
            final Configuration editConfig = editConfiguration();
            editConfig.put(CONFIG_DEVICE_ID, actualDeviceId);
            updateConfiguration(editConfig);
            configuration.deviceId = actualDeviceId;
        } else if (!StringUtil.isBlank(actualDeviceId) && !actualDeviceId.equals(configuration.deviceId)) {
            throw new IllegalArgumentException(
                    String.format("The configured device '%s' doesn't match with the id the device reports: '%s'.",
                            configuration.deviceId, actualDeviceId));
        }
    }

    /**
     * Starts the background refresh thread.
     */
    private void startAutomaticRefresh(final TPLinkSmartHomeConfiguration config) {
        if (refreshJob == null || refreshJob.isCancelled()) {
            refreshJob = scheduler.scheduleWithFixedDelay(this::refreshChannels, config.refresh, config.refresh,
                    TimeUnit.SECONDS);
        }
    }

    void refreshChannels() {
        logger.trace("Update Channels for:{}", thing.getUID());
        getThing().getChannels().forEach(channel -> updateChannelState(channel.getUID(), cache.getValue()));
    }

    /**
     * Updates the state from the device data for the channel given the data..
     *
     * @param channelUID channel to update
     * @param deviceState the state object containing the value to set of the channel
     *
     */
    private void updateChannelState(final ChannelUID channelUID, @Nullable final DeviceState deviceState) {
        if (!isLinked(channelUID)) {
            return;
        }
        final String channelId = channelUID.isInGroup() ? channelUID.getIdWithoutGroup() : channelUID.getId();
        final State state;

        if (deviceState == null) {
            state = UnDefType.UNDEF;
        } else if (CHANNEL_RSSI.equals(channelId)) {
            state = new QuantityType<>(deviceState.getSysinfo().getRssi(), Units.DECIBEL_MILLIWATTS);
        } else {
            state = smartHomeDevice.updateChannel(channelUID, deviceState);
        }
        updateState(channelUID, state);
    }
}
