/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.solarmax.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarmax.internal.connector.SolarMaxCommandKey;
import org.openhab.binding.solarmax.internal.connector.SolarMaxConnector;
import org.openhab.binding.solarmax.internal.connector.SolarMaxData;
import org.openhab.binding.solarmax.internal.connector.SolarMaxException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarMaxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public class SolarMaxHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SolarMaxHandler.class);

    private SolarMaxConfiguration config = getConfigAs(SolarMaxConfiguration.class);

    private SolarMaxFailureCounter failureCounter = new SolarMaxFailureCounter(config.offlineAfterFailures);

    @Nullable
    private ScheduledFuture<?> pollingJob;

    public SolarMaxHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // Read only
    }

    @Override
    public void initialize() {
        config = getConfigAs(SolarMaxConfiguration.class);

        if (config.refreshInterval <= 0) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "The refresh interval must be greater than 0");
            return;
        }
        // stays UNKNOWN until the first successful poll, OFFLINE only after offlineAfterFailures failed polls in a row
        failureCounter = new SolarMaxFailureCounter(config.offlineAfterFailures);
        updateStatus(ThingStatus.UNKNOWN);
        configurePolling(); // Setup the scheduler
    }

    /**
     * This is called to start the refresh job and also to reset that refresh job when a config change is done.
     */
    private void configurePolling() {
        logger.debug("Polling data from {} at {}:{} (Device Address {}) every {} seconds ", getThing().getUID(),
                this.config.host, this.config.portNumber, this.config.deviceAddress, this.config.refreshInterval);
        ScheduledFuture<?> job = pollingJob;
        if (job == null || job.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, this.config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        pollingJob = null;
    }

    /**
     * Polling event used to get data from the SolarMax device
     */
    private Runnable pollingRunnable = () -> {
        updateValuesFromDevice();
    };

    private synchronized void updateValuesFromDevice() {
        logger.debug("Updating data from {} at {}:{} (Device Address {}) ", getThing().getUID(), this.config.host,
                this.config.portNumber, this.config.deviceAddress);
        // get the data from the SolarMax device
        try {
            SolarMaxData solarMaxData = SolarMaxConnector.getAllValuesFromSolarMax(config.host, config.portNumber,
                    this.config.deviceAddress, config.getConnectionSettings());

            if (solarMaxData.wasCommunicationSuccessful()) {
                failureCounter.recordSuccess();
                updateStatus(ThingStatus.ONLINE);
                updateProperties(solarMaxData);
                updateChannels(solarMaxData);
                return;
            }
            handlePollFailure("The device did not return any values");
        } catch (SolarMaxException e) {
            handlePollFailure("Communication error with the device: " + e.getMessage());
        } catch (RuntimeException e) {
            // an exception escaping from a job scheduled with scheduleWithFixedDelay would silently stop all further
            // polling, so catch everything here
            logger.warn("Unexpected error while updating {}: {}", getThing().getUID(), e.getMessage(), e);
            handlePollFailure("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * A single failed poll (e.g. one connection the device didn't accept) doesn't take the thing OFFLINE, the channels
     * just keep their last values. Only after offlineAfterFailures failed polls in a row the thing goes OFFLINE, e.g.
     * at night when the inverter is switched off.
     */
    private void handlePollFailure(String reason) {
        if (failureCounter.recordFailure()) {
            // the description deliberately doesn't contain the running count: an unchanged status info doesn't create
            // a new status event, so a thing that stays OFFLINE (all night) doesn't flood the event log
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                    reason + " (failed " + failureCounter.getThreshold() + " times in a row)");
        } else {
            logger.debug("Polling {} failed ({} of {} failures in a row before going OFFLINE): {}", getThing().getUID(),
                    failureCounter.getFailuresInARow(), failureCounter.getThreshold(), reason);
        }
    }

    /*
     * Update the channels
     */
    private void updateChannels(SolarMaxData solarMaxData) {
        logger.debug("Updating all channels");
        for (SolarMaxChannel solarMaxChannel : SolarMaxChannel.values()) {
            String channelId = solarMaxChannel.getChannelId();
            Channel channel = getThing().getChannel(channelId);

            if (channelId.equals(SolarMaxChannel.CHANNEL_LAST_UPDATED.getChannelId())) {
                // CHANNEL_LAST_UPDATED shows when the device was last read and does not come from the device, so handle
                // it specially
                if (channel != null) {
                    State state = solarMaxData.getDataDateTime();
                    logger.debug("Update channel state: {} - {}", channelId, state);
                    updateState(channel.getUID(), state);
                }
            } else {
                // must be somthing to collect from the device, so...
                if (solarMaxData.has(SolarMaxCommandKey.valueOf(channelId))) {
                    if (channel == null) {
                        logger.debug("No channel found with id: {}", channelId);
                        continue;
                    }
                    Number value = solarMaxData.get(SolarMaxCommandKey.valueOf(channelId));
                    State state = (value == null) ? null : convertValueToState(value, solarMaxChannel.getUnit());

                    if (state != null) {
                        logger.debug("Update channel state: {} - {}", channelId, state);
                        updateState(channel.getUID(), state);
                    } else {
                        logger.debug("Error refreshing channel {}: {}", getThing().getUID(), channelId);
                    }
                }
            }
        }
    }

    private @Nullable State convertValueToState(Number value, @Nullable Unit<?> unit) {
        if (unit == null) {
            // keeps integer values (e.g. operating state codes) integral, so they match the state options
            return new DecimalType(value);
        }
        return new QuantityType<>(value, unit);
    }

    /*
     * Update the properties
     */
    private void updateProperties(SolarMaxData solarMaxData) {
        logger.debug("Updating properties");
        for (SolarMaxProperty solarMaxProperty : SolarMaxProperty.values()) {
            String propertyId = solarMaxProperty.getPropertyId();
            Number valNumber = solarMaxData.get(SolarMaxCommandKey.valueOf(propertyId));
            if (valNumber == null) {
                logger.debug("Null value returned for value of {}: {}", getThing().getUID(), propertyId);
                continue;
            }

            // deal with properties
            if (propertyId.equals(SolarMaxProperty.PROPERTY_BUILD_NUMBER.getPropertyId())
                    || propertyId.equals(SolarMaxProperty.PROPERTY_SOFTWARE_VERSION.getPropertyId())) {
                updateProperty(solarMaxProperty.getPropertyId(), valNumber.toString());
                continue;
            }
        }
    }
}
