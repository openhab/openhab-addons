/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.romyrobot.internal;

import static org.openhab.binding.romyrobot.internal.RomyRobotBindingConstants.*;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.romyrobot.internal.api.RomyApi;
import org.openhab.binding.romyrobot.internal.api.RomyApiFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RomyRobotHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernhard Kreuz - Initial contribution
 */
@NonNullByDefault
public class RomyRobotHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RomyRobotHandler.class);

    private @NotNull RomyRobotConfiguration config;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable RomyApi romyDevice;
    private @Nullable RomyApiFactory apiFactory;

    public RomyRobotHandler(Thing thing, RomyApiFactory apiFactory) {
        super(thing);
        this.apiFactory = apiFactory;
        config = getConfigAs(RomyRobotConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_FW_VERSION.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(RomyRobotConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        pollingJob = scheduler.scheduleWithFixedDelay(this::refreshVacuum, 2, config.refreshInterval, TimeUnit.SECONDS);
    }

    public void refreshVacuum() {
        RomyApi localApi = romyDevice;
        if (localApi == null) {
            setupAPI();
            localApi = romyDevice;
        }
        if (localApi != null) {
            try {
                localApi.refresh();
                updateStatus(ThingStatus.ONLINE);
                this.updateChannels();

            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Could not sync status with RomyRobot, check your robot is unlocked " + e.getMessage());
            }
        }
    }

    private void updateChannels() {
        if (romyDevice != null) {
            updateState(CHANNEL_FW_VERSION, StringType.valueOf(romyDevice.getFirmwareVersion()));
            updateState(CHANNEL_MODE, StringType.valueOf(romyDevice.getModeString()));
            updateState(CHANNEL_ACTIVE_PUMP_VOLUME, StringType.valueOf(romyDevice.getActivePumpVolume()));
            updateState(CHANNEL_BATTERY, QuantityType.valueOf(romyDevice.getBatteryLevel(), PERCENT));
            updateState(CHANNEL_CHARGING, StringType.valueOf(romyDevice.getChargingStatus()));
            updateState(CHANNEL_POWER_STATUS, StringType.valueOf(romyDevice.getPowerStatus()));
            updateState(CHANNEL_RSSI, new DecimalType(romyDevice.getRssi()));
            updateState(CHANNEL_STRATEGY, StringType.valueOf(romyDevice.getStrategy()));
            updateState(CHANNEL_SUCTION_MODE, StringType.valueOf(romyDevice.getSuctionMode()));
            updateState(CHANNEL_AVAILABLE_MAPS, StringType.valueOf(romyDevice.getAvailableMaps()));
        }
    }

    private void setupAPI() {
        logger.debug("Initializing RomyRobot with config (Hostname: {}, Port: {}, Refresh: {}, Timeout {}).",
                config.hostname, config.port, config.refreshInterval, config.timeout);
        try {
            romyDevice = apiFactory.getHttpApi(config);
            RomyApi localApi = romyDevice;

        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create an API connection to RomyRobot. Error received: " + exp);
            return;
        }
    }
}
