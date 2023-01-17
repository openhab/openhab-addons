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
package org.openhab.binding.smaenergymeter.internal.handler;

import static org.openhab.binding.smaenergymeter.internal.SMAEnergyMeterBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.smaenergymeter.internal.configuration.EnergyMeterConfig;
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
 * The {@link SMAEnergyMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Osman Basha - Initial contribution
 */
public class SMAEnergyMeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SMAEnergyMeterHandler.class);
    private EnergyMeter energyMeter;
    private ScheduledFuture<?> pollingJob;

    public SMAEnergyMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
        } else {
            logger.warn("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SMAEnergyMeter handler '{}'", getThing().getUID());

        EnergyMeterConfig config = getConfigAs(EnergyMeterConfig.class);

        int port = (config.getPort() == null) ? EnergyMeter.DEFAULT_MCAST_PORT : config.getPort();
        energyMeter = new EnergyMeter(config.getMcastGroup(), port);
        try {
            energyMeter.update();

            updateProperty(Thing.PROPERTY_VENDOR, "SMA");
            updateProperty(Thing.PROPERTY_SERIAL_NUMBER, energyMeter.getSerialNumber());
            logger.debug("Found a SMA Energy Meter with S/N '{}'", energyMeter.getSerialNumber());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        int pollingPeriod = (config.getPollingPeriod() == null) ? 30 : config.getPollingPeriod();
        pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, pollingPeriod, TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing SMAEnergyMeter handler '{}'", getThing().getUID());

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        energyMeter = null;
    }

    private synchronized void updateData() {
        logger.debug("Update SMAEnergyMeter data '{}'", getThing().getUID());

        try {
            energyMeter.update();

            updateState(CHANNEL_POWER_IN, energyMeter.getPowerIn());
            updateState(CHANNEL_POWER_OUT, energyMeter.getPowerOut());
            updateState(CHANNEL_ENERGY_IN, energyMeter.getEnergyIn());
            updateState(CHANNEL_ENERGY_OUT, energyMeter.getEnergyOut());

            updateState(CHANNEL_POWER_IN_L1, energyMeter.getPowerInL1());
            updateState(CHANNEL_POWER_OUT_L1, energyMeter.getPowerOutL1());
            updateState(CHANNEL_ENERGY_IN_L1, energyMeter.getEnergyInL1());
            updateState(CHANNEL_ENERGY_OUT_L1, energyMeter.getEnergyOutL1());

            updateState(CHANNEL_POWER_IN_L2, energyMeter.getPowerInL2());
            updateState(CHANNEL_POWER_OUT_L2, energyMeter.getPowerOutL2());
            updateState(CHANNEL_ENERGY_IN_L2, energyMeter.getEnergyInL2());
            updateState(CHANNEL_ENERGY_OUT_L2, energyMeter.getEnergyOutL2());

            updateState(CHANNEL_POWER_IN_L3, energyMeter.getPowerInL3());
            updateState(CHANNEL_POWER_OUT_L3, energyMeter.getPowerOutL3());
            updateState(CHANNEL_ENERGY_IN_L3, energyMeter.getEnergyInL3());
            updateState(CHANNEL_ENERGY_OUT_L3, energyMeter.getEnergyOutL3());

            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
