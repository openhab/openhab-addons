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
package org.openhab.binding.hccrubbishcollection.internal;

import static org.openhab.binding.hccrubbishcollection.internal.HCCRubbishCollectionBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HCCRubbishCollectionHandler} is responsible for handling commands,
 * updating the channels and polling the API.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HCCRubbishCollectionHandler extends BaseThingHandler {
    private static final int DELAY_NETWORKERROR = 3; // On network error tries again in 3 minutes.
    private static final int DELAY_UPDATE = 480; // Polls API every 8 hours.

    private final Logger logger = LoggerFactory.getLogger(HCCRubbishCollectionHandler.class);

    private final HttpClient httpClient;
    private @Nullable API api;

    private @Nullable ScheduledFuture<?> refreshScheduler; // The API refresh scheduler
    private @Nullable ScheduledFuture<?> collectionScheduler; // The Collection event trigger scheduler

    /** Object disposing flag */
    private boolean isDisposing = false;

    /**
     * Create Handler.
     * 
     * @param thing The thing type passed from the Handler Factory.
     * @param httpClient The common http client provided from openHAB.
     */
    public HCCRubbishCollectionHandler(Thing thing, HttpClient httpClient) {
        super(thing);

        this.httpClient = httpClient;
    }

    /**
     * Handles a command coming from openHAB.
     * Only RefreshType is supported as all channels are read only.
     * 
     * @param channelUID The channel UID.
     * @param command The command.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateNow();
        }
    }

    /**
     * Refreshes the data immediately.
     */
    private void updateNow() {
        if (isDisposing) {
            return;
        }

        logger.debug("Updating data immediately");
        stopUpdate(false);
        startUpdate(0);
    }

    @Override
    public void initialize() {
        final HCCRubbishCollectionConfiguration config = getConfigAs(HCCRubbishCollectionConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        api = new API(httpClient, config.address);
        startUpdate(0);
    }

    /**
     * Gets the Rubbish collection data from the {@link API} and updates the
     * channels with the data.
     */
    private void updateData() {
        logger.debug("Fetching new data");
        final API localApi = api;
        if (localApi != null) {
            if (isDisposing) {
                return;
            }
            if (localApi.update()) {
                if (isDisposing) {
                    return;
                }
                updateStatus(ThingStatus.ONLINE); // Updates Thing to online since API update was successful.

                Integer localDay = localApi.getDay();
                if (localDay != null) {
                    updateState(CHANNEL_DAY, new DecimalType(localDay));
                }

                ZonedDateTime localGeneralDate = localApi.getGeneralDate();
                if (localGeneralDate != null) {
                    updateState(CHANNEL_BIN_GENERAL, new DateTimeType(localGeneralDate));
                }

                ZonedDateTime localRecyclingDate = localApi.getRecyclingDate();
                if (localRecyclingDate != null) {
                    updateState(CHANNEL_BIN_RECYCLING, new DateTimeType(localRecyclingDate));
                }

                if (localGeneralDate != null && localRecyclingDate != null) {
                    setupCollectionEvent(localGeneralDate, localRecyclingDate);
                } else {
                    logger.debug("Cannot setup Collection Event, one or both collection dates are null.");
                }
            } else {
                if (localApi.getErrorDetail() != ThingStatusDetail.COMMUNICATION_ERROR) {
                    updateStatus(ThingStatus.OFFLINE, localApi.getErrorDetail(), localApi.getErrorDetailMessage());
                    stopUpdate(false);
                } else {
                    stopUpdate(true);
                }
            }
        } else {
            logger.error("API object is null, cannot update");
        }
    }

    /**
     * Calculates some values for the Collection Event before setting up the
     * Collection trigger {@link #scheduleCollectionEvent}.
     * 
     * @param generalDate The General Rubbish Collection Date and Time.
     * @param recyclingDate The Recycling Collection Date and Time.
     */
    private void setupCollectionEvent(ZonedDateTime generalDate, ZonedDateTime recyclingDate) {
        logger.trace("Setup Collection Trigger");

        String event;
        ZonedDateTime dateTime;
        if (generalDate.compareTo(recyclingDate) < 0) {
            logger.trace("Using General Date {} for Event", generalDate);
            dateTime = generalDate;
            event = EVENT_GENERAL;
        } else {
            logger.trace("Using Recycling Date {} for Event", recyclingDate);
            dateTime = recyclingDate;
            event = EVENT_RECYCLING;
        }

        logger.trace("Loading channel config");
        Channel collectionTriggerChannel = getThing().getChannel(TRIGGER_COLLECTION);
        HCCRubbishCollectionEventConfiguration collectionEventConfig = (collectionTriggerChannel == null) ? null
                : collectionTriggerChannel.getConfiguration().as(HCCRubbishCollectionEventConfiguration.class);

        long offset = 0;
        if (collectionEventConfig != null) {
            offset = (long) collectionEventConfig.offset;
        } else {
            logger.debug("Could not get event config, default offset of {} set", offset);
        }

        ZonedDateTime offsettedDateTime = dateTime.plusMinutes(offset);
        logger.trace("Event offset by {} minutes, new datetime {}", offset, offsettedDateTime);
        scheduleCollectionEvent(offsettedDateTime, event);
    }

    /**
     * Sets up the collection event trigger.
     * 
     * @param dateTime The Date and time to trigger the Collection Event.
     * @param event The name of the Event to be triggered.
     */
    private void scheduleCollectionEvent(ZonedDateTime dateTime, String event) {
        stopScheduleCollectionEvent(); // Stop the currently scheduled event

        if (isDisposing) {
            return;
        }

        logger.trace("Setup Collection Trigger Scheduler");

        logger.trace("Local Time {}", ZonedDateTime.now());
        long delay = dateTime.toEpochSecond() - ZonedDateTime.now().toEpochSecond();

        logger.debug("Start collection scheduler, delay {} seconds ({} minutes)", delay, delay / 60);
        if (delay > 0) {
            collectionScheduler = scheduler.schedule(() -> {
                if (isDisposing) {
                    return;
                }
                triggerChannel(TRIGGER_COLLECTION, event);
            }, delay, TimeUnit.SECONDS);
        } else {
            logger.debug("Collection trigger delay already in past, ignoring");
        }
    }

    /**
     * Starts the data update scheduler.
     * 
     * @param delay The start delay in minutes. 0 executes an immediate update.
     */
    private void startUpdate(int delay) {
        if (isDisposing) {
            return;
        }
        logger.debug("Start refresh scheduler, delay {}", delay);

        refreshScheduler = scheduler.scheduleWithFixedDelay(this::updateData, delay, DELAY_UPDATE, TimeUnit.MINUTES);
    }

    /**
     * Stops the scheduler for the collection event trigger.
     */
    private void stopScheduleCollectionEvent() {
        ScheduledFuture<?> localCollectionScheduler = collectionScheduler;
        logger.debug("Stopping Collection Trigger Scheduler");
        if (localCollectionScheduler != null) {
            localCollectionScheduler.cancel(true);
            collectionScheduler = null;
        }
    }

    /**
     * Stop the data update scheduler (stops updating data). If stopping due to a
     * network error, then resets the update scheduler {@link #startUpdate(int)}
     * with an initial delay to wait a short period then try again.
     * 
     * @param networkError Set to true if a network error. False if terminating.
     */
    private void stopUpdate(boolean networkError) {
        final ScheduledFuture<?> localRefreshScheduler = refreshScheduler;
        logger.debug("Stopping updater scheduler, networkError = {}", networkError);
        if (localRefreshScheduler != null) {
            localRefreshScheduler.cancel(true);
            refreshScheduler = null;
        }
        if (networkError) {
            logger.debug("Waiting {} minutes to try again", DELAY_NETWORKERROR);
            startUpdate(DELAY_NETWORKERROR);
        }
    }

    @Override
    public void dispose() {
        isDisposing = true; // Set true to exit any running functions
        stopUpdate(false);
        stopScheduleCollectionEvent();

        super.dispose();
    }
}
