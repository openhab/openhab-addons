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
package org.openhab.binding.visualcrossing.internal;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.visualcrossing.internal.TypeBuilder.*;
import static org.openhab.binding.visualcrossing.internal.VisualCrossingBindingConstants.Channels.BasicChannelGroup.*;
import static org.openhab.binding.visualcrossing.internal.VisualCrossingBindingConstants.Channels.CurrentConditions.*;
import static org.openhab.binding.visualcrossing.internal.VisualCrossingBindingConstants.SUPPORTED_LANGUAGES;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;
import static org.openhab.core.types.RefreshType.REFRESH;
import static org.openhab.core.types.UnDefType.UNDEF;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.visualcrossing.internal.VisualCrossingBindingConstants.Channels.ChannelDay;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingApi;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingApi.UnitGroup;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingApiException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingAuthException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingRateException;
import org.openhab.binding.visualcrossing.internal.api.dto.CurrentConditions;
import org.openhab.binding.visualcrossing.internal.api.dto.Day;
import org.openhab.binding.visualcrossing.internal.api.dto.Hour;
import org.openhab.binding.visualcrossing.internal.api.dto.WeatherResponse;
import org.openhab.binding.visualcrossing.internal.api.rest.ApiClient;
import org.openhab.binding.visualcrossing.internal.api.rest.RestClient;
import org.openhab.binding.visualcrossing.internal.api.rest.RetryHttpClient;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link VisualCrossingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class VisualCrossingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VisualCrossingHandler.class);

    private final HttpClientFactory httpClientFactory;
    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;
    private final AtomicReference<@Nullable WeatherResponse> weatherResponse = new AtomicReference<>();
    @Nullable
    private VisualCrossingApi api;
    @Nullable
    private String location;
    @Nullable
    private String lang;
    @Nullable
    private ScheduledFuture<?> schedule;

    public VisualCrossingHandler(Thing thing, HttpClientFactory httpClientFactory, LocaleProvider localeProvider,
            LocationProvider locationProvider) {
        super(thing);
        this.httpClientFactory = httpClientFactory;
        this.localeProvider = localeProvider;
        this.locationProvider = locationProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            // handling only refresh commands
            return;
        }
        updateState(channelUID, findState(channelUID, this.weatherResponse.get()));
    }

    private State findState(ChannelUID channelUID, @Nullable WeatherResponse weatherResponse) {
        var channelId = channelUID.getId();
        { // base
            if (COST.equals(channelId)) {
                var localApi = api;
                if (localApi == null) {
                    return UNDEF;
                }
                return new DecimalType(localApi.getCurrentCost());
            }
            if (DESCRIPTION.equals(channelId)) {
                return newStringType(weatherResponse, WeatherResponse::description);
            }
        }
        { // current conditions
            var currentConditions = ofNullable(weatherResponse).map(WeatherResponse::currentConditions);
            switch (channelId) {
                case DATE_TIME -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newStringType(cc, CurrentConditions::datetime)).orElse(UNDEF));
                }
                case TIME_STAMP -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newDateTimeType(cc, CurrentConditions::datetimeEpoch)).orElse(UNDEF));
                }
                case TEMPERATURE -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newTemperatureType(cc, CurrentConditions::temp)).orElse(UNDEF));
                }
                case FEELS_LIKE -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newTemperatureType(cc, CurrentConditions::feelslike)).orElse(UNDEF));
                }
                case HUMIDITY -> {
                    return requireNonNull(currentConditions.map(cc -> newHumidityType(cc, CurrentConditions::humidity))
                            .orElse(UNDEF));
                }
                case DEW -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newTemperatureType(cc, CurrentConditions::dew)).orElse(UNDEF));
                }
                case PRECIP -> {
                    return requireNonNull(currentConditions.map(cc -> newMilliLengthType(cc, CurrentConditions::precip))
                            .orElse(UNDEF));
                }
                case PRECIP_PROB -> {
                    return requireNonNull(currentConditions.map(cc -> newPercentType(cc, CurrentConditions::precipprob))
                            .orElse(UNDEF));
                }
                case PRECIP_TYPE -> {
                    return requireNonNull(currentConditions.map(cc -> newStringType(cc, CurrentConditions::preciptype))
                            .orElse(UNDEF));
                }
                case SNOW -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newCentiLengthType(cc, CurrentConditions::snow)).orElse(UNDEF));
                }
                case SNOW_DEPTH -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newCentiLengthType(cc, CurrentConditions::snowdepth)).orElse(UNDEF));
                }
                case WIND_GUST -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newSpeedType(cc, CurrentConditions::windgust)).orElse(UNDEF));
                }
                case WIND_SPEED -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newSpeedType(cc, CurrentConditions::windspeed)).orElse(UNDEF));
                }
                case WIND_DIR -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newAngleType(cc, CurrentConditions::winddir)).orElse(UNDEF));
                }
                case PRESSURE -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newMilliPressureType(cc, CurrentConditions::pressure)).orElse(UNDEF));
                }
                case VISIBILITY -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newKiloMeterType(cc, CurrentConditions::visibility)).orElse(UNDEF));
                }
                case CLOUD_COVER -> {
                    return requireNonNull(currentConditions.map(cc -> newPercentType(cc, CurrentConditions::cloudcover))
                            .orElse(UNDEF));
                }
                case SOLAR_RADIATION -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newSolarRadiationType(cc, CurrentConditions::solarradiation)).orElse(UNDEF));
                }
                case SOLAR_ENERGY -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newSolarEnergyType(cc, CurrentConditions::solarenergy)).orElse(UNDEF));
                }
                case UV_INDEX -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newDecimalType(cc, CurrentConditions::uvindex)).orElse(UNDEF));
                }
                case CONDITIONS -> {
                    return requireNonNull(currentConditions.map(cc -> newStringType(cc, CurrentConditions::conditions))
                            .orElse(UNDEF));
                }
                case ICON -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newStringType(cc, CurrentConditions::icon)).orElse(UNDEF));
                }
                case STATIONS -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newStringCollectionType(cc, CurrentConditions::stations)).orElse(UNDEF));
                }
                case SOURCE -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newStringType(cc, CurrentConditions::source)).orElse(UNDEF));
                }
                case SUNRISE -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newStringType(cc, CurrentConditions::sunrise)).orElse(UNDEF));
                }
                case SUNRISE_EPOCH -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newDateTimeType(cc, CurrentConditions::sunriseEpoch)).orElse(UNDEF));
                }
                case SUNSET -> {
                    return requireNonNull(
                            currentConditions.map(cc -> newStringType(cc, CurrentConditions::sunset)).orElse(UNDEF));
                }
                case SUNSET_EPOCH -> {
                    return requireNonNull(currentConditions
                            .map(cc -> newDateTimeType(cc, CurrentConditions::sunsetEpoch)).orElse(UNDEF));
                }
                case MOON_PHASE -> {
                    return requireNonNull(currentConditions.map(cc -> newDecimalType(cc, CurrentConditions::moonphase))
                            .orElse(UNDEF));
                }
            }
        }
        { // days
            var days = ofNullable(weatherResponse).map(WeatherResponse::days).orElse(List.of());
            for (var dayIdx = 1; dayIdx <= ChannelDay.NR_OF_DAYS; dayIdx++) {
                Day weatherDay;
                if (dayIdx - 1 < days.size()) {
                    weatherDay = days.get(dayIdx - 1);
                } else {
                    // there is no day with given dayIdx ; need to null the channel
                    weatherDay = null;
                }
                var day = new ChannelDay(dayIdx);
                if (channelId.equals(day.dateTime())) {
                    return newStringType(weatherDay, Day::datetime);
                }
                if (channelId.equals(day.timeStamp())) {
                    return newDateTimeType(weatherDay, Day::datetimeEpoch);
                }
                if (channelId.equals(day.temperature())) {
                    return newTemperatureType(weatherDay, Day::temp);
                }
                if (channelId.equals(day.temperatureMin())) {
                    return newTemperatureType(weatherDay, Day::tempmin);
                }
                if (channelId.equals(day.temperatureMax())) {
                    return newTemperatureType(weatherDay, Day::tempmax);
                }
                if (channelId.equals(day.feelsLike())) {
                    return newTemperatureType(weatherDay, Day::feelslike);
                }
                if (channelId.equals(day.feelsLikeMin())) {
                    return newTemperatureType(weatherDay, Day::feelslikemin);
                }
                if (channelId.equals(day.feelsLikeMax())) {
                    return newTemperatureType(weatherDay, Day::feelslikemax);
                }
                if (channelId.equals(day.dew())) {
                    return newTemperatureType(weatherDay, Day::dew);
                }
                if (channelId.equals(day.humidity())) {
                    return newHumidityType(weatherDay, Day::humidity);
                }
                if (channelId.equals(day.precip())) {
                    return newMilliLengthType(weatherDay, Day::precip);
                }
                if (channelId.equals(day.precipProb())) {
                    return newPercentType(weatherDay, Day::precipprob);
                }
                if (channelId.equals(day.precipType())) {
                    return newStringCollectionType(weatherDay, Day::preciptype);
                }
                if (channelId.equals(day.precipCover())) {
                    return newPercentType(weatherDay, Day::precipcover);
                }
                if (channelId.equals(day.snow())) {
                    return newCentiLengthType(weatherDay, Day::snow);
                }
                if (channelId.equals(day.snowDepth())) {
                    return newCentiLengthType(weatherDay, Day::snowdepth);
                }
                if (channelId.equals(day.windGust())) {
                    return newSpeedType(weatherDay, Day::windgust);
                }
                if (channelId.equals(day.windSpeed())) {
                    return newSpeedType(weatherDay, Day::windspeed);
                }
                if (channelId.equals(day.windDir())) {
                    return newAngleType(weatherDay, Day::winddir);
                }
                if (channelId.equals(day.pressure())) {
                    return newMilliPressureType(weatherDay, Day::pressure);
                }
                if (channelId.equals(day.cloudCover())) {
                    return newPercentType(weatherDay, Day::cloudcover);
                }
                if (channelId.equals(day.visibility())) {
                    return newKiloMeterType(weatherDay, Day::visibility);
                }
                if (channelId.equals(day.solarRadiation())) {
                    return newSolarRadiationType(weatherDay, Day::solarradiation);
                }
                if (channelId.equals(day.solarEnergy())) {
                    return newSolarEnergyType(weatherDay, Day::solarenergy);
                }
                if (channelId.equals(day.uvIndex())) {
                    return newDecimalType(weatherDay, Day::uvindex);
                }
                if (channelId.equals(day.sunrise())) {
                    return newStringType(weatherDay, Day::sunrise);
                }
                if (channelId.equals(day.sunriseEpoch())) {
                    return newDateTimeType(weatherDay, Day::sunriseEpoch);
                }
                if (channelId.equals(day.sunset())) {
                    return newStringType(weatherDay, Day::sunset);
                }
                if (channelId.equals(day.sunsetEpoch())) {
                    return newDateTimeType(weatherDay, Day::sunsetEpoch);
                }
                if (channelId.equals(day.moonPhase())) {
                    return newDecimalType(weatherDay, Day::moonphase);
                }
                if (channelId.equals(day.conditions())) {
                    return newStringType(weatherDay, Day::conditions);
                }
                if (channelId.equals(day.description())) {
                    return newStringType(weatherDay, Day::description);
                }
                if (channelId.equals(day.icon())) {
                    return newStringType(weatherDay, Day::icon);
                }
                if (channelId.equals(day.stations())) {
                    return newStringCollectionType(weatherDay, Day::stations);
                }
                if (channelId.equals(day.source())) {
                    return newStringType(weatherDay, Day::source);
                }
                if (channelId.equals(day.severeRisk())) {
                    return newDecimalType(weatherDay, Day::severerisk);
                }

                // hours
                var hours = requireNonNull(ofNullable(weatherDay).map(Day::hours).orElse(List.of()));
                for (var hourIdx = 0; hourIdx < ChannelDay.NR_OF_HOURS; hourIdx++) {
                    var dayHour = day.hour(hourIdx);

                    if (dayHour.hourDateTime().equals(channelId)) {
                        return newStringType(findHour(hours, hourIdx), Hour::datetime);
                    }
                    if (dayHour.hourTimeStamp().equals(channelId)) {
                        return newDateTimeType(findHour(hours, hourIdx), Hour::datetimeEpoch);
                    }
                    if (dayHour.hourTemperature().equals(channelId)) {
                        return newTemperatureType(findHour(hours, hourIdx), Hour::temp);
                    }
                    if (dayHour.hourFeelsLike().equals(channelId)) {
                        return newTemperatureType(findHour(hours, hourIdx), Hour::feelslike);
                    }
                    if (dayHour.hourHumidity().equals(channelId)) {
                        return newHumidityType(findHour(hours, hourIdx), Hour::humidity);
                    }
                    if (dayHour.hourDew().equals(channelId)) {
                        return newTemperatureType(findHour(hours, hourIdx), Hour::dew);
                    }
                    if (dayHour.hourPrecip().equals(channelId)) {
                        return newMilliLengthType(findHour(hours, hourIdx), Hour::precip);
                    }
                    if (dayHour.hourPrecipProb().equals(channelId)) {
                        return newPercentType(findHour(hours, hourIdx), Hour::precipprob);
                    }
                    if (dayHour.hourPrecipType().equals(channelId)) {
                        return newStringCollectionType(findHour(hours, hourIdx), Hour::preciptype);
                    }
                    if (dayHour.hourSnow().equals(channelId)) {
                        return newCentiLengthType(findHour(hours, hourIdx), Hour::snow);
                    }
                    if (dayHour.hourSnowDepth().equals(channelId)) {
                        return newCentiLengthType(findHour(hours, hourIdx), Hour::snowdepth);
                    }
                    if (dayHour.hourWindGust().equals(channelId)) {
                        return newSpeedType(findHour(hours, hourIdx), Hour::windgust);
                    }
                    if (dayHour.hourWindSpeed().equals(channelId)) {
                        return newSpeedType(findHour(hours, hourIdx), Hour::windspeed);
                    }
                    if (dayHour.hourWindDir().equals(channelId)) {
                        return newAngleType(findHour(hours, hourIdx), Hour::winddir);
                    }
                    if (dayHour.hourPressure().equals(channelId)) {
                        return newMilliPressureType(findHour(hours, hourIdx), Hour::pressure);
                    }
                    if (dayHour.hourVisibility().equals(channelId)) {
                        return newKiloMeterType(findHour(hours, hourIdx), Hour::visibility);
                    }
                    if (dayHour.hourCloudCover().equals(channelId)) {
                        return newPercentType(findHour(hours, hourIdx), Hour::cloudcover);
                    }
                    if (dayHour.hourSolarRadiation().equals(channelId)) {
                        return newSolarRadiationType(findHour(hours, hourIdx), Hour::solarradiation);
                    }
                    if (dayHour.hourSolarEnergy().equals(channelId)) {
                        return newSolarEnergyType(findHour(hours, hourIdx), Hour::solarenergy);
                    }
                    if (dayHour.hourUvIndex().equals(channelId)) {
                        return newDecimalType(findHour(hours, hourIdx), Hour::uvindex);
                    }
                    if (dayHour.hourSevereRisk().equals(channelId)) {
                        return newDecimalType(findHour(hours, hourIdx), Hour::severerisk);
                    }
                    if (dayHour.hourConditions().equals(channelId)) {
                        return newStringType(findHour(hours, hourIdx), Hour::conditions);
                    }
                    if (dayHour.hourIcon().equals(channelId)) {
                        return newStringType(findHour(hours, hourIdx), Hour::icon);
                    }
                    if (dayHour.hourStations().equals(channelId)) {
                        return newStringCollectionType(findHour(hours, hourIdx), Hour::stations);
                    }
                    if (dayHour.hourSource().equals(channelId)) {
                        return newStringType(findHour(hours, hourIdx), Hour::source);
                    }
                }
            }
        }

        logger.warn("Do not know this channel ID: {}", channelId);
        return UNDEF;
    }

    @Nullable
    private Hour findHour(List<Hour> hours, int hourIdx) {
        if (hourIdx >= hours.size()) {
            return null;
        }
        return hours.get(hourIdx);
    }

    @Override
    public void initialize() {
        updateStatus(OFFLINE, CONFIGURATION_PENDING, "@text/channel-type.visualcrossing.weather.config-pending");
        weatherResponse.set(null);

        var config = getConfigAs(VisualCrossingConfiguration.class);
        var hostname = config.hostname;
        try {
            new URL(hostname);
        } catch (MalformedURLException e) {
            logger.debug("Hostname [{}] is not an URL!", hostname, e);
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "@text/addon.visualcrossing.weather.error.hostname [\"%s\"]".formatted(hostname));
            return;
        }

        var apiKey = config.apiKey;
        if (apiKey == null) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@text/addon.visualcrossing.weather.error.missing-api-key");
            return;
        }

        var restClient = (RestClient) new ApiClient(httpClientFactory.getCommonHttpClient());
        if (config.httpRetries > 0) {
            restClient = new RetryHttpClient(restClient, config.httpRetries);
        }

        if (config.location != null && !config.location.isBlank()) {
            location = config.location;
        } else {
            var pointType = locationProvider.getLocation();
            if (pointType != null) {
                var latitude = pointType.getLatitude();
                var longitude = pointType.getLongitude();
                location = "%s,%s".formatted(latitude, longitude);
            }
        }
        if (location == null) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@text/addon.visualcrossing.weather.error.no-location");
            return;
        }

        if (config.lang != null && !config.lang.isBlank()) {
            lang = config.lang;
        } else {
            lang = localeProvider.getLocale().getLanguage().toLowerCase();
        }
        if (!SUPPORTED_LANGUAGES.contains(lang)) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "@text/addon.visualcrossing.weather.error.bad-language [\"%s\"]".formatted(lang));
            return;
        }

        api = new VisualCrossingApi(hostname, apiKey, restClient, new Gson());
        schedule = scheduler.scheduleWithFixedDelay(this::pull, 0, config.refreshInterval, SECONDS);
        // do not set status to online - it is done in `pull` method that is run in schedule
    }

    private void pull() {
        var localApi = api;
        if (localApi == null) {
            logger.debug("Api was null!");
            updateStatus(OFFLINE, CONFIGURATION_PENDING, "@text/channel-type.visualcrossing.weather.config-pending");
            return;
        }
        try {
            var weatherResponse = localApi.timeline(requireNonNull(location), null, lang, null, null);
            this.weatherResponse.set(weatherResponse);
            updateStatus(ONLINE);
            thing.getChannels().forEach(channel -> handleCommand(channel.getUID(), REFRESH));
        } catch (VisualCrossingAuthException e) {
            logger.debug("Auth error while getting timeline for {}", location, e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, "@text/addon.visualcrossing.weather.error.auth");
        } catch (VisualCrossingRateException e) {
            logger.debug("Rate error while getting timeline for {}", location, e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, "@text/addon.visualcrossing.weather.error.rate");
        } catch (VisualCrossingApiException e) {
            logger.debug("Error while getting timeline for {}", location, e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR,
                    "@text/addon.visualcrossing.weather.error.api [\"%s\"]".formatted(e.getLocalizedMessage()));
        } catch (Exception e) {
            logger.debug("Error while getting timeline for {}", location, e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR,
                    "@text/addon.visualcrossing.weather.error.generic [\"%s\"]".formatted(e.getLocalizedMessage()));
        }
    }

    @Override
    public void dispose() {
        {
            var localSchedule = schedule;
            schedule = null;
            if (localSchedule != null) {
                localSchedule.cancel(true);
            }
        }
        api = null;
        weatherResponse.set(null);
        location = null;
    }

    @Nullable
    public WeatherResponse timeline(@Nullable String location, @Nullable UnitGroup unitGroup, @Nullable String lang,
            @Nullable String dateFrom, @Nullable String dateTo)
            throws VisualCrossingAuthException, VisualCrossingApiException, VisualCrossingRateException {
        var localApi = api;
        if (localApi == null) {
            logger.debug("Api was null!");
            return null;
        }
        if (location == null) {
            location = this.location;
        }
        return localApi.timeline(requireNonNull(location), unitGroup, lang, dateFrom, dateTo);
    }
}
