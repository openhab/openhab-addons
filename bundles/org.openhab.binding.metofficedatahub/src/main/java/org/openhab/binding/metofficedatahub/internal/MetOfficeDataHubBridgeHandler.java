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
package org.openhab.binding.metofficedatahub.internal;

import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.BRIDGE_PROP_FORECAST_REQUEST_COUNT;
import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.DAY_IN_MILLIS;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.org.openhab.binding.metofficedatahub.internal.RequestLimiter;

/**
 * The {@link MetOfficeDataHubBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class MetOfficeDataHubBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(MetOfficeDataHubBridgeHandler.class);

    public main.java.org.openhab.binding.metofficedatahub.internal.RequestLimiter forecastDataLimiter = new RequestLimiter();

    public MetOfficeDataHubBridgeHandler(final Bridge bridge) {
        super(bridge);
    }

    public void updateLimiterStats() {
        final Map<String, String> newProps = new HashMap<>();
        newProps.put(BRIDGE_PROP_FORECAST_REQUEST_COUNT, String.valueOf(forecastDataLimiter.getCurrentRequestCount()));
        this.updateProperties(newProps);
    }

    private String configuredClientId = "";
    private String configuredClientSecret = "";

    protected String getClientId() {
        return configuredClientId;
    }

    protected String getClientSecret() {
        return configuredClientSecret;
    }

    private static long getMillisUntilMidnight() {
        return Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay()).toMillis();
    }

    protected static long getMillisSinceDayStart() {
        return Duration.between(LocalDate.now().atStartOfDay(), LocalDateTime.now()).toMillis();
    }

    @Override
    public void initialize() {
        updateLimiterStats();
        final MetOfficeDataHubBridgeConfiguration config = getConfigAs(MetOfficeDataHubBridgeConfiguration.class);
        forecastDataLimiter.updateLimit(config.siteSpecificRateDailyLimit);
        configuredClientId = config.siteSpecificClientId;
        configuredClientSecret = config.siteSpecificClientSecret;

        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true;
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        scheduleResetDailyLimiters();
    }

    @Override
    public void dispose() {
        cancelResetDailyLimiters();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Forecast Data Limiter Reset Scheduling
     */

    @Nullable
    private ScheduledFuture<?> timerResetScheduler = null;
    private final Object timerResetSchedulerLock = new Object();

    private void scheduleResetDailyLimiters() {
        logger.debug("Scheduling reset of forecast data limiter");
        cancelResetDailyLimiters();
        long delayUntilResetCounters = getMillisUntilMidnight();
        synchronized (timerResetSchedulerLock) {
            timerResetScheduler = scheduler.scheduleWithFixedDelay(() -> {
                logger.debug("Resetting forecast request data limiter");
                forecastDataLimiter.resetLimiter();
            }, delayUntilResetCounters, DAY_IN_MILLIS, TimeUnit.MILLISECONDS);
        }
        logger.debug("Scheduled reset of forecast data limiter complete");
    }

    private void cancelResetDailyLimiters() {
        synchronized (timerResetSchedulerLock) {
            ScheduledFuture<?> job = timerResetScheduler;
            if (job != null) {
                job.cancel(true);
                timerResetScheduler = null;
                logger.debug("Cancelled scheduled reset of forecast data limiter");
            }
        }
    }
}
