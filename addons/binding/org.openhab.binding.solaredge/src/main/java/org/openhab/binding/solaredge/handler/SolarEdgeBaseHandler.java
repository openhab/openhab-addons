/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.handler;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.solaredge.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.connector.WebInterface;
import org.openhab.binding.solaredge.internal.model.Channel;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarEdgeBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public abstract class SolarEdgeBaseHandler extends BaseThingHandler implements SolarEdgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SolarEdgeBaseHandler.class);

    /**
     * Interface object for querying the Solaredge web interface
     */
    private WebInterface webInterface;

    /**
     * Schedule for polling live data
     */
    @Nullable
    private ScheduledFuture<?> liveDataPollingJob;

    /**
     * Schedule for polling aggregate data
     */
    @Nullable
    private ScheduledFuture<?> aggregateDataPollingJob;

    public SolarEdgeBaseHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.webInterface = new WebInterface(getConfiguration(), scheduler, this, httpClient);
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
        logger.debug("Solaredge initialized with configuration: {}", config);

        if (config.getTokenOrApiKey() != null) {
            startPolling();
            webInterface.start();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no username/password set");
        }
    }

    /**
     * Start the polling.
     */
    private synchronized void startPolling() {
        if (liveDataPollingJob == null || liveDataPollingJob.isCancelled()) {
            logger.debug("start live data polling job at intervall {}",
                    getConfiguration().getLiveDataPollingInterval());
            liveDataPollingJob = scheduler.scheduleWithFixedDelay(new SolarEdgeLiveDataPolling(this), 1,
                    getConfiguration().getLiveDataPollingInterval(), TimeUnit.MINUTES);
        } else {
            logger.debug("live data pollingJob already active");
        }
        if (aggregateDataPollingJob == null || aggregateDataPollingJob.isCancelled()) {
            logger.debug("start aggregate data polling job at intervall {}",
                    getConfiguration().getAggregateDataPollingInterval());
            liveDataPollingJob = scheduler.scheduleWithFixedDelay(new SolarEdgeAggregateDataPolling(this), 2,
                    getConfiguration().getAggregateDataPollingInterval(), TimeUnit.MINUTES);
        } else {
            logger.debug("aggregate data pollingJob already active");
        }
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if (liveDataPollingJob != null && !liveDataPollingJob.isCancelled()) {
            logger.debug("stop live data polling job");
            liveDataPollingJob.cancel(true);
            liveDataPollingJob = null;
        }
        if (aggregateDataPollingJob != null && !aggregateDataPollingJob.isCancelled()) {
            logger.debug("stop aggregate data polling job");
            aggregateDataPollingJob.cancel(true);
            aggregateDataPollingJob = null;
        }

        // the webinterface also makes use of the scheduler and must stop it's jobs
        if (webInterface != null) {
            webInterface.dispose();
        }
    }

    @Override
    public @Nullable WebInterface getWebInterface() {
        return webInterface;
    }

    /**
     * will update all channels provided in the map
     */
    @Override
    public void updateChannelStatus(Map<String, @Nullable String> values) {
        logger.debug("Handling channel update.");

        for (String key : values.keySet()) {
            Channel channel = getThingSpecificChannel(key);
            if (channel != null) {
                String value = values.get(key);
                logger.debug("Channel is to be updated: {}: {}", channel.getFQName(), value);
                if (value != null) {
                    if (channel.getJavaType().equals(Double.class)) {
                        try {
                            updateState(channel.getFQName(), convertToDecimal(value));
                        } catch (NumberFormatException ex) {
                            logger.debug("Could not update channel {} - invalid number: '{}'", channel.getFQName(),
                                    value);
                            updateState(channel.getFQName(), UnDefType.UNDEF);
                        }
                    } else {
                        updateState(channel.getFQName(), new StringType(value));
                    }
                } else {
                    logger.debug("Value is null or not provided by solaredge (channel: {})", channel.getFQName());
                    updateState(channel.getFQName(), UnDefType.UNDEF);
                }
            } else {
                logger.debug("Could not identify channel: {} for model {}", key,
                        getThing().getThingTypeUID().getAsString());
            }
        }
    }

    /**
     * internal method for conversion to a valid decimal value.
     *
     * @throws NumberFormatException
     * @param number as String
     * @return converted value to DecimalType
     */
    private DecimalType convertToDecimal(String number) {
        return new DecimalType(number.replaceAll(",", ".").replaceAll("[^0-9.-]", ""));
    }

    /**
     * return the channel with the specific id. Will return null if no channel is found.
     *
     * @param id identifier of the channel
     * @return
     */
    @Nullable
    protected abstract Channel getThingSpecificChannel(String id);

    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public SolarEdgeConfiguration getConfiguration() {
        return this.getConfigAs(SolarEdgeConfiguration.class);
    }

}
