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

import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.MILLI;
import static org.openhab.core.library.unit.SIUnits.METRE;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.metofficedatahub.internal.api.ISiteResponseListener;
import org.openhab.binding.metofficedatahub.internal.api.ResponseDataProcessor;
import org.openhab.binding.metofficedatahub.internal.dto.responses.SiteApiFeatureCollection;
import org.openhab.binding.metofficedatahub.internal.dto.responses.SiteApiFeatureProperties;
import org.openhab.binding.metofficedatahub.internal.dto.responses.SiteApiTimeSeries;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
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
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MetOfficeDataHubSiteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class MetOfficeDataHubSiteHandler extends BaseThingHandler implements ISiteResponseListener {

    private final Object checkDataRequiredSchedulerLock = new Object();
    private final Object checkDailySchedulerLock = new Object();
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;
    private final LocationProvider locationProvider;
    private final PollManager dailyForecastPollManager;
    private final PollManager hourlyForecastPollManager;

    private volatile MetOfficeDataHubSiteConfiguration config = getConfigAs(MetOfficeDataHubSiteConfiguration.class);

    private PointType location = new PointType();
    private @Nullable ScheduledFuture<?> checkDataRequiredScheduler = null;
    private @Nullable ScheduledFuture<?> dailyScheduler = null;
    private @Nullable ScheduledFuture<?> initTask = null;

    private String dailyPollKey = "";
    private String hourlyPollKey = "";

    public MetOfficeDataHubSiteHandler(Thing thing, @Reference LocationProvider locationProvider,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider,
            @Reference TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.locationProvider = locationProvider;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());

        final ResponseDataProcessor updateHourlyFromCache = new ResponseDataProcessor() {
            @Override
            public void processResponse(final String content) {
                processHourlyContent(content);
            }
        };

        final ResponseDataProcessor updateDailyFromCache = new ResponseDataProcessor() {
            @Override
            public void processResponse(final String content) {
                processDailyContent(content);
            }
        };

        this.hourlyForecastPollManager = new PollManager("Hourly", timeZoneProvider, scheduler, Duration.ofHours(1),
                updateHourlyFromCache, () -> {
                    sendForecastRequest(false);
                });
        this.dailyForecastPollManager = new PollManager("Daily", timeZoneProvider, scheduler, Duration.ofHours(3),
                updateDailyFromCache, () -> {
                    sendForecastRequest(true);
                });
    }

    @Override
    public void dispose() {
        cancelInitTask();
        cancelDataRequiredCheck();
        cancelScheduleDailyDataPoll(true);
        hourlyForecastPollManager.dispose();
        dailyForecastPollManager.dispose();
        super.dispose();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        dailyForecastPollManager.setDataRequired(false, false);
        hourlyForecastPollManager.setDataRequired(false, false);

        config = getConfigAs(MetOfficeDataHubSiteConfiguration.class);

        if (config.location.isBlank()) {
            @Nullable
            PointType userLocation = locationProvider.getLocation();
            if (userLocation == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        getLocalizedText("site.error.no-user-location"));
                return;
            } else {
                location = userLocation;
            }
        } else {
            try {
                location = new PointType(config.location);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        getLocalizedText("site.error.invalid-location"));
                return;
            }
        }

        dailyPollKey = location + ",daily";
        hourlyPollKey = location + ",hourly";

        if (config.hourlyForecastPollRate > 0) {
            hourlyForecastPollManager.setPollDuration(Duration.ofHours(config.hourlyForecastPollRate));
        }
        if (config.dailyForecastPollRate > 0) {
            dailyForecastPollManager.setPollDuration(Duration.ofHours(config.dailyForecastPollRate));
        }

        scheduleInitTask();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);

        handleCommand(channelUID, RefreshType.REFRESH);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        scheduleDataRequiredCheck();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        scheduler.execute(() -> {
            if (RefreshType.REFRESH.equals(command)) {
                scheduleDataRequiredCheck();
            }
        });
    }

    private @Nullable MetOfficeDataHubBridgeHandler getMetOfficeDataHubBridge() {
        Bridge baseBridge = getBridge();

        if (baseBridge != null && baseBridge.getHandler() instanceof MetOfficeDataHubBridgeHandler bridgeHandler) {
            return bridgeHandler;
        } else {
            return null;
        }
    }

    protected boolean requiresPoll() {
        return hourlyForecastPollManager.getIsDataRequired() || dailyForecastPollManager.getIsDataRequired();
    }

    protected void scheduleInitTask() {
        cancelInitTask();
        initTask = scheduler.schedule(() -> {
            MetOfficeDataHubBridgeHandler metBridge = getMetOfficeDataHubBridge();
            if (metBridge != null) {
                updateStatus(metBridge.getThing().getStatus());
            }

            checkDataRequired();
        }, 200, TimeUnit.MILLISECONDS);
    }

    private void cancelInitTask() {
        final ScheduledFuture<?> initTaskRef = initTask;
        if (initTaskRef != null) {
            initTaskRef.cancel(true);
            initTask = null;
        }
    }

    private void checkDataRequired() {
        final List<@Nullable String> activeGroups = getThing().getChannels().stream().filter(x -> isLinked(x.getUID()))
                .map(x -> x.getUID().getGroupId()).distinct().toList();

        if (activeGroups.stream().anyMatch(g -> g != null && g.startsWith(GROUP_PREFIX_DAILY_FORECAST))) {
            dailyForecastPollManager.setDataRequired(true, true);
        } else {
            dailyForecastPollManager.setDataRequired(false, false);
        }

        if (activeGroups.stream().anyMatch(g -> g != null && g.startsWith(GROUP_PREFIX_HOURS_FORECAST))) {
            hourlyForecastPollManager.setDataRequired(true, true);
        } else {
            hourlyForecastPollManager.setDataRequired(false, false);
        }
    }

    private void sendForecastRequest(final boolean daily) {
        MetOfficeDataHubBridgeHandler uplinkBridge = getMetOfficeDataHubBridge();
        if (uplinkBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getLocalizedText("site.error.no-bridge"));
            return;
        }
        final String pollId = (daily) ? dailyPollKey : hourlyPollKey;
        final MetOfficeDataHubBridgeHandler metOfficeBridgeHandler = getMetOfficeDataHubBridge();
        if (metOfficeBridgeHandler != null) {
            if (!metOfficeBridgeHandler.getSiteApi().sendRequest(daily, location, this, pollId)) {
                if (daily) {
                    dailyForecastPollManager.cachedPollOrLiveStart(false);
                } else {
                    hourlyForecastPollManager.cachedPollOrLiveStart(false);
                }
            }
        }
    }

    /**
     * Scheduler to evaluate which data is required to be polled from
     * the APIs
     */
    private void scheduleDataRequiredCheck() {
        synchronized (checkDataRequiredSchedulerLock) {
            cancelDataRequiredCheck();
            checkDataRequiredScheduler = scheduler.schedule(this::checkDataRequired, 2, TimeUnit.SECONDS);
        }
    }

    private void cancelDataRequiredCheck() {
        synchronized (checkDataRequiredSchedulerLock) {
            ScheduledFuture<?> job = checkDataRequiredScheduler;
            if (job != null) {
                job.cancel(true);
                checkDataRequiredScheduler = null;
            }
        }
    }

    private void cancelScheduleDailyDataPoll(final boolean allowInterrupt) {
        synchronized (checkDailySchedulerLock) {
            ScheduledFuture<?> job = dailyScheduler;
            if (job != null) {
                job.cancel(allowInterrupt);
                dailyScheduler = null;
            }
        }
    }

    // Localization functionality

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    // Implementation of ISiteResponseListener and associated methods

    @Override
    public void processDailyResponse(final String responseData, final String pollId) {
        if (dailyPollKey.equals(pollId)) {
            dailyForecastPollManager.setDataContentReceived(responseData);
            processDailyContent(responseData);
        }
    }

    public void processDailyContent(final String responseData) {
        final SiteApiFeatureCollection response = GSON.fromJson(responseData, SiteApiFeatureCollection.class);

        if (response == null) {
            return;
        }

        final SiteApiFeatureProperties props = response.getFirstProperties();
        if (props == null) {
            return;
        }

        final String startOfHour = MetOfficeDataHubSiteHandler.getDataTsAtLastChronoUnit(ChronoUnit.DAYS);
        final int forecastForthisHour = props.getHourlyTimeSeriesPositionForCurrentHour(startOfHour);

        for (int dayOffset = 0; dayOffset <= 6; ++dayOffset) {
            // Calculate the correct array position for the data
            final int dataIdx = (forecastForthisHour != -1) ? forecastForthisHour + dayOffset : -1;

            final String channelPrefix = MetOfficeDataHubSiteHandler.calculatePrefix(GROUP_PREFIX_DAILY_FORECAST,
                    dayOffset);

            final SiteApiTimeSeries data = props.getTimeSeries(dataIdx);

            updateState(channelPrefix + SITE_TIMESTAMP, getDateTimeTypeState(data.getTime()));

            updateState(channelPrefix + SITE_DAILY_MIDDAY_WIND_SPEED_10M,
                    getQuantityTypeState(data.getMidday10MWindSpeed(), Units.METRE_PER_SECOND));

            updateState(channelPrefix + SITE_DAILY_MIDNIGHT_WIND_SPEED_10M,
                    getQuantityTypeState(data.getMidnight10MWindSpeed(), Units.METRE_PER_SECOND));

            updateState(channelPrefix + SITE_DAILY_MIDDAY_WIND_DIRECTION_10M,
                    getQuantityTypeState(data.getMidday10MWindDirection(), Units.DEGREE_ANGLE));

            updateState(channelPrefix + SITE_DAILY_MIDNIGHT_WIND_DIRECTION_10M,
                    getQuantityTypeState(data.getMidnight10MWindDirection(), Units.DEGREE_ANGLE));

            updateState(channelPrefix + SITE_DAILY_MIDDAY_WIND_GUST_10M,
                    getQuantityTypeState(data.getMidday10MWindGust(), Units.METRE_PER_SECOND));

            updateState(channelPrefix + SITE_DAILY_MIDNIGHT_WIND_GUST_10M,
                    getQuantityTypeState(data.getMidnight10MWindGust(), Units.METRE_PER_SECOND));

            updateState(channelPrefix + SITE_DAILY_MIDDAY_VISIBILITY,
                    getQuantityTypeState(data.getMiddayVisibility(), METRE));

            updateState(channelPrefix + SITE_DAILY_MIDNIGHT_VISIBILITY,
                    getQuantityTypeState(data.getMidnightVisibility(), METRE));

            updateState(channelPrefix + SITE_DAILY_MIDDAY_REL_HUMIDITY,
                    getQuantityTypeState(data.getMiddayRelativeHumidity(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_MIDNIGHT_REL_HUMIDITY,
                    getQuantityTypeState(data.getMidnightRelativeHumidity(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_MIDDAY_PRESSURE,
                    getQuantityTypeState(data.getMiddayPressure(), SIUnits.PASCAL));

            updateState(channelPrefix + SITE_DAILY_MIDNIGHT_PRESSURE,
                    getQuantityTypeState(data.getMidnightPressure(), SIUnits.PASCAL));

            updateState(channelPrefix + SITE_DAILY_DAY_MAX_UV_INDEX, getDecimalTypeState(data.getMaxUvIndex()));

            updateState(channelPrefix + SITE_DAILY_DAY_UPPER_BOUND_MAX_TEMP,
                    getQuantityTypeState(data.getDayUpperBoundMaxTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_DAY_LOWER_BOUND_MAX_TEMP,
                    getQuantityTypeState(data.getDayLowerBoundMaxTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_NIGHT_UPPER_BOUND_MAX_TEMP,
                    getQuantityTypeState(data.getNightUpperBoundMinTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_NIGHT_LOWER_BOUND_MAX_TEMP,
                    getQuantityTypeState(data.getNightLowerBoundMinTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_NIGHT_FEELS_LIKE_MIN_TEMP,
                    getQuantityTypeState(data.getNightMinFeelsLikeTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_DAY_FEELS_LIKE_MAX_TEMP,
                    getQuantityTypeState(data.getDayMaxFeelsLikeTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_NIGHT_LOWER_BOUND_MIN_TEMP,
                    getQuantityTypeState(data.getNightLowerBoundMinTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_DAY_MAX_FEELS_LIKE_TEMP,
                    getQuantityTypeState(data.getDayMaxFeelsLikeTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_NIGHT_LOWER_BOUND_MIN_FEELS_LIKE_TEMP,
                    getQuantityTypeState(data.getNightLowerBoundMinFeelsLikeTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_DAY_LOWER_BOUND_MAX_FEELS_LIKE_TEMP,
                    getQuantityTypeState(data.getDayLowerBoundMaxFeelsLikeTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_DAY_UPPER_BOUND_MAX_FEELS_LIKE_TEMP,
                    getQuantityTypeState(data.getDayUpperBoundMaxFeelsLikeTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_UPPER_BOUND_MIN_FEELS_LIKE_TEMP,
                    getQuantityTypeState(data.getNightUpperBoundMinFeelsLikeTemp(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_DAY_MAX_SCREEN_TEMPERATURE,
                    getQuantityTypeState(data.getDayMaxScreenTemperature(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_NIGHT_MIN_SCREEN_TEMPERATURE,
                    getQuantityTypeState(data.getNightMinScreenTemperature(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_DAILY_DAY_PROBABILITY_OF_PRECIPITATION,
                    getQuantityTypeState(data.getDayProbabilityOfPrecipitation(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_NIGHT_PROBABILITY_OF_PRECIPITATION,
                    getQuantityTypeState(data.getNightProbabilityOfPrecipitation(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_DAY_PROBABILITY_OF_SNOW,
                    getQuantityTypeState(data.getDayProbabilityOfSnow(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_NIGHT_PROBABILITY_OF_SNOW,
                    getQuantityTypeState(data.getNightProbabilityOfSnow(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_DAY_PROBABILITY_OF_HEAVY_SNOW,
                    getQuantityTypeState(data.getDayProbabilityOfHeavySnow(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_NIGHT_PROBABILITY_OF_HEAVY_SNOW,
                    getQuantityTypeState(data.getNightProbabilityOfHeavySnow(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_DAY_PROBABILITY_OF_RAIN,
                    getQuantityTypeState(data.getDayProbabilityOfRain(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_NIGHT_PROBABILITY_OF_RAIN,
                    getQuantityTypeState(data.getNightProbabilityOfRain(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_DAY_PROBABILITY_OF_HEAVY_RAIN,
                    getQuantityTypeState(data.getDayProbabilityOfHeavyRain(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_NIGHT_PROBABILITY_OF_HEAVY_RAIN,
                    getQuantityTypeState(data.getNightProbabilityOfHeavyRain(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_DAY_PROBABILITY_OF_HAIL,
                    getQuantityTypeState(data.getDayProbabilityOfHail(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_NIGHT_PROBABILITY_OF_HAIL,
                    getQuantityTypeState(data.getNightProbabilityOfHail(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_DAY_PROBABILITY_OF_SFERICS,
                    getQuantityTypeState(data.getDayProbabilityOfSferics(), Units.PERCENT));

            updateState(channelPrefix + SITE_DAILY_NIGHT_PROBABILITY_OF_SFERICS,
                    getQuantityTypeState(data.getNightProbabilityOfSferics(), Units.PERCENT));
        }
    }

    @Override
    public void processHourlyResponse(final String responseData, final String pollId) {
        if (hourlyPollKey.equals(pollId)) {
            hourlyForecastPollManager.setDataContentReceived(responseData);
            processHourlyContent(responseData);
        }
    }

    public void processHourlyContent(final String responseData) {
        final SiteApiFeatureCollection response = GSON.fromJson(responseData, SiteApiFeatureCollection.class);

        if (response == null) {
            return;
        }

        final SiteApiFeatureProperties props = response.getFirstProperties();
        if (props == null) {
            return;
        }

        final String startOfHour = MetOfficeDataHubSiteHandler.getDataTsAtLastChronoUnit(ChronoUnit.HOURS);
        final int forecastForthisHour = props.getHourlyTimeSeriesPositionForCurrentHour(startOfHour);

        for (int hrOffset = 0; hrOffset <= 24; ++hrOffset) {
            // Calculate the correct array position for the data
            final int dataIdx = (forecastForthisHour != -1) ? forecastForthisHour + hrOffset : -1;
            final SiteApiTimeSeries data = props.getTimeSeries(dataIdx);

            final String channelPrefix = MetOfficeDataHubSiteHandler.calculatePrefix(GROUP_PREFIX_HOURS_FORECAST,
                    hrOffset);

            updateState(channelPrefix + SITE_TIMESTAMP, getDateTimeTypeState(data.getTime()));

            updateState(channelPrefix + SITE_HOURLY_FORECAST_SCREEN_TEMPERATURE,
                    getQuantityTypeState(data.getScreenTemperature(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_HOURLY_FORECAST_MIN_SCREEN_TEMPERATURE,
                    getQuantityTypeState(data.getMinScreenTemperature(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_HOURLY_FORECAST_MAX_SCREEN_TEMPERATURE,
                    getQuantityTypeState(data.getMaxScreenTemperature(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_HOURLY_FEELS_LIKE_TEMPERATURE,
                    getQuantityTypeState(data.getFeelsLikeTemperature(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_HOURLY_SCREEN_RELATIVE_HUMIDITY,
                    getQuantityTypeState(data.getScreenRelativeHumidity(), Units.PERCENT));

            updateState(channelPrefix + SITE_HOURLY_VISIBILITY, getQuantityTypeState(data.getVisibility(), METRE));

            updateState(channelPrefix + SITE_HOURLY_PROBABILITY_OF_PRECIPITATION,
                    getQuantityTypeState(data.getProbOfPrecipitation(), Units.PERCENT));

            updateState(channelPrefix + SITE_HOURLY_PRECIPITATION_RATE,
                    getQuantityTypeState(data.getPrecipitationRate(), Units.MILLIMETRE_PER_HOUR));

            updateState(channelPrefix + SITE_HOURLY_TOTAL_PRECIPITATION_AMOUNT,
                    getQuantityTypeState(data.getTotalPrecipAmount(), MILLI(METRE)));

            updateState(channelPrefix + SITE_HOURLY_TOTAL_SNOW_AMOUNT,
                    getQuantityTypeState(data.getTotalSnowAmount(), MILLI(METRE)));

            updateState(channelPrefix + SITE_HOURLY_PRESSURE, getQuantityTypeState(data.getPressure(), SIUnits.PASCAL));

            updateState(channelPrefix + SITE_HOURLY_WIND_SPEED_10M,
                    getQuantityTypeState(data.getWindSpeed10m(), Units.METRE_PER_SECOND));

            updateState(channelPrefix + SITE_HOURLY_MAX_10M_WIND_GUST,
                    getQuantityTypeState(data.getMax10mWindGust(), Units.METRE_PER_SECOND));

            updateState(channelPrefix + SITE_HOURLY_WIND_GUST_SPEED_10M,
                    getQuantityTypeState(data.getWindGustSpeed10m(), Units.METRE_PER_SECOND));

            updateState(channelPrefix + SITE_HOURLY_SCREEN_DEW_POINT_TEMPERATURE,
                    getQuantityTypeState(data.getScreenDewPointTemperature(), SIUnits.CELSIUS));

            updateState(channelPrefix + SITE_HOURLY_UV_INDEX, getDecimalTypeState(data.getUvIndex()));

            updateState(channelPrefix + SITE_HOURLY_WIND_DIRECTION_FROM_10M,
                    getQuantityTypeState(data.getWindDirectionFrom10m(), Units.DEGREE_ANGLE));
        }
    }

    public static String getDataTsAtLastChronoUnit(final ChronoUnit unit) {
        return Instant.now().truncatedTo(unit).toString().substring(0, 16) + "Z";
    }

    // Helpers for updating channels support

    private static String calculatePrefix(final String prefix, final int plusOffset) {
        final StringBuilder strBldr = new StringBuilder(26);
        strBldr.append(prefix);
        if (plusOffset > 0) {
            strBldr.append(GROUP_POSTFIX_BOTH_FORECASTS);
            if (plusOffset < 10) {
                strBldr.append("0");
            }
            strBldr.append(plusOffset);
        }
        strBldr.append(GROUP_PREFIX_TO_ITEM);
        return strBldr.toString();
    }

    protected State getDateTimeTypeState(@Nullable String value) {
        return (value == null) ? UnDefType.UNDEF : new DateTimeType(value).toLocaleZone();
    }

    protected State getQuantityTypeState(@Nullable Number value, Unit<?> unit) {
        return (value == null) ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    protected State getDecimalTypeState(@Nullable Number value) {
        return (value == null) ? UnDefType.UNDEF : new DecimalType(value);
    }
}
