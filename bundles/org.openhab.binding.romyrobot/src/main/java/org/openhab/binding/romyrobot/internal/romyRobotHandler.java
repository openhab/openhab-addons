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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

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
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RomyRobotHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernhard Kreuz - Initial contribution
 */

public class RomyRobotHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(RomyRobotHandler.class);

    private RomyRobotConfiguration config;
    private ScheduledFuture<?> pollingJob;
    private RomyApi romyDevice;
    private RomyApiFactory apiFactory;
    private RomyRobotStateDescriptionOptionsProvider stateDescriptionProvider;

    public RomyRobotHandler(Thing thing, @NotNull RomyApiFactory apiFactory,
            RomyRobotStateDescriptionOptionsProvider stateDescriptionProvider) throws Exception {
        super(thing);
        this.apiFactory = apiFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
        config = getConfigAs(RomyRobotConfiguration.class);
        romyDevice = setupAPI(apiFactory);
    }

    @Override
    public void handleCommand(@NotNull ChannelUID channelUID, @NotNull Command command) {
        if (command instanceof RefreshType) {
            try {
                getRomyApi().refresh();
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "RomyRobot refresh threw exception.");
            }
        }
        if (CHANNEL_STRATEGY.equals(channelUID.getId())) {
            updateState(CHANNEL_STRATEGY, new StringType(command.toString()));
            try {
				getRomyApi().setStrategy(command.toString());
			} catch (Exception e) {
				logger.error("error updating strategy: {}", e);
			}
        } else if (CHANNEL_SUCTION_MODE.equals(channelUID.getId())) {
            updateState(CHANNEL_SUCTION_MODE, new StringType(command.toString()));
            try {
				getRomyApi().setSuctionMode(command.toString());
			} catch (Exception e) {
				logger.error("error updating suctionmode: {}", e);
			}
        } else if (CHANNEL_COMMAND.equals(channelUID.getId())) {
            updateState(CHANNEL_COMMAND, new StringType(command.toString()));
            try {
                getRomyApi().executeCommand(command.toString());
            } catch (Exception e) {
                logger.error("error executing command against RomyRobot", e);
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(RomyRobotConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        pollingJob = scheduler.scheduleWithFixedDelay(this::refreshVacuum, 2, config.refreshInterval, TimeUnit.SECONDS);
    }

    public void refreshVacuum() {

        try {
            getRomyApi().refresh();
            updateStatus(ThingStatus.ONLINE);
            this.updateChannels(getRomyApi());

        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not sync status with RomyRobot, check your robot is unlocked " + e.getMessage());
        }
    }

    private void updateChannels(RomyApi device) {
        if (device != null) {
            updateState(CHANNEL_FW_VERSION, StringType.valueOf(device.getFirmwareVersion()));
            updateState(CHANNEL_MODE, StringType.valueOf(device.getModeString()));
            updateState(CHANNEL_ACTIVE_PUMP_VOLUME, StringType.valueOf(device.getActivePumpVolume()));
            updateState(CHANNEL_BATTERY, QuantityType.valueOf(device.getBatteryLevel(), PERCENT));
            updateState(CHANNEL_CHARGING, StringType.valueOf(device.getChargingStatus()));
            updateState(CHANNEL_POWER_STATUS, StringType.valueOf(device.getPowerStatus()));
            updateState(CHANNEL_RSSI, new DecimalType(device.getRssi()));
            updateState(CHANNEL_AVAILABLE_MAPS_JSON, StringType.valueOf(device.getAvailableMapsJson()));
            updateMapsList(device.getAvailableMaps());
        }
    }

    public void updateMapsList(HashMap<String, String> maps) {
        logger.trace("RomyRobot updating maps list with {} options", maps.size());
        List<StateOption> options = new ArrayList<>();
        for (String key : maps.keySet()) {
            options.add(new StateOption(key, maps.get(key)));
        }
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_SELECTED_MAP), options);
    }

    private RomyApi setupAPI(RomyApiFactory apiFactory) throws Exception {
        logger.debug("Initializing RomyRobot with config (Hostname: {}, Port: {}, Refresh: {}, Timeout {}).",
                config.hostname, config.port, config.refreshInterval, config.timeout);
        return apiFactory.getHttpApi(config);
    }

    private RomyApi getRomyApi() throws Exception {
        if (romyDevice == null) {
            romyDevice = apiFactory.getHttpApi(config);
        }
        return romyDevice;
    }

    @Override
    public void dispose() {
        RomyApi localApi = romyDevice;
        if (localApi != null) {
            romyDevice = null;
        }
        ScheduledFuture<?> localFuture = pollingJob;
        if (localFuture != null) {
            localFuture.cancel(true);
            pollingJob = null;
        }
    }
}
