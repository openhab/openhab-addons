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
package org.openhab.binding.metofficedatahub.internal.api;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RequestLimiter} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class RequestLimiter {

    public static final int INVALID_REQUEST_ID = -1;
    public static final int SECONDS_PER_DAY = 86400;

    static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    final StorageService storageService;
    final ScheduledExecutorService scheduler;
    final TimeZoneProvider timeZoneProvider;
    final TranslationProvider translationProvider;
    final LocaleProvider localeProvider;
    final Bundle bundle;

    private final Logger logger = LoggerFactory.getLogger(RequestLimiter.class);
    private final Object dailyResetLock = new Object();

    private int requestLimit = 0;
    private int currentRequestCount = 0;
    private String limiterId;
    private String storageKeyCount;
    private String storageKeyTimestamp;
    private @Nullable ScheduledFuture<?> dailyResetFuture = null;

    public int getCurrentRequestCount() {
        return currentRequestCount;
    }

    public RequestLimiter(final String limiterId, @Reference StorageService storageService,
            @Reference TimeZoneProvider timeZoneProvider, ScheduledExecutorService scheduler,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider,
            @Reference Bundle bundle) {
        this.limiterId = limiterId;
        this.storageKeyCount = limiterId + "_count";
        this.storageKeyTimestamp = limiterId + "_ts";
        this.storageService = storageService;
        this.timeZoneProvider = timeZoneProvider;
        this.scheduler = scheduler;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = bundle;
        loadLimiterData();
    }

    public void dispose() {
        cancelPendingDailyReset();
        saveLimiterData();
    }

    private void scheduleStandardResetTime(final int resetLimit) {
        // Met Office Data Hub resets at midnight UTC tz it's daily counter call's
        final ZonedDateTime currentTs = Instant.now().atZone(UTC_ZONE_ID);
        final ZonedDateTime resetTs = currentTs.minusHours(currentTs.getHour()).minusMinutes(currentTs.getMinute())
                .minusSeconds(currentTs.getSecond()).minusNanos(currentTs.getNano()).plusDays(1);
        scheduleDailyReset(resetTs, resetLimit);
    }

    private void cancelPendingDailyReset() {
        synchronized (dailyResetLock) {
            final ScheduledFuture<?> ref = dailyResetFuture;
            if (ref != null) {
                ref.cancel(true);
                dailyResetFuture = null;
            }
        }
    }

    private void scheduleDailyReset(final ZonedDateTime resetLimiterDailyTime, final int resetLimit) {
        final ZonedDateTime currentTs = Instant.now().atZone(resetLimiterDailyTime.getZone());
        final long secondsUnitReset = ChronoUnit.SECONDS.between(currentTs, resetLimiterDailyTime);
        synchronized (dailyResetLock) {
            cancelPendingDailyReset();
            dailyResetFuture = scheduler.scheduleWithFixedDelay(() -> {
                resetLimiter(resetLimit);
            }, secondsUnitReset, SECONDS_PER_DAY, TimeUnit.SECONDS);
        }
    }

    private void loadLimiterData() {
        final Storage<String> storage = storageService.getStorage(limiterId, String.class.getClassLoader());
        @Nullable
        final String countStored = storage.get(storageKeyCount);
        @Nullable
        final String tsStored = storage.get(storageKeyTimestamp);
        if (countStored != null && tsStored != null) {
            int newCount = -1;

            try {
                newCount = Integer.parseInt(countStored);
                ZonedDateTime newTs = ZonedDateTime.parse(tsStored, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                final ZonedDateTime currentTs = Instant.now().atZone(UTC_ZONE_ID);
                if (newTs.getDayOfYear() == currentTs.getDayOfYear()) {
                    currentRequestCount = newCount;
                    logger.trace("Limiter {} -> Restored Request Limiter count of {} for day of year {}", limiterId,
                            currentRequestCount, currentTs.getDayOfYear());
                } else {
                    logger.trace("Limiter {} -> Days of saved data are not the same not restoring {} != {}", limiterId,
                            newTs.getDayOfYear(), currentTs.getDayOfYear());
                }
            } catch (DateTimeParseException | NumberFormatException exception) {
                logger.warn("{}",
                        getLocalizedText("api.log.rate-limiter.failed-restore", limiterId, exception.getMessage()),
                        exception);
            }
        }
    }

    private void saveLimiterData() {
        final ZonedDateTime saveTime = Instant.now().atZone(UTC_ZONE_ID);
        final Storage<String> storage = storageService.getStorage(limiterId, String.class.getClassLoader());
        storage.put(storageKeyCount, String.valueOf(currentRequestCount));
        storage.put(storageKeyTimestamp, saveTime.format(DateTimeFormatter.ISO_INSTANT));
        logger.trace("Limiter {} -> Persisted Request Limiter count of {} for date {}", limiterId, currentRequestCount,
                saveTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }

    public void resetLimiter() {
        resetLimiter(requestLimit);
    }

    public synchronized void resetLimiter(int newLimit) {
        requestLimit = newLimit;
        currentRequestCount = 0;
        logger.trace("Limiter {} -> Resetting limiter to 0 used for new limit {}", limiterId, newLimit);
        scheduler.schedule(this::saveLimiterData, 1, TimeUnit.SECONDS);
    }

    public synchronized void updateLimit(int newLimit) {
        requestLimit = newLimit;
        logger.trace("Limiter {} -> Updated limiter to new total limit {}", limiterId, newLimit);
        scheduler.schedule(this::saveLimiterData, 1, TimeUnit.SECONDS);
        scheduleStandardResetTime(newLimit);
    }

    public synchronized int getRequestCountIfAvailable() {
        final int requestId = currentRequestCount;
        if (currentRequestCount < requestLimit) {
            ++currentRequestCount;
            scheduler.schedule(this::saveLimiterData, 1, TimeUnit.SECONDS);
        } else {
            return INVALID_REQUEST_ID;
        }
        return requestId;
    }

    public boolean isInvalidRequestId(final int requestId) {
        return INVALID_REQUEST_ID == requestId;
    }

    // Localization functionality

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }
}
