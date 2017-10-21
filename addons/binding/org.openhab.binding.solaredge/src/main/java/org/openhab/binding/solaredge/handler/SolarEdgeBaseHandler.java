/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.solaredge.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.connector.WebInterface;
import org.openhab.binding.solaredge.internal.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarEdgeBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 *
 */
public abstract class SolarEdgeBaseHandler extends BaseThingHandler implements SolarEdgeHandler {

    private final String NO_VALUE = "--";

    private final Logger logger = LoggerFactory.getLogger(SolarEdgeBaseHandler.class);

    /**
     * Refresh interval which is used to poll values from the NibeUplink web interface (optional, defaults to 60 s)
     */
    private int refreshInterval;

    /**
     * Interface object for querying the FRITZ!Box web interface
     */
    private WebInterface webInterface;

    /**
     * Job which will do the FRITZ!Box polling
     */
    private final SolarEdgePolling pollingRunnable;

    /**
     * Schedule for polling
     */
    private ScheduledFuture<?> pollingJob;

    public SolarEdgeBaseHandler(@NonNull Thing thing) {
        super(thing);
        this.pollingRunnable = new SolarEdgePolling(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("command for {}: {}", channelUID, command);

        // TODO: write access is not supported.
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize SolarEdge");
        Thing thing = this.getThing();
        SolarEdgeConfiguration config = getConfiguration();

        logger.debug("Discovered NibeUplink initialized: {}", config);

        this.refreshInterval = config.getPollingInterval();
        this.webInterface = new WebInterface(config, this);

        if (config.getPassword() != null && config.getUsername() != null) {
            this.startPolling();
        } else {
            thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "no username/password set"));
        }
    }

    /**
     * Start the polling.
     */
    private synchronized void startPolling() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at intervall {}", refreshInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1, refreshInterval, TimeUnit.SECONDS);
        } else {
            logger.debug("pollingJob already active");
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
    }

    @Override
    public WebInterface getWebInterface() {
        return webInterface;
    }

    /**
     * will update all channels provided in the map
     */
    @Override
    public void updateChannelStatus(Map<String, String> values) {
        logger.debug("Handling channel update.");

        for (String key : values.keySet()) {
            Channel channel = getThingSpecificChannel(key);
            if (channel != null) {
                String value = values.get(key);
                logger.debug("Channel is to be updated: {}: {}", channel.getFQName(), value);
                if (value != null && !value.equals(NO_VALUE)) {
                    if (channel.getJavaType().equals(Double.class)) {
                        try {
                            updateState(channel.getFQName(), convertToDecimal(value));
                        } catch (NumberFormatException ex) {
                            logger.warn("Could not update channel {} - invalid number: '{}'", channel.getFQName(),
                                    value);
                        }
                    } else {
                        updateState(channel.getFQName(), new StringType(value));
                    }
                } else {
                    logger.debug("Value is null or not provided by solaredge (channel: {})", channel.getFQName());
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
