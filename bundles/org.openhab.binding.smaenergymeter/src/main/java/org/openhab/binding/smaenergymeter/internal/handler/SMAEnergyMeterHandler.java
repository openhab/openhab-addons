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
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

//            int pollingPeriod = (config.getPollingPeriod() == null) ? 30 : config.getPollingPeriod();
//            pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, pollingPeriod, TimeUnit.SECONDS);
//            logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
//
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
            List<SmaChannel> channels = energyMeter.update();

            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }

            ThingBuilder thingBuilder = editThing();
            boolean updated = false;
            Set<String> notUpdatedChannels = thing.getChannels().stream().map(c -> c.getUID().getId())
                    .collect(Collectors.toSet());
            for (SmaChannel smaChannel : channels) {
                if (getThing().getChannel(smaChannel.getMeasuredUnit() + "_" + smaChannel.getDatatype()) == null) {
                    Channel channel = createChannel(smaChannel);
                    thingBuilder.withChannel(channel);
                    updated = true;
                }
                notUpdatedChannels.remove(smaChannel.getMeasuredUnit() + "_" + smaChannel.getDatatype());
            }

            for (SmaChannel smaChannel : channelsToDelete) {
                Channel channel = thing.getChannel(smaChannel.getMeasuredUnit() + "_" + smaChannel.getDatatype());
                thingBuilder.withoutChannel(channel.getUID());
                logger.debug("Channel removed {}", channel);
                updated = true;
            }
            if (updated)
                updateThing(thingBuilder.build());

            for (SmaChannel channelInformation : channels) {
                String channelName = channelInformation.getMeasuredUnit() + "_" + channelInformation.getDatatype();
                updateState(channelName, new org.openhab.core.library.types.DecimalType(channelInformation.getValue()));
            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private Channel createChannel(SmaChannel channelInformation) {
        Channel channel = ChannelBuilder
                .create(new ChannelUID(thing.getUID(),
                        channelInformation.getMeasuredUnit() + "_" + channelInformation.getDatatype()), "Number")
                .withAcceptedItemType("Number").withLabel(channelInformation.getMeasuredUnit().toString())
                .withType(new ChannelTypeUID("smaenergymeter", channelInformation.getUnit().toString()))
                .withDescription(channelInformation.getMeasuredUnit() + " " + channelInformation.getDatatype()).build();
        logger.debug("New channel created {}", channel);
        return channel;
    }
}
