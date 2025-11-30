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
package org.openhab.binding.smainverterbluetooth.internal;

import static org.openhab.binding.smainverterbluetooth.internal.SmaInverterBluetoothBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smainverterbluetooth.internal.cli.DeviceController;
import org.openhab.binding.smainverterbluetooth.internal.config.SmaInverterBluetoothConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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
 * The {@link smainverterbluetoothHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lee Charlton - Initial contribution
 */
@NonNullByDefault
public class SmaInverterBluetoothHandler extends BaseThingHandler {

    private final int CLIMINCALLTIME = 20; // lockout time in seconds
    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(SmaInverterBluetoothHandler.class);
    private @Nullable ZonedDateTime lockoutTimer = null;
    private SmaInverterBluetoothConfiguration config = new SmaInverterBluetoothConfiguration();
    private @Nullable ScheduledFuture<?> refreshTask;
    private DeviceController inverter = new DeviceController();
    @Nullable
    private String currentControlState = "ON";

    public SmaInverterBluetoothHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ("thing-polling-switch".equals(channelUID.getId())) { // track the current state of the polling switch
            this.currentControlState = command.toString();
        }
        if (command instanceof RefreshType) {
            refreshStateAndUpdate();
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(SmaInverterBluetoothConfiguration.class);
        logger.debug("Inverter Config: {}", config);
        inverter = new DeviceController(config);
        startAutomaticRefresh();
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_THING_POLLING_SWITCH, OnOffType.ON);
        this.currentControlState = "ON";
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        ScheduledFuture<?> refreshTask = this.refreshTask;
        if (refreshTask != null) {
            refreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    private void refreshStateAndUpdate() {
        if ("OFF".equals(currentControlState)) { // polling disabled
            logger.debug("Polling disabled, skipping refresh");
            return;
        }
        ZonedDateTime lockoutTimer = this.lockoutTimer;
        if (lockoutTimer != null && lockoutTimer.isAfter(ZonedDateTime.now())) { // lockout calls that come
                                                                                 // too fast
            logger.debug("CLI called too frequent, ignored {} ", lockoutTimer);
            return;
        }
        this.lockoutTimer = ZonedDateTime.now().plusSeconds(CLIMINCALLTIME);
        int exitCode = inverter.fetchInverterData();
        if (exitCode != 0) {
            logger.debug("Inverter CLI failed: {}", exitCode);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "CLI exited with code: " + exitCode + ". Check the CLI program is installed and executable.");
            return;
        }
        if (inverter.getCode() != 0) {
            logger.debug("Inverter returned error code: {}", inverter.getCode());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Inverter returned error code: " + inverter.getCode() + " CLI message: " + inverter.getMessage());
        }
        if (inverter.getCode() == 1) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NOT_YET_READY, "Inverter asleep");
        }
        publishChannels();
    }

    private void publishChannels() {
        if (inverter.getCode() == 0) {
            updateState(CHANNEL_INVERTER_DAY_GENERATION, new DecimalType(inverter.getDaily()));
            updateState(CHANNEL_INVERTER_TOTAL_GENERATION, new DecimalType(inverter.getTotal()));
            updateState(CHANNEL_INVERTER_SPOT_AC_VOLTAGE, new DecimalType(inverter.getSpotACVolts()));
            updateState(CHANNEL_INVERTER_SPOT_POWER, new DecimalType(inverter.getSpotPower()));
            updateState(CHANNEL_INVERTER_SPOT_TEMPERATURE, new DecimalType(inverter.getSpotTemperature()));
            updateState(CHANNEL_INVERTER_TIME, new StringType(inverter.getTime()));
        }
        updateState(CHANNEL_INVERTER_STATUS_CODE, new DecimalType(inverter.getCode()));
        updateState(CHANNEL_INVERTER_STATUS_MESSAGE, new StringType(inverter.getMessage()));
    }

    private void startAutomaticRefresh() {
        this.refreshTask = scheduler.scheduleWithFixedDelay(this::refreshStateAndUpdate, 0, config.getRefreshInterval(),
                TimeUnit.SECONDS);
    }
}
