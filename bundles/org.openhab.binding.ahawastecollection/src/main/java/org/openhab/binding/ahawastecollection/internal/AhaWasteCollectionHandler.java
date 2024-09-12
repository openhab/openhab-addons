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
package org.openhab.binding.ahawastecollection.internal;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ahawastecollection.internal.CollectionDate.WasteType;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
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
 * The {@link AhaWasteCollectionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class AhaWasteCollectionHandler extends BaseThingHandler {

    private static final String DAILY_MIDNIGHT = "30 0 0 * * ? *";

    /** Scheduler to schedule jobs */
    private final CronScheduler cronScheduler;
    private final Lock monitor = new ReentrantLock();
    private final ExpiringCache<Map<WasteType, CollectionDate>> cache;

    private final TimeZoneProvider timeZoneProvider;
    private final Logger logger = LoggerFactory.getLogger(AhaWasteCollectionHandler.class);

    private @Nullable AhaCollectionSchedule collectionSchedule;

    private @Nullable ScheduledCompletableFuture<?> dailyJob;

    private final AhaCollectionScheduleFactory scheduleFactory;

    private final ScheduledExecutorService executorService;

    public AhaWasteCollectionHandler(final Thing thing, final CronScheduler scheduler,
            final TimeZoneProvider timeZoneProvider, final AhaCollectionScheduleFactory scheduleFactory,
            @Nullable final ScheduledExecutorService executorService) {
        super(thing);
        this.cronScheduler = scheduler;
        this.timeZoneProvider = timeZoneProvider;
        this.scheduleFactory = scheduleFactory;
        this.cache = new ExpiringCache<>(Duration.ofMinutes(5), this::loadCollectionDates);
        this.executorService = executorService == null ? this.scheduler : executorService;
    }

    private Map<WasteType, CollectionDate> loadCollectionDates() {
        try {
            final Map<WasteType, CollectionDate> collectionDates = this.collectionSchedule.getCollectionDates();
            this.updateStatus(ThingStatus.ONLINE);
            return collectionDates;
        } catch (final IOException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            this.executorService.execute(this::updateCollectionDates);
        } else {
            this.logger.warn("The AHA Abfuhrkalender is a read-only binding and can not handle commands");
        }
    }

    @Override
    public void initialize() {
        final AhaWasteCollectionConfiguration config = this.getConfigAs(AhaWasteCollectionConfiguration.class);

        final String commune = config.commune;
        final String street = config.street;
        final String houseNumber = config.houseNumber;
        final String houseNumberAddon = config.houseNumberAddon;
        final String collectionPlace = config.collectionPlace;

        if (commune.isBlank() || street.isBlank() || houseNumber.isBlank() || collectionPlace.isBlank()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameters are mandatory and must be configured");
            return;
        }

        this.collectionSchedule = this.scheduleFactory.create(commune, street, houseNumber, houseNumberAddon,
                collectionPlace);

        this.updateStatus(ThingStatus.UNKNOWN);

        this.executorService.execute(() -> {
            final boolean online = this.updateCollectionDates();
            if (online) {
                this.restartJob();
            }
        });
    }

    /**
     * Schedules a job that updates the collection dates at midnight.
     */
    private void restartJob() {
        this.logger.debug("Restarting jobs for thing {}", this.getThing().getUID());
        this.monitor.lock();
        try {
            this.stopJob();
            if (this.getThing().getStatus() == ThingStatus.ONLINE) {
                this.dailyJob = this.cronScheduler.schedule(this::updateCollectionDates, DAILY_MIDNIGHT);
                this.logger.debug("Scheduled {} at midnight", this.dailyJob);
                // Execute daily startup job immediately
                this.updateCollectionDates();
            }
        } finally {
            this.monitor.unlock();
        }
    }

    /**
     * Stops all jobs for this thing.
     */
    private void stopJob() {
        this.monitor.lock();
        try {
            final ScheduledCompletableFuture<?> job = this.dailyJob;
            if (job != null) {
                job.cancel(true);
            }
            this.dailyJob = null;
        } finally {
            this.monitor.unlock();
        }
    }

    private boolean updateCollectionDates() {
        final Map<WasteType, CollectionDate> collectionDates = this.cache.getValue();
        if (collectionDates == null || collectionDates.isEmpty()) {
            return false;
        }

        this.logger.debug("Retrieved {} collection entries.", collectionDates.size());
        this.updateChannels(collectionDates);
        return true;
    }

    /**
     * Refreshes the channel values with the given {@link CollectionDate}s.
     */
    private void updateChannels(final Map<WasteType, CollectionDate> collectionDates) {
        for (final Channel channel : this.getThing().getChannels()) {

            final WasteType wasteType = getWasteTypeByChannel(channel.getUID().getId());

            final CollectionDate collectionDate = collectionDates.get(wasteType);
            if (collectionDate == null) {
                this.logger.debug("No collection dates found for waste type: {}", wasteType);
                continue;
            }

            final Date nextCollectionDate = collectionDate.getDates().get(0);

            final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(nextCollectionDate.toInstant(),
                    this.timeZoneProvider.getTimeZone());
            this.updateState(channel.getUID(), new DateTimeType(zonedDateTime));
        }
    }

    private static WasteType getWasteTypeByChannel(final String channelId) {
        switch (channelId) {
            case AhaWasteCollectionBindingConstants.BIOWASTE:
                return WasteType.BIO_WASTE;
            case AhaWasteCollectionBindingConstants.LEIGHTWEIGHT_PACKAGING:
                return WasteType.LIGHT_PACKAGES;
            case AhaWasteCollectionBindingConstants.PAPER:
                return WasteType.PAPER;
            case AhaWasteCollectionBindingConstants.GENERAL_WASTE:
                return WasteType.GENERAL_WASTE;
            default:
                throw new IllegalArgumentException("Unknown channel type: " + channelId);
        }
    }
}
