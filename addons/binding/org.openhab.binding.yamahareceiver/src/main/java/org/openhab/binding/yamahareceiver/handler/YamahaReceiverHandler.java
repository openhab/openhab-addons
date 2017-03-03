/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.discovery.ZoneDiscoveryService;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverState;
import org.openhab.binding.yamahareceiver.internal.protocol.YamahaReceiverCommunication;
import org.openhab.binding.yamahareceiver.internal.protocol.YamahaReceiverCommunication.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YamahaReceiverHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Gräff - Initial contribution
 */
public class YamahaReceiverHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(YamahaReceiverHandler.class);
    private String host;
    private int refrehInterval = 60; // Default: Every 1min
    private float relativeVolumeChangeFactor = 0.5f; // Default: 0.5 percent
    private long lastRefreshInMS = 0;
    private YamahaReceiverState state = null;
    private ScheduledFuture<?> refreshTimer;
    private ZoneDiscoveryService zoneDiscoveryService;

    public YamahaReceiverHandler(Thing thing) {
        super(thing);
    }

    /**
     * We handle updates of this thing ourself.
     */
    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;

        // Check if host configuration has changed
        String host_config = (String) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_HOST_NAME);
        if (host_config != null && !host_config.equals(host)) {
            host = host_config;
            createCommunicationObject();
        }

        // Check if refresh configuration has changed
        BigDecimal interval_config = (BigDecimal) thing.getConfiguration()
                .get(YamahaReceiverBindingConstants.CONFIG_REFRESH);
        if (interval_config != null && interval_config.intValue() != refrehInterval) {
            setupRefreshTimer(interval_config.intValue());
        }

        // Read the configuration for the relative volume change factor.
        BigDecimal relVolumeChange = (BigDecimal) thing.getConfiguration()
                .get(YamahaReceiverBindingConstants.CONFIG_RELVOLUMECHANGE);
        if (relVolumeChange != null) {
            relativeVolumeChangeFactor = relVolumeChange.floatValue();
        } else {
            relativeVolumeChangeFactor = 0.5f;
        }
    }

    /**
     * Calls createCommunicationObject if the host name is configured correctly.
     */
    @Override
    public void initialize() {
        host = (String) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_HOST_NAME);

        if (host == null || host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname not set!");
            return;
        }

        createCommunicationObject();
    }

    /**
     * We create a YamahaReceiverState that handles the current state (loudness, power, input etc)
     * and a communication object.
     */
    private void createCommunicationObject() {
        // Read the configuration for the relative volume change factor.
        BigDecimal relVolumeChange = (BigDecimal) thing.getConfiguration()
                .get(YamahaReceiverBindingConstants.CONFIG_RELVOLUMECHANGE);
        if (relVolumeChange != null) {
            relativeVolumeChangeFactor = relVolumeChange.floatValue();
        }

        // Determine the zone of this thing
        String zoneName = (String) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_ZONE);
        if (zoneName == null) {
            zoneName = YamahaReceiverCommunication.Zone.Main_Zone.name();
        }

        Zone zone = YamahaReceiverCommunication.Zone.valueOf(zoneName);

        state = new YamahaReceiverState(new YamahaReceiverCommunication(host, zone));
        try {
            state.updateDeviceInformation();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
        updateReceiverState();
        setupRefreshTimer(0);

        // If we are the main zone, detect other zones now.
        if (zone == Zone.Main_Zone) {
            zoneDiscoveryService = new ZoneDiscoveryService();
            zoneDiscoveryService.start(bundleContext);
            zoneDiscoveryService.detectZones(state, thing.getUID().getId());
        }
    }

    /**
     * Sets up a refresh timer (using the scheduler) with the CONFIG_REFRESH interval.
     *
     * @param initial_wait_time The delay before the first refresh. Maybe 0 to immediately
     *            initiate a refresh.
     */
    private void setupRefreshTimer(int initial_wait_time) {
        if (state == null) {
            return;
        }

        Object interval_config_o = thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_REFRESH);
        Integer interval_config;

        if (interval_config_o == null) {
            interval_config = refrehInterval;
        } else {
            interval_config = interval_config_o instanceof Integer ? (Integer) interval_config_o
                    : ((BigDecimal) interval_config_o).intValue();
        }

        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
        refreshTimer = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateReceiverState();
            }
        }, initial_wait_time, interval_config, TimeUnit.SECONDS);

        refrehInterval = interval_config;
    }

    /**
     * All channels of this thing will be updated after a response from the Yamaha device.
     * If the device does not respond it be assumed to be offline.
     * We cannot refresh just a single channel, but only all
     * channels at a time. Because that is a costly operation, we only allow a refresh every
     * 3 seconds.
     */
    private void updateReceiverState() {
        if (lastRefreshInMS + 3000 > System.currentTimeMillis()) {
            return;
        }
        lastRefreshInMS = System.currentTimeMillis();

        try {
            state.updateState();
            updateStatus(ThingStatus.ONLINE);
            updateState(YamahaReceiverBindingConstants.CHANNEL_POWER, state.isPower() ? OnOffType.ON : OnOffType.OFF);
            updateState(YamahaReceiverBindingConstants.CHANNEL_INPUT, new StringType(state.getInput()));
            updateState(YamahaReceiverBindingConstants.CHANNEL_SURROUND, new StringType(state.getSurroundProgram()));
            updateState(YamahaReceiverBindingConstants.CHANNEL_VOLUME, new PercentType((int) state.getVolume()));
            updateState(YamahaReceiverBindingConstants.CHANNEL_MUTE, state.isMute() ? OnOffType.ON : OnOffType.OFF);
            updateState(YamahaReceiverBindingConstants.CHANNEL_NETRADIO_TUNE, new DecimalType(state.netRadioChannel));
            logger.trace("State upddated!");
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (state == null) {
            logger.debug("Thing not yet initialized!");
            return;
        }

        String id = channelUID.getId();

        // The user want to refresh a value. We cannot refresh just a single channel, but only all
        // channels at a time. Because that is a costly operation, we only allow a user requested refresh every
        // 3 seconds.
        if (command instanceof RefreshType) {
            updateReceiverState();
            return;
        }

        try {
            switch (id) {
                case YamahaReceiverBindingConstants.CHANNEL_POWER:
                    state.setPower(((OnOffType) command) == OnOffType.ON);
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_INPUT:
                    state.setInput(((StringType) command).toString());
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_SURROUND:
                    state.setSurroundProgram(((StringType) command).toString());
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_VOLUME_DB:
                    state.setVolumeDB(((DecimalType) command).floatValue());
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_VOLUME:
                    if (command instanceof DecimalType) {
                        state.setVolume(((DecimalType) command).floatValue());
                    } else if (command instanceof IncreaseDecreaseType) {
                        state.setVolumeRelative(((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE
                                ? relativeVolumeChangeFactor : -relativeVolumeChangeFactor);
                    }
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_MUTE:
                    state.setMute(((OnOffType) command) == OnOffType.ON);
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_NETRADIO_TUNE:
                    state.setNetRadio(((DecimalType) command).intValue());
                    break;
                default:
                    logger.error("Channel " + id + " not supported!");
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
