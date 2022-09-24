/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.smaenergymeter.internal.configuration.EnergyMeterConfig;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
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

            int pollingPeriod = (config.getPollingPeriod() == null) ? 30 : config.getPollingPeriod();
            pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, pollingPeriod, TimeUnit.SECONDS);
            logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
        }
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
            List<EnergyMeterData> energyMeterDataEntries = energyMeter.update();

            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }

            ThingBuilder thingBuilder = editThing();
            boolean updatedThing = false;

            for (EnergyMeterData smaEnergyMeterData : energyMeterDataEntries) {
                if (getThing().getChannel(smaEnergyMeterData.getOhChannelName()) == null) {
                    org.openhab.core.thing.Channel channel = createChannel(smaEnergyMeterData);
                    thingBuilder.withChannel(channel);
                    updatedThing = true;
                }
            }

            if (updatedThing)
                updateThing(thingBuilder.build());

            for (EnergyMeterData energyMeterDataInformation : energyMeterDataEntries) {
                String channelName = energyMeterDataInformation.getEnergyMeterValue() + "_"
                        + energyMeterDataInformation.getDatatype();
                updateState(channelName,
                        new org.openhab.core.library.types.DecimalType(energyMeterDataInformation.getValue()));
            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private org.openhab.core.thing.Channel createChannel(EnergyMeterData energyMeterDataInformation) {
        org.openhab.core.thing.Channel channel = ChannelBuilder
                .create(new ChannelUID(thing.getUID(), energyMeterDataInformation.getOhChannelName()), "Number")
                .withAcceptedItemType("Number").withLabel(energyMeterDataInformation.getOhChannelName())
                .withType(new ChannelTypeUID("smaenergymeter", energyMeterDataInformation.getUnit().toString()))
                .withDescription(energyMeterDataInformation.getEnergyMeterValue() + " "
                        + energyMeterDataInformation.getDatatype())
                .build();
        return channel;
    }
}
