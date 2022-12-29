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
package org.openhab.binding.ecowatt.internal.handler;

import static org.openhab.binding.ecowatt.internal.EcowattBindingConstants.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.ecowatt.internal.configuration.EcowattConfiguration;
import org.openhab.binding.ecowatt.internal.exception.EcowattApiLimitException;
import org.openhab.binding.ecowatt.internal.restapi.EcowattApiResponse;
import org.openhab.binding.ecowatt.internal.restapi.EcowattDaySignals;
import org.openhab.binding.ecowatt.internal.restapi.EcowattRestApi;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcowattHandler} is responsible for updating the state of the channels
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class EcowattHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EcowattHandler.class);

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private final TranslationProvider i18nProvider;
    private final TimeZoneProvider timeZoneProvider;
    private final Bundle bundle;

    private @Nullable EcowattRestApi api;
    private ExpiringCache<EcowattApiResponse> cachedApiResponse = new ExpiringCache<>(Duration.ofHours(4),
            this::getApiResponse); // cache the API response during 4 hours

    private @Nullable ScheduledFuture<?> updateJob;

    public EcowattHandler(Thing thing, OAuthFactory oAuthFactory, HttpClient httpClient,
            TranslationProvider i18nProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
        this.i18nProvider = i18nProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.bundle = FrameworkUtil.getBundle(this.getClass());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            updateChannel(channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        EcowattConfiguration config = getConfigAs(EcowattConfiguration.class);

        final String idClient = config.idClient;
        final String idSecret = config.idSecret;

        if (idClient.isBlank() || idSecret.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-unset-parameters");
        } else {
            api = new EcowattRestApi(oAuthFactory, httpClient, thing.getUID().getAsString(), idClient, idSecret);
            updateStatus(ThingStatus.UNKNOWN);
            scheduleNextUpdate(0, true);
        }
    }

    @Override
    public void dispose() {
        stopScheduledJob();
        EcowattRestApi localApi = api;
        if (localApi != null) {
            localApi.dispose();
            api = null;
        }
    }

    /**
     * Schedule the next update of channels.
     *
     * After this update is run, a new update will be rescheduled, either just after the API is reachable again or at
     * the beginning of the following hour.
     *
     * @param delayInSeconds the delay in seconds before running the next update
     * @param retryIfApiLimitReached true if a retry is expected when the update fails due to reached API limit
     */
    private void scheduleNextUpdate(long delayInSeconds, boolean retryIfApiLimitReached) {
        logger.debug("scheduleNextUpdate delay={}s retryIfLimitReached={}", delayInSeconds, retryIfApiLimitReached);
        updateJob = scheduler.schedule(() -> {
            int retryDelay = updateChannels(retryIfApiLimitReached);
            long delayNextUpdate;
            if (retryDelay > 0) {
                // Schedule a new update just after the API is reachable again
                logger.debug("retryDelay {}", retryDelay);
                delayNextUpdate = retryDelay;
            } else {
                // Schedule a new update at the beginning of the following hour
                final LocalDateTime now = LocalDateTime.now();
                final LocalDateTime beginningNextHour = now.plusHours(1).truncatedTo(ChronoUnit.HOURS);
                delayNextUpdate = ChronoUnit.SECONDS.between(now, beginningNextHour);
            }
            // Add 3s of additional delay for security...
            delayNextUpdate += 3;
            scheduleNextUpdate(delayNextUpdate, retryDelay == 0);
        }, delayInSeconds, TimeUnit.SECONDS);
    }

    private void stopScheduledJob() {
        ScheduledFuture<?> job = updateJob;
        if (job != null) {
            job.cancel(true);
            updateJob = null;
        }
    }

    private EcowattApiResponse getApiResponse() {
        EcowattRestApi localApi = api;
        if (localApi == null) {
            return new EcowattApiResponse();
        }

        EcowattApiResponse response;
        try {
            response = localApi.getSignals();
        } catch (CommunicationException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                logger.warn("{}: {}", e.getMessage(bundle, i18nProvider), cause.getMessage());
            } else {
                logger.warn("{}", e.getMessage(bundle, i18nProvider));
            }
            response = new EcowattApiResponse(e);
        }
        return response;
    }

    private int updateChannels(boolean retryIfApiLimitReached) {
        return updateChannel(null, retryIfApiLimitReached);
    }

    private void updateChannel(String channelId) {
        updateChannel(channelId, false);
    }

    private synchronized int updateChannel(@Nullable String channelId, boolean retryIfApiLimitReached) {
        logger.debug("updateChannel channelId={}, retryIfApiLimitReached={}", channelId, retryIfApiLimitReached);
        int retryDelay = 0;
        EcowattApiResponse response = cachedApiResponse.getValue();
        if (response == null || !response.succeeded()) {
            CommunicationException exception = response == null ? null : response.getException();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    exception == null ? null : exception.getRawMessage());

            // Invalidate the cache to be sure the next request will trigger the API
            cachedApiResponse.invalidateValue();

            if (retryIfApiLimitReached && exception instanceof EcowattApiLimitException
                    && ((EcowattApiLimitException) exception).getRetryAfter() > 0) {
                // Will retry when the API is available again (just after the limit expired)
                retryDelay = ((EcowattApiLimitException) exception).getRetryAfter();
            }
        } else {
            updateStatus(ThingStatus.ONLINE);
        }

        ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone());
        logger.debug("now {}", now.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        if ((channelId == null || CHANNEL_TODAY_SIGNAL.equals(channelId)) && isLinked(CHANNEL_TODAY_SIGNAL)) {
            updateState(CHANNEL_TODAY_SIGNAL, getDaySignalState(response, now));
        }
        if ((channelId == null || CHANNEL_TOMORROW_SIGNAL.equals(channelId)) && isLinked(CHANNEL_TOMORROW_SIGNAL)) {
            updateState(CHANNEL_TOMORROW_SIGNAL, getDaySignalState(response, now.plusDays(1)));
        }
        if ((channelId == null || CHANNEL_IN_TWO_DAYS_SIGNAL.equals(channelId))
                && isLinked(CHANNEL_IN_TWO_DAYS_SIGNAL)) {
            updateState(CHANNEL_IN_TWO_DAYS_SIGNAL, getDaySignalState(response, now.plusDays(2)));
        }
        if ((channelId == null || CHANNEL_IN_THREE_DAYS_SIGNAL.equals(channelId))
                && isLinked(CHANNEL_IN_THREE_DAYS_SIGNAL)) {
            updateState(CHANNEL_IN_THREE_DAYS_SIGNAL, getDaySignalState(response, now.plusDays(3)));
        }
        if ((channelId == null || CHANNEL_CURRENT_HOUR_SIGNAL.equals(channelId))
                && isLinked(CHANNEL_CURRENT_HOUR_SIGNAL)) {
            updateState(CHANNEL_CURRENT_HOUR_SIGNAL, getHourSignalState(response, now));
        }

        return retryDelay;
    }

    /**
     * Get the signal applicable for a given day from the API response
     *
     * @param response the API response
     * @param dateTime the date and time to consider
     * @return the found valid signal as a channel state or UndefType.UNDEF if not found
     */
    public static State getDaySignalState(@Nullable EcowattApiResponse response, ZonedDateTime dateTime) {
        EcowattDaySignals signals = response == null ? null : response.getDaySignals(dateTime);
        return signals != null && signals.getDaySignal() >= 1 && signals.getDaySignal() <= 3
                ? new DecimalType(signals.getDaySignal())
                : UnDefType.UNDEF;
    }

    /**
     * Get the signal applicable for a given day and hour from the API response
     *
     * @param response the API response
     * @param dateTime the date and time to consider
     * @return the found valid signal as a channel state or UndefType.UNDEF if not found
     */
    public static State getHourSignalState(@Nullable EcowattApiResponse response, ZonedDateTime dateTime) {
        EcowattDaySignals signals = response == null ? null : response.getDaySignals(dateTime);
        ZonedDateTime day = signals == null ? null : signals.getDay();
        if (signals != null && day != null) {
            // Move the current time to the same offset as the data returned by the API to get and use the right current
            // hour index in these data
            int hour = dateTime.withZoneSameInstant(day.getZone()).getHour();
            int value = signals.getHourSignal(hour);
            LoggerFactory.getLogger(EcowattHandler.class).debug("hour {} value {}", hour, value);
            if (value >= 1 && value <= 3) {
                return new DecimalType(value);
            }
        }
        return UnDefType.UNDEF;
    }
}
