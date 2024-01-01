/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxtronikheatpump.internal.enums.HeatpumpChannel;
import org.openhab.binding.luxtronikheatpump.internal.enums.HeatpumpCoolingOperationMode;
import org.openhab.binding.luxtronikheatpump.internal.enums.HeatpumpOperationMode;
import org.openhab.binding.luxtronikheatpump.internal.exceptions.InvalidChannelException;
import org.openhab.binding.luxtronikheatpump.internal.exceptions.InvalidOperationModeException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
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
    private final Set<ScheduledFuture<?>> scheduledFutures = new HashSet<>();
    private static final int RETRY_INTERVAL_SEC = 60;
    private boolean tiggerChannelUpdate = false;
    private final LuxtronikTranslationProvider translationProvider;
    private LuxtronikHeatpumpConfiguration config;

    public LuxtronikHeatpumpHandler(Thing thing, LuxtronikTranslationProvider translationProvider) {
        super(thing);
        this.translationProvider = translationProvider;
        config = new LuxtronikHeatpumpConfiguration();
    }

    @Override
    public void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }

    @Override
    public void updateProperty(String name, @Nullable String value) {
        super.updateProperty(name, value);
    }

    public void setStatusConnectionError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "couldn't establish network connection [host '" + config.ipAddress + "']");
    }

    public void setStatusOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        if (command == RefreshType.REFRESH) {
            // ignore resresh command as channels will be updated automatically
            return;
        }

        HeatpumpChannel channel;

        try {
            channel = HeatpumpChannel.fromString(channelId);
        } catch (InvalidChannelException e) {
            logger.debug("Channel '{}' could not be found for thing {}", channelId, thing.getUID());
            return;
        }

        if (!channel.isWritable()) {
            logger.debug("Channel {} is a read-only channel and cannot handle command '{}'", channelId, command);
            return;
        }

        if (command instanceof QuantityType value) {
            Unit<?> unit = channel.getUnit();
            if (unit != null) {
                value = value.toUnit(unit);
            }

            command = new DecimalType(value.floatValue());
        }

        if (command instanceof OnOffType onOffCommand) {
            command = onOffCommand == OnOffType.ON ? new DecimalType(1) : DecimalType.ZERO;
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
                } catch (InvalidOperationModeException e) {
                    logger.warn("Heatpump {} mode recevieved invalid value {}: {}", channel.getCommand(), value,
                            e.getMessage());
                    return;
                }
                break;
            case CHANNEL_EINST_WK_AKT:
            case CHANNEL_EINST_BWS_AKT:
            case CHANNEL_EINST_KUCFTL_AKT:
            case CHANNEL_SOLLWERT_KUCFTL_AKT:
            case CHANNEL_SOLL_BWS_AKT:
            case CHANNEL_EINST_HEIZGRENZE_TEMP:
                float temperature = ((DecimalType) command).floatValue();
                value = (int) (temperature * 10);
                break;
            case CHANNEL_EINST_BWSTYP_AKT:
                value = ((DecimalType) command).intValue();
                try {
                    // validate the value is valid
                    HeatpumpCoolingOperationMode.fromValue(value);
                } catch (InvalidOperationModeException e) {
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
                logger.debug("Heat pump mode {} set to {}.", channel.getCommand(), value);
            } else {
                logger.warn("Failed setting heat pump mode {} to {}", channel.getCommand(), value);
            }
        } else {
            logger.warn("No valid value given for Heatpump operation {}", channel.getCommand());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(LuxtronikHeatpumpConfiguration.class);

        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "At least one mandatory configuration field is empty");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        synchronized (scheduledFutures) {
            ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(this::internalInitialize, 0,
                    RETRY_INTERVAL_SEC, TimeUnit.SECONDS);
            scheduledFutures.add(future);
        }
    }

    private void internalInitialize() {
        // connect to heatpump and check if values can be fetched
        HeatpumpConnector connector = new HeatpumpConnector(config.ipAddress, config.port);

        try {
            connector.read();
        } catch (IOException e) {
            setStatusConnectionError();
            return;
        }

        // stop trying to establish a connection for initializing the thing once it was established
        stopJobs();

        // When thing is initialized the first time or and update was triggered, set the available channels
        if (thing.getProperties().isEmpty() || tiggerChannelUpdate) {
            updateChannels(connector);
        }

        setStatusOnline();
        restartJobs();
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        tiggerChannelUpdate = true;
        super.updateConfiguration(configuration);
    }

    @Override
    public void dispose() {
        stopJobs();
    }

    private void updateChannels(HeatpumpConnector connector) {
        Integer[] visibilityValues = connector.getVisibilities();
        Integer[] heatpumpValues = connector.getValues();
        Integer[] heatpumpParams = connector.getParams();

        logger.debug("Updating available channels for thing {}", thing.getUID());

        final ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            logger.debug("ThingHandlerCallback is null. Skipping migration of last_update channel.");
            return;
        }

        ThingBuilder thingBuilder = editThing();
        List<Channel> channelList = new ArrayList<>();

        // clear channel list
        thingBuilder.withoutChannels(thing.getChannels());

        // create list with available channels
        for (HeatpumpChannel channel : HeatpumpChannel.values()) {
            Integer channelId = channel.getChannelId();
            int length = channel.isWritable() ? heatpumpParams.length : heatpumpValues.length;
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channel.getCommand());
            ChannelTypeUID channelTypeUID;
            if (channel.getCommand().matches("^channel[0-9]+$")) {
                channelTypeUID = new ChannelTypeUID(LuxtronikHeatpumpBindingConstants.BINDING_ID, "unknown");
            } else {
                channelTypeUID = new ChannelTypeUID(LuxtronikHeatpumpBindingConstants.BINDING_ID, channel.getCommand());
            }
            if ((channelId != null && length <= channelId)
                    || (config.showAllChannels == Boolean.FALSE && !channel.isVisible(visibilityValues))) {
                logger.debug("Hiding channel {}", channel.getCommand());
            } else {
                channelList.add(callback.createChannelBuilder(channelUID, channelTypeUID).build());
            }
        }

        thingBuilder.withChannels(channelList);

        updateThing(thingBuilder.build());
    }

    private void restartJobs() {
        stopJobs();

        synchronized (scheduledFutures) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                // Repeat channel update job every configured seconds
                Runnable channelUpdaterJob = new ChannelUpdaterJob(this, translationProvider);
                ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(channelUpdaterJob, 0, config.refresh,
                        TimeUnit.SECONDS);
                scheduledFutures.add(future);
            }
        }
    }

    private void stopJobs() {
        synchronized (scheduledFutures) {
            for (ScheduledFuture<?> future : scheduledFutures) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
            scheduledFutures.clear();
        }
    }

    /**
     * Set a parameter on the Luxtronik heatpump.
     *
     * @param param
     * @param value
     */
    private boolean sendParamToHeatpump(int param, int value) {
        HeatpumpConnector connector = new HeatpumpConnector(config.ipAddress, config.port);

        try {
            return connector.setParam(param, value);
        } catch (IOException e) {
            setStatusConnectionError();
        }

        return false;
    }
}
