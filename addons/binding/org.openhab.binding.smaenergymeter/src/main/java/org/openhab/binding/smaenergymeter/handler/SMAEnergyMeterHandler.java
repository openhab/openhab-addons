/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smaenergymeter.handler;

import static org.openhab.binding.smaenergymeter.SMAEnergyMeterBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.smaenergymeter.internal.configuration.EnergyMeterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SMAEnergyMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Osman Basha - Initial contribution
 */
public class SMAEnergyMeterHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(SMAEnergyMeterHandler.class);
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
        pollingJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateData();
            }
        }, 0, pollingPeriod, TimeUnit.SECONDS);
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

            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

}
