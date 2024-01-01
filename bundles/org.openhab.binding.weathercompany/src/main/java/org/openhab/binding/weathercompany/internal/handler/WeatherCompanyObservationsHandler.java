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
package org.openhab.binding.weathercompany.internal.handler;

import static org.openhab.binding.weathercompany.internal.WeatherCompanyBindingConstants.*;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.weathercompany.internal.config.WeatherCompanyObservationsConfig;
import org.openhab.binding.weathercompany.internal.model.PwsObservationsDTO;
import org.openhab.binding.weathercompany.internal.model.PwsObservationsDTO.Observations;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link WeatherCompanyObservationsHandler} is responsible for pulling Personal
 * Weather Station (PWS) observations from the Weather Company API.
 *
 * API documentation is located here
 * - https://docs.google.com/document/d/1eKCnKXI9xnoMGRRzOL1xPCBihNV2rOet08qpE_gArAY/edit
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class WeatherCompanyObservationsHandler extends WeatherCompanyAbstractHandler {
    private static final String BASE_PWS_URL = "https://api.weather.com/v2/pws/observations/current";

    private final Logger logger = LoggerFactory.getLogger(WeatherCompanyObservationsHandler.class);

    private int refreshIntervalSeconds;

    private @Nullable Future<?> refreshObservationsJob;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshPwsObservations();
        }
    };

    public WeatherCompanyObservationsHandler(Thing thing, TimeZoneProvider timeZoneProvider, HttpClient httpClient,
            UnitProvider unitProvider, LocaleProvider localeProvider) {
        super(thing, timeZoneProvider, httpClient, unitProvider);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing observations handler with configuration: {}",
                getConfigAs(WeatherCompanyObservationsConfig.class).toString());

        refreshIntervalSeconds = getConfigAs(WeatherCompanyObservationsConfig.class).refreshInterval * 60;
        weatherDataCache.clear();
        scheduleRefreshJob();
        updateStatus(isBridgeOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.equals(RefreshType.REFRESH)) {
            State state = weatherDataCache.get(channelUID.getId());
            if (state != null) {
                updateChannel(channelUID.getId(), state);
            }
        }
    }

    /*
     * Build the URL for requesting the PWS current observations
     */
    private @Nullable String buildPwsUrl() {
        WeatherCompanyObservationsConfig config = getConfigAs(WeatherCompanyObservationsConfig.class);
        String pwsStationId = config.pwsStationId;
        if (pwsStationId == null || pwsStationId.isEmpty()) {
            return null;
        }
        String apiKey = getApiKey();
        StringBuilder sb = new StringBuilder(BASE_PWS_URL);
        // Set to use Imperial units. UoM will convert to the other units
        sb.append("?units=e");
        // Get temperatures with one decimal point precision
        sb.append("&numericPrecision=decimal");
        // Set response type as JSON
        sb.append("&format=json");
        // Set PWS station Id from config
        sb.append("&stationId=").append(pwsStationId);
        // Set API key from config
        sb.append("&apiKey=").append(apiKey);
        String url = sb.toString();
        logger.debug("PWS observations URL is {}", url.replace(apiKey, REPLACE_API_KEY));
        return url;
    }

    private synchronized void refreshPwsObservations() {
        if (!isBridgeOnline()) {
            // If bridge is not online, API has not been validated yet
            logger.debug("Handler: Can't refresh PWS observations because bridge is not online");
            return;
        }
        logger.debug("Handler: Requesting PWS observations from The Weather Company API");
        String response = executeApiRequest(buildPwsUrl());
        if (response == null) {
            return;
        }
        try {
            logger.debug("Handler: Parsing PWS observations response: {}", response);
            PwsObservationsDTO pwsObservations = Objects
                    .requireNonNull(gson.fromJson(response, PwsObservationsDTO.class));
            logger.debug("Handler: Successfully parsed PWS observations response object");
            updateStatus(ThingStatus.ONLINE);
            updatePwsObservations(pwsObservations);
        } catch (JsonSyntaxException e) {
            logger.debug("Handler: Error parsing pws observations response object: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-parsing-pws-forecast");
            return;
        }
    }

    private void updatePwsObservations(PwsObservationsDTO pwsObservations) {
        if (pwsObservations.observations.length == 0) {
            logger.debug("Handler: PWS observation object contains no observations!");
            return;
        }
        Observations obs = pwsObservations.observations[0];
        logger.debug("Handler: Processing observations from station {} at {}", obs.stationID, obs.obsTimeLocal);
        updateChannel(CH_PWS_TEMP, undefOrQuantity(obs.imperial.temp, ImperialUnits.FAHRENHEIT));
        updateChannel(CH_PWS_TEMP_HEAT_INDEX, undefOrQuantity(obs.imperial.heatIndex, ImperialUnits.FAHRENHEIT));
        updateChannel(CH_PWS_TEMP_WIND_CHILL, undefOrQuantity(obs.imperial.windChill, ImperialUnits.FAHRENHEIT));
        updateChannel(CH_PWS_TEMP_DEW_POINT, undefOrQuantity(obs.imperial.dewpt, ImperialUnits.FAHRENHEIT));
        updateChannel(CH_PWS_HUMIDITY, undefOrQuantity(obs.humidity, Units.PERCENT));
        updateChannel(CH_PWS_PRESSURE, undefOrQuantity(obs.imperial.pressure, ImperialUnits.INCH_OF_MERCURY));
        updateChannel(CH_PWS_PRECIPTATION_RATE, undefOrQuantity(obs.imperial.precipRate, Units.INCHES_PER_HOUR));
        updateChannel(CH_PWS_PRECIPITATION_TOTAL, undefOrQuantity(obs.imperial.precipTotal, ImperialUnits.INCH));
        updateChannel(CH_PWS_WIND_SPEED, undefOrQuantity(obs.imperial.windSpeed, ImperialUnits.MILES_PER_HOUR));
        updateChannel(CH_PWS_WIND_GUST, undefOrQuantity(obs.imperial.windGust, ImperialUnits.MILES_PER_HOUR));
        updateChannel(CH_PWS_WIND_DIRECTION, undefOrQuantity(obs.winddir, Units.DEGREE_ANGLE));
        updateChannel(CH_PWS_SOLAR_RADIATION, undefOrQuantity(obs.solarRadiation, Units.IRRADIANCE));
        updateChannel(CH_PWS_UV, undefOrDecimal(obs.uv));
        updateChannel(CH_PWS_OBSERVATION_TIME_LOCAL, undefOrDate(obs.obsTimeUtc));
        updateChannel(CH_PWS_NEIGHBORHOOD, undefOrString(obs.neighborhood));
        updateChannel(CH_PWS_STATION_ID, undefOrString(obs.stationID));
        updateChannel(CH_PWS_COUNTRY, undefOrString(obs.country));
        updateChannel(CH_PWS_LOCATION, undefOrPoint(obs.lat, obs.lon));
        updateChannel(CH_PWS_ELEVATION, undefOrQuantity(obs.imperial.elev, ImperialUnits.FOOT));
        updateChannel(CH_PWS_QC_STATUS, undefOrDecimal(obs.qcStatus));
        updateChannel(CH_PWS_SOFTWARE_TYPE, undefOrString(obs.softwareType));
    }

    /*
     * The refresh job updates the PWS current observations
     * on the refresh interval set in the thing config
     */
    private void scheduleRefreshJob() {
        logger.debug("Handler: Scheduling observations refresh job in {} seconds", REFRESH_JOB_INITIAL_DELAY_SECONDS);
        cancelRefreshJob();
        refreshObservationsJob = scheduler.scheduleWithFixedDelay(refreshRunnable, REFRESH_JOB_INITIAL_DELAY_SECONDS,
                refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    private void cancelRefreshJob() {
        if (refreshObservationsJob != null) {
            refreshObservationsJob.cancel(true);
            logger.debug("Handler: Canceling observations refresh job");
        }
    }
}
