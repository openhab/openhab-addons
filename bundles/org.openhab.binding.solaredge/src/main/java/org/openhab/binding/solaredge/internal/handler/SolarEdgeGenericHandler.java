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
package org.openhab.binding.solaredge.internal.handler;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.STATUS_WAITING_FOR_LOGIN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solaredge.internal.AtomicReferenceTrait;
import org.openhab.binding.solaredge.internal.command.AggregateDataUpdatePrivateApi;
import org.openhab.binding.solaredge.internal.command.AggregateDataUpdatePublicApi;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdateMeterless;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdatePrivateApi;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdatePublicApi;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.connector.CommunicationStatus;
import org.openhab.binding.solaredge.internal.connector.WebInterface;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarEdgeGenericHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SolarEdgeGenericHandler extends BaseThingHandler implements SolarEdgeHandler, AtomicReferenceTrait {
    private final Logger logger = LoggerFactory.getLogger(SolarEdgeGenericHandler.class);

    private static final long LIVE_POLLING_INITIAL_DELAY = 1;
    private static final long AGGREGATE_POLLING_INITIAL_DELAY = 2;

    /**
     * Interface object for querying the Solaredge web interface
     */
    private WebInterface webInterface;

    /**
     * Schedule for polling live data
     */
    private final AtomicReference<@Nullable Future<?>> liveDataPollingJobReference;

    /**
     * Schedule for polling aggregate data
     */
    private final AtomicReference<@Nullable Future<?>> aggregateDataPollingJobReference;

    public SolarEdgeGenericHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.webInterface = new WebInterface(scheduler, this, httpClient);
        this.liveDataPollingJobReference = new AtomicReference<>(null);
        this.aggregateDataPollingJobReference = new AtomicReference<>(null);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("command for {}: {}", channelUID, command);
        // write access is not supported.
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize SolarEdge");
        SolarEdgeConfiguration config = getConfiguration();
        logger.debug("SolarEdge initialized with configuration: {}", config);

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_LOGIN);
        webInterface.start();
        startPolling();
    }

    /**
     * Start the polling.
     */
    private void startPolling() {
        updateJobReference(liveDataPollingJobReference, scheduler.scheduleWithFixedDelay(this::liveDataPollingRun,
                LIVE_POLLING_INITIAL_DELAY, getConfiguration().getLiveDataPollingInterval(), TimeUnit.MINUTES));

        updateJobReference(aggregateDataPollingJobReference,
                scheduler.scheduleWithFixedDelay(this::aggregateDataPollingRun, AGGREGATE_POLLING_INITIAL_DELAY,
                        getConfiguration().getAggregateDataPollingInterval(), TimeUnit.MINUTES));
    }

    /**
     * Poll the SolarEdge Webservice one time per call to retrieve live data.
     */
    void liveDataPollingRun() {
        logger.debug("polling SolarEdge live data {}", getConfiguration());
        SolarEdgeCommand ldu;

        if (getConfiguration().isUsePrivateApi()) {
            ldu = new LiveDataUpdatePrivateApi(this, this::updateOnlineStatus);
        } else {
            if (getConfiguration().isMeterInstalled()) {
                ldu = new LiveDataUpdatePublicApi(this, this::updateOnlineStatus);
            } else {
                ldu = new LiveDataUpdateMeterless(this, this::updateOnlineStatus);
            }
        }
        getWebInterface().enqueueCommand(ldu);
    }

    /**
     * Poll the SolarEdge Webservice one time per call to retrieve aggregate data.
     */
    void aggregateDataPollingRun() {
        // if no meter is present all data will be fetched by the 'LiveDataUpdateMeterless'
        if (getConfiguration().isMeterInstalled()) {
            logger.debug("polling SolarEdge aggregate data {}", getConfiguration());
            List<SolarEdgeCommand> commands = new ArrayList<>();

            if (getConfiguration().isUsePrivateApi()) {
                commands.add(new AggregateDataUpdatePrivateApi(this, AggregatePeriod.DAY, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePrivateApi(this, AggregatePeriod.WEEK, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePrivateApi(this, AggregatePeriod.MONTH, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePrivateApi(this, AggregatePeriod.YEAR, this::updateOnlineStatus));
            } else {
                commands.add(new AggregateDataUpdatePublicApi(this, AggregatePeriod.DAY, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePublicApi(this, AggregatePeriod.WEEK, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePublicApi(this, AggregatePeriod.MONTH, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePublicApi(this, AggregatePeriod.YEAR, this::updateOnlineStatus));
            }

            for (SolarEdgeCommand command : commands) {
                getWebInterface().enqueueCommand(command);
            }
        }
    }

    private void updateOnlineStatus(CommunicationStatus status) {
        switch (status.getHttpCode()) {
            case SERVICE_UNAVAILABLE:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, status.getMessage());
                break;
            case OK:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, status.getMessage());
        }
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");

        cancelJobReference(liveDataPollingJobReference);
        cancelJobReference(aggregateDataPollingJobReference);

        webInterface.dispose();
    }

    @Override
    public WebInterface getWebInterface() {
        return webInterface;
    }

    /**
     * will update all channels provided in the map
     */
    @Override
    public void updateChannelStatus(Map<Channel, State> values) {
        logger.debug("Handling channel update.");

        for (Channel channel : values.keySet()) {
            if (getChannels().contains(channel)) {
                State value = values.get(channel);
                if (value != null) {
                    logger.debug("Channel is to be updated: {}: {}", channel.getUID().getAsString(), value);
                    updateState(channel.getUID(), value);
                } else {
                    logger.debug("Value is null or not provided by solaredge (channel: {})",
                            channel.getUID().getAsString());
                    updateState(channel.getUID(), UnDefType.UNDEF);
                }
            } else {
                logger.debug("Could not identify channel: {} for model {}", channel.getUID().getAsString(),
                        getThing().getThingTypeUID().getAsString());
            }
        }
    }

    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public SolarEdgeConfiguration getConfiguration() {
        return this.getConfigAs(SolarEdgeConfiguration.class);
    }

    @Override
    public List<Channel> getChannels() {
        return getThing().getChannels();
    }

    @Override
    public @Nullable Channel getChannel(String groupId, String channelId) {
        ThingUID thingUID = this.getThing().getUID();
        ChannelGroupUID channelGroupUID = new ChannelGroupUID(thingUID, groupId);
        return getThing().getChannel(new ChannelUID(channelGroupUID, channelId));
    }
}
