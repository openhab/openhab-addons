/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nibeuplink.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.internal.command.UpdateSetting;
import org.openhab.binding.nibeuplink.internal.connector.UplinkWebInterface;
import org.openhab.binding.nibeuplink.internal.model.Channel;
import org.openhab.binding.nibeuplink.internal.model.CustomChannels;
import org.openhab.binding.nibeuplink.internal.model.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UplinkBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 *
 */
@NonNullByDefault
public abstract class UplinkBaseHandler extends BaseThingHandler implements NibeUplinkHandler {

    private final String NO_VALUE = "--";

    private final Logger logger = LoggerFactory.getLogger(UplinkBaseHandler.class);

    private Set<Channel> deadChannels = new HashSet<>(1000);

    /**
     * Refresh interval which is used to poll values from the NibeUplink web interface (optional, defaults to 60 s)
     */
    private int refreshInterval;

    /**
     * Refresh interval which is used clean the dead channel list (optional, defaults to 1 h)
     */
    private int houseKeepingInterval = 1;

    /**
     * Interface object for querying the FRITZ!Box web interface
     */
    private UplinkWebInterface webInterface;

    /**
     * Schedule for polling
     */
    @Nullable
    private ScheduledFuture<?> pollingJob;

    /**
     * Schedule for periodic cleaning dead channel list
     */
    @Nullable
    private ScheduledFuture<?> deadChannelHouseKeeping;

    public UplinkBaseHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.webInterface = new UplinkWebInterface(getConfiguration(), scheduler, this, httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            logger.debug("command for {}: {}", channelUID.getIdWithoutGroup(), command.toString());
            Channel channel = getSpecificChannel(channelUID.getIdWithoutGroup());
            if (channel != null && !channel.isReadOnly()) {
                webInterface.enqueueCommand(new UpdateSetting(this, channel, command.toString()));
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize NibeUplink");
        NibeUplinkConfiguration config = getConfiguration();

        logger.debug("NibeUplink initialized with configuration: {}", config);

        setupCustomChannels(config);
        this.refreshInterval = config.getPollingInterval();
        this.houseKeepingInterval = config.getHouseKeepingInterval();

        startPolling();
        webInterface.start();
    }

    /**
     * initialize the custom channels out of the configuration
     *
     * @param config
     */
    private void setupCustomChannels(NibeUplinkConfiguration config) {
        CustomChannels.CH_CH01.setId(config.getCustomChannel01());
        CustomChannels.CH_CH02.setId(config.getCustomChannel02());
        CustomChannels.CH_CH03.setId(config.getCustomChannel03());
        CustomChannels.CH_CH04.setId(config.getCustomChannel04());
        CustomChannels.CH_CH05.setId(config.getCustomChannel05());
        CustomChannels.CH_CH06.setId(config.getCustomChannel06());
        CustomChannels.CH_CH07.setId(config.getCustomChannel07());
        CustomChannels.CH_CH08.setId(config.getCustomChannel08());
    }

    /**
     * Start the polling.
     */
    private synchronized void startPolling() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at intervall {}", refreshInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(new UplinkPolling(this), 1, refreshInterval,
                    TimeUnit.SECONDS);
        } else {
            logger.debug("pollingJob already active");
        }
        if (deadChannelHouseKeeping == null || deadChannelHouseKeeping.isCancelled()) {
            logger.debug("start deadChannelHouseKeeping job at intervall {}", houseKeepingInterval);
            deadChannelHouseKeeping = scheduler.scheduleWithFixedDelay(deadChannels::clear, 1, houseKeepingInterval,
                    TimeUnit.SECONDS);
        } else {
            logger.debug("deadChannelHouseKeeping already active");
        }

    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            logger.debug("stop polling job");
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (deadChannelHouseKeeping != null && !deadChannelHouseKeeping.isCancelled()) {
            logger.debug("stop polling job");
            deadChannelHouseKeeping.cancel(true);
            deadChannelHouseKeeping = null;
        }

        // the webinterface also makes use of the scheduler and must stop it's jobs
        if (webInterface != null) {
            webInterface.dispose();
        }
    }

    @Override
    public UplinkWebInterface getWebInterface() {
        return webInterface;
    }

    /**
     * will update all channels provided in the map
     */
    @Override
    public void updateChannelStatus(Map<String, String> values) {
        logger.debug("Handling channel update. ({} Channels)", values.size());

        for (String key : values.keySet()) {
            List<Channel> channels = getAllSpecificChannels(key);
            if (channels.size() == 0) {
                logger.info("Could not identify channel: {} for model {}", key,
                        getThing().getThingTypeUID().getAsString());
            }
            for (Channel channel : channels) {
                String value = values.get(key);
                logger.debug("Channel is to be updated: {}: {}", channel.getFQName(), value);
                if (value != null && !value.equals(NO_VALUE)) {
                    try {
                        updateState(channel.getFQName(), convertToDecimal(value, channel.getValueType()));
                    } catch (NumberFormatException ex) {
                        logger.warn("Could not update channel {} - invalid number: '{}'", channel.getFQName(), value);
                        updateState(channel.getFQName(), UnDefType.UNDEF);
                    }
                } else {
                    logger.debug("Value is null or not provided by heatpump (channel: {})", channel.getFQName());
                    updateState(channel.getFQName(), UnDefType.UNDEF);
                    deadChannels.add(channel);
                }
            }
        }
    }

    /**
     * internal method which composes the channel list out of the specific channels and the additional custom channels.
     * custom channel implementation is the same for all models because it has a generic approach.
     *
     * @param specificChannels
     * @return
     */
    protected List<Channel> getChannels(Channel[] specificChannels) {
        List<Channel> list = new ArrayList<>(specificChannels.length + CustomChannels.values().length);

        for (Channel channel : specificChannels) {
            list.add(channel);
        }
        for (Channel channel : CustomChannels.values()) {
            list.add(channel);
        }

        return list;
    }

    /**
     * internal method for conversion to a valid decimal value.
     *
     * @throws NumberFormatException
     * @param number as String
     * @return converted value to DecimalType
     */
    private DecimalType convertToDecimal(String number, ValueType type) {
        double value = new DecimalType(number).doubleValue();
        switch (type) {
            case NUMBER_10:
                value /= 10;
                break;
            case NUMBER_100:
                value /= 100;
                break;
            default:
                break;
        }
        return new DecimalType(value);
    }

    protected abstract List<Channel> getAllSpecificChannels(String id);

    protected abstract @Nullable Channel getSpecificChannel(String id);

    @Override
    public Set<Channel> getDeadChannels() {
        return deadChannels;
    }

    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public NibeUplinkConfiguration getConfiguration() {
        return this.getConfigAs(NibeUplinkConfiguration.class);
    }

}
