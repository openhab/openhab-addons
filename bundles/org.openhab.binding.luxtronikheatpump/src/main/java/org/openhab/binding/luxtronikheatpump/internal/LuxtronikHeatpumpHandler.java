/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.luxtronikheatpump.internal;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.luxtronikheatpump.internal.enums.HeatpumpChannel;
import org.openhab.binding.luxtronikheatpump.internal.enums.HeatpumpCoolingOperationMode;
import org.openhab.binding.luxtronikheatpump.internal.enums.HeatpumpOperationMode;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LuxtronikHeatpumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
public class LuxtronikHeatpumpHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LuxtronikHeatpumpHandler.class);
    private final Lock monitor = new ReentrantLock();
    private final Set<ScheduledFuture<?>> scheduledFutures = new HashSet<>();

    public LuxtronikHeatpumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }

    @Override
    public void updateProperty(String name, String value) {
        super.updateProperty(name, value);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        if (command == RefreshType.REFRESH) {
            restartJobs();
            return;
        }

        try {
            HeatpumpChannel.fromString(channelId);
        } catch (Exception e) {
            logger.debug("Channel {} is a read-only channel and cannot handle command '{}'", channelId, command);
            return;
        }

        HeatpumpChannel channel = HeatpumpChannel.fromString(channelId);

        if (channel.isWritable().equals(Boolean.FALSE)) {
            logger.debug("Channel {} is a read-only channel and cannot handle command '{}'", channelId, command);
            return;
        }

        if (command instanceof QuantityType) {
            command = new DecimalType(((QuantityType<?>) command).floatValue());
        }

        if (command instanceof OnOffType) {
            command = ((OnOffType) command) == OnOffType.ON ? new DecimalType(1) : DecimalType.ZERO;
        }

        if (!(command instanceof DecimalType)) {
            logger.warn("Heatpump operation for item {} must be from type: {}. Received {}", channel.getCommand(),
                    DecimalType.class.getSimpleName(), command.getClass());
            return;
        }

        Integer param = channel.getChannelId();
        Integer value = null;

        switch (channel) {
            case CHANNEL_EINST_BWTDI_AKT_MO:
            case CHANNEL_EINST_BWTDI_AKT_DI:
            case CHANNEL_EINST_BWTDI_AKT_MI:
            case CHANNEL_EINST_BWTDI_AKT_DO:
            case CHANNEL_EINST_BWTDI_AKT_FR:
            case CHANNEL_EINST_BWTDI_AKT_SA:
            case CHANNEL_EINST_BWTDI_AKT_SO:
            case CHANNEL_EINST_BWTDI_AKT_AL:
                value = ((DecimalType) command).intValue();
                break;
            case CHANNEL_BA_HZ_AKT:
            case CHANNEL_BA_BW_AKT:
                value = ((DecimalType) command).intValue();
                try {
                    // validate the value is valid
                    HeatpumpOperationMode.fromValue(value);
                } catch (Exception e) {
                    logger.warn("Heatpump {} mode recevieved invalid value {}: {}", channel.getCommand(), value,
                            e.getMessage());
                    return;
                }
                break;
            case CHANNEL_EINST_WK_AKT:
            case CHANNEL_EINST_BWS_AKT:
            case CHANNEL_EINST_KUCFTL_AKT:
            case CHANNEL_SOLLWERT_KUCFTL_AKT:
                float temperature = ((DecimalType) command).floatValue();
                value = (int) (temperature * 10);
                break;
            case CHANNEL_EINST_BWSTYP_AKT:
                value = ((DecimalType) command).intValue();
                try {
                    // validate the value is valid
                    HeatpumpCoolingOperationMode.fromValue(value);
                } catch (Exception e) {
                    logger.warn("Heatpump {} mode recevieved invalid value {}: {}", channel.getCommand(), value,
                            e.getMessage());
                    return;
                }
                break;
            case CHANNEL_EINST_KUHL_ZEIT_EIN_AKT:
            case CHANNEL_EINST_KUHL_ZEIT_AUS_AKT:
                float hours = ((DecimalType) command).floatValue();
                value = (int) (hours * 10);
                break;

            default:
                logger.debug("Received unknown channel {}", channelId);
                break;
        }

        if (param != null && value != null) {
            if (sendParamToHeatpump(param, value)) {
                logger.info("Heat pump mode {} set to {}.", channel.getCommand(), value);
            } else {
                logger.warn("Failed setting heat pump mode {} to {}", channel.getCommand(), value);
            }
        } else {
            logger.warn("No valid value given for Heatpump operation {}", channel.getCommand());
        }
    }

    @Override
    public void initialize() {
        LuxtronikHeatpumpConfiguration config = getConfigAs(LuxtronikHeatpumpConfiguration.class);

        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "At least one mandatory configuration field is empty");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        // connect to heatpump and check if values can be fetched
        HeatpumpConnector connector = new HeatpumpConnector(config.ipAddress, config.port);

        try {
            connector.read();
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "the given hostname ''" + config.ipAddress + "' of the heatpump is unknown");
            return;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "couldn't establish network connection [host '" + config.ipAddress + "']");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        Integer[] visibilityValues = connector.getVisibilities();
        Integer[] heatpumpValues = connector.getValues();
        Integer[] heatpumpParams = connector.getParams();

        ThingBuilder thingBuilder = editThing();

        // Hide all channel that are actually not available
        for (HeatpumpChannel channel : HeatpumpChannel.values()) {
            Integer channelId = channel.getChannelId();
            int length = channel.isWritable() == Boolean.TRUE ? heatpumpParams.length : heatpumpValues.length;
            if ((channelId != null && length < channelId) || (config.showAllChannels == Boolean.FALSE
                    && channel.isVisible(visibilityValues).equals(Boolean.FALSE))) {
                logger.debug("Hiding channel {}", channel.getCommand());
                ChannelUID channelUID = new ChannelUID(thing.getUID(), channel.getCommand());
                thingBuilder.withoutChannel(channelUID);
            }
        }

        updateThing(thingBuilder.build());

        restartJobs();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", getThing().getUID());
        stopJobs();
        logger.debug("Thing {} disposed", getThing().getUID());
    }

    private void restartJobs() {
        LuxtronikHeatpumpConfiguration config = getConfigAs(LuxtronikHeatpumpConfiguration.class);

        logger.debug("Restarting jobs for thing {}", getThing().getUID());
        monitor.lock();
        try {
            stopJobs();
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                // Repeat channel update job every configured seconds
                Runnable channelUpdaterJob = new ChannelUpdaterJob(getThing());
                ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(channelUpdaterJob, 0, config.refresh,
                        TimeUnit.SECONDS);
                scheduledFutures.add(future);
                logger.info("Scheduled {} every {} seconds", channelUpdaterJob, config.refresh);
            }
        } finally {
            monitor.unlock();
        }
    }

    private void stopJobs() {
        logger.debug("Stopping scheduled jobs for thing {}", getThing().getUID());
        monitor.lock();
        try {
            for (ScheduledFuture<?> future : scheduledFutures) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
            scheduledFutures.clear();
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        } finally {
            monitor.unlock();
        }
    }

    /**
     * Set a parameter on the Luxtronik heatpump.
     *
     * @param param
     * @param value
     */
    private boolean sendParamToHeatpump(int param, int value) {
        LuxtronikHeatpumpConfiguration config = getConfigAs(LuxtronikHeatpumpConfiguration.class);
        HeatpumpConnector connector = new HeatpumpConnector(config.ipAddress, config.port);

        try {
            return connector.setParam(param, value);
        } catch (UnknownHostException e) {
            logger.warn("the given hostname / ip '{}' of the heatpump is unknown", config.ipAddress);
            return false;
        } catch (IOException e) {
            logger.warn("couldn't establish network connection [host '{}']", config.ipAddress);
            return false;
        }
    }
}
