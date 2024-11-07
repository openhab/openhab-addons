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
package org.openhab.binding.metofficedatahub.internal;

import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.BRIDGE_PROP_FORECAST_REQUEST_COUNT;
import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.DAY_IN_MILLIS;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.openhab.binding.metofficedatahub.internal.api.RequestLimiter;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MetOfficeDataHubBridgeHandler} models the account(s) to the MetOfficeDataHub services.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class MetOfficeDataHubBridgeHandler extends BaseBridgeHandler {

    private volatile MetOfficeDataHubBridgeConfiguration config = getConfigAs(
            MetOfficeDataHubBridgeConfiguration.class);

    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;
    private final HttpClient httpClient;
    private final StorageService storageService;
    private final TimeZoneProvider timeZoneProvider;

    private final Logger logger = LoggerFactory.getLogger(MetOfficeDataHubBridgeHandler.class);
    private final Object timerResetSchedulerLock = new Object();

    private @Nullable ScheduledFuture<?> timerResetScheduler = null;
    private @Nullable ScheduledFuture initTask;

    protected final RequestLimiter forecastDataLimiter;

    public MetOfficeDataHubBridgeHandler(final Bridge bridge, IHttpClientProvider httpClientProvider,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider,
            @Reference StorageService storageService, @Reference TimeZoneProvider timeZoneProvider) {
        super(bridge);
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
        this.httpClient = httpClientProvider.getHttpClient();
        this.storageService = storageService;
        this.timeZoneProvider = timeZoneProvider;
        this.forecastDataLimiter = new RequestLimiter(getThing().getUID().getId(), storageService, timeZoneProvider,
                scheduler, translationProvider, localeProvider, bundle);
    }

    protected String getApiKey() {
        return config.siteApiKey;
    }

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    public void updateLimiterStats() {
        final Map<String, String> newProps = new HashMap<>();
        newProps.put(BRIDGE_PROP_FORECAST_REQUEST_COUNT, String.valueOf(forecastDataLimiter.getCurrentRequestCount()));
        this.updateProperties(newProps);
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
        config = getConfigAs(MetOfficeDataHubBridgeConfiguration.class);
        forecastDataLimiter.updateLimit(config.siteRateDailyLimit);

        updateStatus(ThingStatus.UNKNOWN);
        scheduleResetDailyLimiters();

        initTask = scheduler.schedule(() -> {
            final PointType siteApiTestLocation = new PointType("51.5072,0.1276");
            updateLimiterStats();

            final Response.CompleteListener siteResponseListener = new BufferingResponseListener() { // 4.5kb buffer
                @Override
                public void onComplete(@Nullable Result result) {
                    if (result != null && !result.isFailed()) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                getLocalizedText("bridge.error.site-specific.auth-issue"));
                    }
                }
            };
            MetOfficeDataHubSiteHandler.sendAsyncSiteApiRequest(httpClient, true, getApiKey(), siteApiTestLocation,
                    siteResponseListener);
            initTask = null;
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        cancelResetDailyLimiters();
        if (initTask != null) {
            initTask.cancel(true);
            initTask = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Forecast Data Limiter Reset Scheduling
     */

    private void scheduleResetDailyLimiters() {
        logger.trace("Scheduling reset of forecast data limiter");
        cancelResetDailyLimiters();
        long delayUntilResetCounters = getMillisUntilMidnight();
        synchronized (timerResetSchedulerLock) {
            timerResetScheduler = scheduler.scheduleWithFixedDelay(() -> {
                logger.trace("Resetting forecast request data limiter");
                forecastDataLimiter.resetLimiter();
            }, delayUntilResetCounters, DAY_IN_MILLIS, TimeUnit.MILLISECONDS);
        }
        logger.trace("Scheduled reset of forecast data limiter complete");
    }

    private void cancelResetDailyLimiters() {
        synchronized (timerResetSchedulerLock) {
            ScheduledFuture<?> job = timerResetScheduler;
            if (job != null) {
                job.cancel(true);
                timerResetScheduler = null;
                logger.trace("Cancelled scheduled reset of forecast data limiter");
            }
        }
    }
}
