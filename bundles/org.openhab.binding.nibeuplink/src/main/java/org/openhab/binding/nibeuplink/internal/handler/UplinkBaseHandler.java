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
package org.openhab.binding.nibeuplink.internal.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.nibeuplink.internal.AtomicReferenceTrait;
import org.openhab.binding.nibeuplink.internal.NibeUplinkBindingConstants;
import org.openhab.binding.nibeuplink.internal.command.UpdateSetting;
import org.openhab.binding.nibeuplink.internal.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.internal.connector.UplinkWebInterface;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UplinkBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public abstract class UplinkBaseHandler extends BaseThingHandler implements NibeUplinkHandler, AtomicReferenceTrait {
    private final Logger logger = LoggerFactory.getLogger(UplinkBaseHandler.class);

    private final long POLLING_INITIAL_DELAY = 30;
    private final long HOUSE_KEEPING_INITIAL_DELAY = 300;

    private final Set<Channel> deadChannels = new HashSet<>(100);
    private final Set<ChannelGroupUID> registeredGroups = new HashSet<>(10);

    /**
     * Interface object for querying the NibeUplink web interface
     */
    private UplinkWebInterface webInterface;

    /**
     * Schedule for polling
     */
    private final AtomicReference<@Nullable Future<?>> pollingJobReference;

    /**
     * Schedule for periodic cleaning dead channel list
     */
    private final AtomicReference<@Nullable Future<?>> deadChannelHouseKeepingReference;

    public UplinkBaseHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.webInterface = new UplinkWebInterface(scheduler, this, httpClient);
        this.pollingJobReference = new AtomicReference<>(null);
        this.deadChannelHouseKeepingReference = new AtomicReference<>(null);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            logger.debug("command for {}: {}", channelUID.getIdWithoutGroup(), command.toString());
            Channel channel = getSpecificChannel(channelUID.getIdWithoutGroup());
            if (channel != null) {
                ChannelTypeUID typeUID = channel.getChannelTypeUID();
                if (typeUID != null && typeUID.getId() != null
                        && typeUID.getId().startsWith(NibeUplinkBindingConstants.RW_CHANNEL_PREFIX)) {
                    webInterface.enqueueCommand(new UpdateSetting(this, channel, command));
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize NibeUplink");
        NibeUplinkConfiguration config = getConfiguration();
        logger.debug("NibeUplink initialized with configuration: {}", config);

        registeredGroups.clear();
        validateChannelsAndRegisterGroups();

        startPolling();
        webInterface.start();
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "waiting for web api login");
    }

    /**
     * initialize the channels out of the configuration
     *
     */
    private void validateChannelsAndRegisterGroups() {
        logger.debug("Validating {} channels", getThing().getChannels().size());
        for (Channel channel : getThing().getChannels()) {
            if (!ChannelUtil.isValidNibeChannel(channel)) {
                logger.warn("Channel {} is not a valid Nibe channel ({})", channel.getUID().getIdWithoutGroup(),
                        channel.getLabel());
                deadChannels.add(channel);
            } else {
                logger.debug("Successfully validated channel {} ({})", channel.getUID().getIdWithoutGroup(),
                        channel.getLabel());
                String groupId = channel.getUID().getGroupId();
                if (groupId != null) {
                    ThingUID thingUID = this.getThing().getUID();
                    if (registeredGroups.add(new ChannelGroupUID(thingUID, groupId))) {
                        logger.debug("Successfully registered channel-group '{}'", groupId);
                    }
                }
            }
        }
    }

    /**
     * Start the polling.
     */
    private void startPolling() {
        updateJobReference(pollingJobReference, scheduler.scheduleWithFixedDelay(new UplinkPolling(this),
                POLLING_INITIAL_DELAY, getConfiguration().getPollingInterval(), TimeUnit.SECONDS));
        updateJobReference(deadChannelHouseKeepingReference, scheduler.scheduleWithFixedDelay(deadChannels::clear,
                HOUSE_KEEPING_INITIAL_DELAY, getConfiguration().getHouseKeepingInterval(), TimeUnit.SECONDS));
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");

        cancelJobReference(pollingJobReference);
        cancelJobReference(deadChannelHouseKeepingReference);

        // the webinterface also makes use of the scheduler and must stop it's jobs
        webInterface.dispose();
    }

    @Override
    public UplinkWebInterface getWebInterface() {
        return webInterface;
    }

    /**
     * will update all channels provided in the map
     *
     * @param values map containing the data updates
     */
    @Override
    public void updateChannelStatus(Map<Channel, State> values) {
        logger.debug("Handling channel update. ({} Channels)", values.size());

        for (Channel channel : values.keySet()) {
            if (getChannels().contains(channel)) {
                State value = values.get(channel);
                if (value != null) {
                    logger.debug("Channel is to be updated: {}: {}", channel.getUID().getAsString(), value);
                    updateState(channel.getUID(), value);
                }
            } else {
                logger.debug("Could not identify channel: {} for model {}", channel.getUID().getAsString(),
                        getThing().getThingTypeUID().getAsString());
            }
        }
    }

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

    public Set<ChannelGroupUID> getRegisteredGroups() {
        return registeredGroups;
    }
}
