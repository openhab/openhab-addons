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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.avmfritz.internal.dto.json.EnergyStats;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Handler for a FRITZ! power meter. Handles commands, which are sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class AVMFritzPowerMeterDeviceHandler extends DeviceHandler implements AVMFritzPowerMeterActionsHandler {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzPowerMeterDeviceHandler.class);

    private final Gson gson = new Gson();

    /**
     * Schedule for polling
     */
    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable Long deviceId;

    /**
     * Constructor
     *
     * @param thing Thing object representing a FRITZ! device
     */
    public AVMFritzPowerMeterDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }

    @Override
    public void enablePowerMeterHighRefresh(long deviceId, long pollingInterval) {
        // TODO use Thing property "deviceId" instead of passing an argument
        this.deviceId = deviceId;
        startPolling(deviceId, pollingInterval);
    }

    @Override
    public void disablePowerMeterHighRefresh() {
        stopPolling();
    }

    public void onEnergyStatsUpdated(String response) {
        try {
            EnergyStats energyStats = gson.fromJson(response, EnergyStats.class);
            if (energyStats != null) {
                updateThingChannelState(CHANNEL_ENERGY,
                        new QuantityType<>(energyStats.getScaledEnergy(), Units.WATT_HOUR));
                updateThingChannelState(CHANNEL_POWER, new QuantityType<>(energyStats.getScaledPower(), Units.WATT));
                updateThingChannelState(CHANNEL_VOLTAGE,
                        new QuantityType<>(energyStats.getScaledVoltage(), Units.VOLT));
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Failed to parse JSON: {}", e.getMessage());
        }
    }

    /**
     * Start the polling.
     */
    private void startPolling(long deviceId, long pollingInterval) {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob == null || localPollingJob.isCancelled()) {
            logger.debug("Start polling job for device '{}' at interval {}s", deviceId, pollingInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                handleAction(GET_ENERGY_STATS, deviceId);
            }, 0, pollingInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops the polling.
     */
    private void stopPolling() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob != null && !localPollingJob.isCancelled()) {
            logger.debug("Stop polling job for device '{}'", deviceId);
            localPollingJob.cancel(true);
            pollingJob = null;
            deviceId = null;
        }
    }
}
