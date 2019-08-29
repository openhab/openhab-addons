/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.MILLI;
import static org.openhab.binding.weathercompany.internal.WeatherCompanyBindingConstants.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;
import javax.measure.Unit;
import javax.measure.spi.SystemOfUnits;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.weathercompany.internal.config.WeatherCompanyConfig;
import org.openhab.binding.weathercompany.internal.model.DayPart;
import org.openhab.binding.weathercompany.internal.model.Forecast;
import org.openhab.binding.weathercompany.internal.model.PwsObservations;
import org.openhab.binding.weathercompany.internal.model.PwsObservations.Observations;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WeatherCompanyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * API documentation is located here
 * - https://docs.google.com/document/d/1eKCnKXI9xnoMGRRzOL1xPCBihNV2rOet08qpE_gArAY/edit
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class WeatherCompanyHandler extends BaseThingHandler {
    // Five-day weather forecast URL
    private static final String BASE_FORECAST_URL = "https://api.weather.com/v3/wx/forecast/daily/5day";

    // Personal Weather Station observations URL
    private static final String BASE_PWS_URL = "https://api.weather.com/v2/pws/observations/current";

    private static final int WEATHER_COMPANY_API_TIMEOUT_SECONDS = 10;
    private static final int REFRESH_JOB_INITIAL_DELAY_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(WeatherCompanyHandler.class);

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private final Map<String, State> weatherDataCache = Collections.synchronizedMap(new HashMap<>());

    // Provided by handler factory
    private TimeZoneProvider timeZoneProvider;
    private HttpClient httpClient;
    private SystemOfUnits systemOfUnits;

    // Thing configuration
    private @Nullable String apiKey;
    private @Nullable String locationType;
    private @Nullable String postalCode;
    private @Nullable String geocode;
    private @Nullable String iataCode;
    private @Nullable String language;
    private @Nullable String pwsStationId;
    private int refreshIntervalSeconds;

    private @Nullable String forecastUrl;
    private @Nullable String pwsUrl;

    // Job to update the forecast and PWS observations
    private @Nullable Future<?> refreshJob;

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshForecast();
            refreshPwsObservations();
        }
    };

    public WeatherCompanyHandler(Thing thing, TimeZoneProvider timeZoneProvider, HttpClient httpClient,
            UnitProvider unitProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.httpClient = httpClient;
        this.systemOfUnits = unitProvider.getMeasurementSystem();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);

        // Get the configuration
        WeatherCompanyConfig config = getConfigAs(WeatherCompanyConfig.class);
        logger.debug("Configuration: {}", config.toString());
        apiKey = config.apiKey;
        locationType = config.locationType;
        postalCode = config.postalCode;
        geocode = config.geocode;
        iataCode = config.iataCode;
        language = config.language;
        pwsStationId = config.pwsStationId;
        refreshIntervalSeconds = config.refreshInterval * 60;

        // Construct the URL for querying the forecast
        forecastUrl = buildForecastUrl();

        // Construct the URL for querying the PWS observations
        pwsUrl = buildPwsUrl();

        weatherDataCache.clear();

        // Schedule the job to refresh the forecast
        scheduleRefreshJob();
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
     * Build the URL for requesting the 5-day forecast. It's important to request
     * the desired language AND units so that the forecast narrative contains
     * the consistent language and units (e.g. wind gusts to 30 mph).
     */
    private String buildForecastUrl() {
        StringBuilder sb = new StringBuilder(BASE_FORECAST_URL);
        // Set response type as JSON
        sb.append("?format=json");
        // Set language from config
        sb.append("&language=").append(language);
        // Set API key from config
        sb.append("&apiKey=").append(apiKey);
        // Set the units to Imperial or Metric
        sb.append("&units=").append(getUnitsQueryString());
        // Set the location from config
        sb.append(getLocationQueryString());
        String url = sb.toString();
        logger.debug("Forecast URL is {}", url.replace(apiKey, REPLACE_API_KEY));
        return url.toString();
    }

    private String getLocationQueryString() {
        boolean validConfig = true;
        StringBuilder sb = new StringBuilder();
        String location;
        switch (locationType) {
            case CONFIG_LOCATION_TYPE_POSTAL_CODE:
                location = StringUtils.trimToNull(postalCode);
                if (location == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Postal code is not set");
                    validConfig = false;
                } else {
                    sb.append("&postalKey=").append(location.replace(" ", ""));
                }
                break;
            case CONFIG_LOCATION_TYPE_GEOCODE:
                location = StringUtils.trimToNull(geocode);
                if (location == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Geocode is not set");
                    validConfig = false;
                } else {
                    sb.append("&geocode=").append(location.replace(" ", ""));
                }
                break;
            case CONFIG_LOCATION_TYPE_IATA_CODE:
                location = StringUtils.trimToNull(iataCode);
                if (location == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "IATA code is not set");
                    validConfig = false;
                } else {
                    sb.append("&iataCode=").append(location.replace(" ", "").toUpperCase());
                }
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Location Type is not set");
                validConfig = false;
        }
        if (validConfig) {
            updateStatus(ThingStatus.ONLINE);
        }
        return sb.toString();
    }

    /*
     * Set either Imperial or Metric SI for the API call
     */
    private String getUnitsQueryString() {
        return isImperial() ? "e" : "s";
    }

    /*
     * Build the URL for requesting the PWS current observations
     */
    private @Nullable String buildPwsUrl() {
        if (StringUtils.isEmpty(pwsStationId)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(BASE_PWS_URL);
        // Set to use Imperial units. UoM will convert to the other units
        sb.append("?units=e");
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

    /*
     * Determine the units configured in the system
     */
    private boolean isImperial() {
        return systemOfUnits instanceof ImperialUnits ? true : false;
    }

    private synchronized void refreshForecast() {
        if (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
            logger.debug("Handler: Can't refresh forecast because thing configuration is incomplete");
            return;
        }
        logger.debug("Handler: Requesting forecast from The Weather Company API");
        String response = executeApiRequest(forecastUrl);
        if (response == null) {
            return;
        }
        try {
            logger.trace("Handler: Parsing forecast response: {}", response);
            Forecast forecast = gson.fromJson(response, Forecast.class);
            logger.debug("Handler: Successfully parsed daily forecast response object");
            updateStatus(ThingStatus.ONLINE);
            updateDailyForecast(forecast);
            updateDaypartForecast(forecast.daypart);
        } catch (JsonSyntaxException e) {
            logger.debug("Handler: Error parsing daily forecast response object", e);
            markThingOffline("Error parsing daily forecast");
            return;
        }
    }

    private void updateDailyForecast(Forecast forecast) {
        for (int day = 0; day < forecast.dayOfWeek.length; day++) {
            logger.debug("Processing daily forecast for '{}'", forecast.dayOfWeek[day]);
            updateDaily(day, CH_DAY_OF_WEEK, undefOrString(forecast.dayOfWeek[day]));
            updateDaily(day, CH_NARRATIVE, undefOrString(forecast.narrative[day]));
            updateDaily(day, CH_VALID_TIME_LOCAL, undefOrDate(forecast.validTimeUtc[day]));
            updateDaily(day, CH_EXPIRATION_TIME_LOCAL, undefOrDate(forecast.expirationTimeUtc[day]));
            updateDaily(day, CH_TEMP_MAX, undefOrQuantity(forecast.temperatureMax[day], getTempUnit()));
            updateDaily(day, CH_TEMP_MIN, undefOrQuantity(forecast.temperatureMin[day], getTempUnit()));
            updateDaily(day, CH_PRECIP_RAIN, undefOrQuantity(forecast.qpf[day], getLengthUnit()));
            updateDaily(day, CH_PRECIP_SNOW, undefOrQuantity(forecast.qpfSnow[day], getLengthUnit()));
        }
    }

    private void updateDaypartForecast(Object daypartObject) {
        DayPart[] dayparts;
        try {
            String innerJson = gson.toJson(daypartObject);
            logger.debug("Parsing daypartsObject: {}", innerJson);
            dayparts = gson.fromJson(innerJson.toString(), DayPart[].class);
            logger.debug("Handler: Successfully parsed daypart forecast object");
        } catch (JsonSyntaxException e) {
            logger.debug("Handler: Error parsing daypart forecast object: {}", e.getMessage(), e);
            markThingOffline("Error parsing daypart forecast");
            return;
        }
        logger.debug("There are {} daypart forecast entries", dayparts.length);
        if (dayparts.length == 0) {
            logger.debug("There is no daypart forecast object in this message");
            return;
        }
        logger.debug("There are {} daypartName entries in this forecast", dayparts[0].daypartName.length);
        for (int i = 0; i < dayparts[0].daypartName.length; i++) {
            // Note: All dayparts[0] (i.e. today day) values are null after 3 pm local time
            DayPart dp = dayparts[0];
            // Even daypart indexes are Day (D); odd daypart indexes are Night (N)
            String dOrN = dp.dayOrNight[i] == null ? (i % 2 == 0 ? "D" : "N") : dp.dayOrNight[i];
            logger.debug("Processing daypart forecast for '{}'", dp.daypartName[i]);
            updateDaypart(i, dOrN, CH_DP_NAME, undefOrString(dp.daypartName[i]));
            updateDaypart(i, dOrN, CH_DP_DAY_OR_NIGHT, undefOrString(dayparts[0].dayOrNight[i]));
            updateDaypart(i, dOrN, CH_DP_NARRATIVE, undefOrString(dayparts[0].narrative[i]));
            updateDaypart(i, dOrN, CH_DP_WX_PHRASE_SHORT, undefOrString(dayparts[0].wxPhraseShort[i]));
            updateDaypart(i, dOrN, CH_DP_WX_PHRASE_LONG, undefOrString(dayparts[0].wxPhraseLong[i]));
            updateDaypart(i, dOrN, CH_DP_QUALIFIER_PHRASE, undefOrString(dayparts[0].qualifierPhrase[i]));
            updateDaypart(i, dOrN, CH_DP_QUALIFIER_CODE, undefOrString(dayparts[0].qualifierCode[i]));
            updateDaypart(i, dOrN, CH_DP_TEMP, undefOrQuantity(dp.temperature[i], getTempUnit()));
            updateDaypart(i, dOrN, CH_DP_TEMP_HEAT_INDEX, undefOrQuantity(dp.temperatureHeatIndex[i], getTempUnit()));
            updateDaypart(i, dOrN, CH_DP_TEMP_WIND_CHILL, undefOrQuantity(dp.temperatureWindChill[i], getTempUnit()));
            updateDaypart(i, dOrN, CH_DP_HUMIDITY, undefOrQuantity(dp.relativeHumidity[i], SmartHomeUnits.PERCENT));
            updateDaypart(i, dOrN, CH_DP_CLOUD_COVER, undefOrQuantity(dp.cloudCover[i], SmartHomeUnits.PERCENT));
            updateDaypart(i, dOrN, CH_DP_PRECIP_CHANCE, undefOrQuantity(dp.precipChance[i], SmartHomeUnits.PERCENT));
            updateDaypart(i, dOrN, CH_DP_PRECIP_TYPE, undefOrString(dp.precipType[i]));
            updateDaypart(i, dOrN, CH_DP_PRECIP_RAIN, undefOrQuantity(dp.qpf[i], getLengthUnit()));
            updateDaypart(i, dOrN, CH_DP_PRECIP_SNOW, undefOrQuantity(dp.qpfSnow[i], getLengthUnit()));
            updateDaypart(i, dOrN, CH_DP_SNOW_RANGE, undefOrString(dp.snowRange[i]));
            updateDaypart(i, dOrN, CH_DP_WIND_SPEED, undefOrQuantity(dp.windSpeed[i], getSpeedUnit()));
            updateDaypart(i, dOrN, CH_DP_WIND_DIR_CARDINAL, undefOrString(dp.windDirectionCardinal[i]));
            updateDaypart(i, dOrN, CH_DP_WIND_PHRASE, undefOrString(dp.windPhrase[i]));
            updateDaypart(i, dOrN, CH_DP_WIND_DIR, undefOrQuantity(dp.windDirection[i], SmartHomeUnits.DEGREE_ANGLE));
            updateDaypart(i, dOrN, CH_DP_THUNDER_CATEGORY, undefOrString(dp.thunderCategory[i]));
            updateDaypart(i, dOrN, CH_DP_THUNDER_INDEX, undefOrDecimal(dp.thunderIndex[i]));
            updateDaypart(i, dOrN, CH_DP_UV_DESCRIPTION, undefOrString(dp.uvDescription[i]));
            updateDaypart(i, dOrN, CH_DP_UV_INDEX, undefOrDecimal(dp.uvIndex[i]));
            updateDaypart(i, dOrN, CH_DP_ICON_CODE, undefOrDecimal(dp.iconCode[i]));
            updateDaypart(i, dOrN, CH_DP_ICON_CODE_EXTEND, undefOrDecimal(dp.iconCodeExtend[i]));
            updateDaypart(i, dOrN, CH_DP_ICON_IMAGE, getIconImage(dp.iconCode[i]));
        }
    }

    private State getIconImage(Integer iconCode) {
        // First try to get the image associated with the icon code
        byte[] image = getImage("icons" + File.separator + String.format("%02d", iconCode) + ".png");
        if (image != null) {
            return new RawType(image, "image/png");
        }
        // Next try to get the N/A image
        image = getImage("icons" + File.separator + "na.png");
        if (image != null) {
            return new RawType(image, "image/png");
        }
        // Couldn't get any icon image, so set to UNDEF
        return UnDefType.UNDEF;
    }

    private byte @Nullable [] getImage(String iconPath) {
        byte[] data = null;
        URL url = FrameworkUtil.getBundle(getClass()).getResource(iconPath);
        logger.trace("Path to icon image resource is: {}", url);
        if (url != null) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                InputStream is = url.openStream();
                BufferedImage image = ImageIO.read(is);
                ImageIO.write(image, "png", out);
                out.flush();
                data = out.toByteArray();
            } catch (IOException e) {
                logger.debug("I/O exception occurred getting image data: {}", e.getMessage(), e);
            }
        }
        return data;
    }

    private synchronized void refreshPwsObservations() {
        if (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
            logger.debug("Handler: Can't refresh PWS observations because thing configuration is incomplete");
            return;
        }
        // Station id is optional, so check to see if it's set
        if (pwsUrl == null) {
            return;
        }
        logger.debug("Handler: Requesting PWS observations from The Weather Company API");
        String response = executeApiRequest(pwsUrl);
        if (response == null) {
            return;
        }
        try {
            logger.debug("Handler: Parsing PWS observations response: {}", response);
            PwsObservations pwsObservations = gson.fromJson(response, PwsObservations.class);
            logger.debug("Handler: Successfully parsed PWS observations response object");
            updatePwsObservations(pwsObservations);
        } catch (JsonSyntaxException e) {
            logger.debug("Handler: Error parsing pws observations response object: {}", e.getMessage(), e);
            markThingOffline("Error parsing PWS observations");
            return;
        }
    }

    private void updatePwsObservations(PwsObservations pwsObservations) {
        if (pwsObservations.observations.length == 0) {
            logger.debug("Handler: PWS observation object contains no observations!");
            return;
        }
        Observations obs = pwsObservations.observations[0];
        logger.debug("Handler: Processing observations from station {} at {}", obs.stationID, obs.obsTimeLocal);
        updatePws(CH_PWS_TEMP, undefOrQuantity(obs.imperial.temp, ImperialUnits.FAHRENHEIT));
        updatePws(CH_PWS_TEMP_HEAT_INDEX, undefOrQuantity(obs.imperial.heatIndex, ImperialUnits.FAHRENHEIT));
        updatePws(CH_PWS_TEMP_WIND_CHILL, undefOrQuantity(obs.imperial.windChill, ImperialUnits.FAHRENHEIT));
        updatePws(CH_PWS_TEMP_DEW_POINT, undefOrQuantity(obs.imperial.dewpt, ImperialUnits.FAHRENHEIT));
        updatePws(CH_PWS_HUMIDITY, undefOrQuantity(obs.humidity, SmartHomeUnits.PERCENT));
        updatePws(CH_PWS_PRESSURE, undefOrQuantity(obs.imperial.pressure, ImperialUnits.INCH_OF_MERCURY));
        updatePws(CH_PWS_PRECIPTATION_RATE, undefOrQuantity(obs.imperial.precipRate, SmartHomeUnits.INCHES_PER_HOUR));
        updatePws(CH_PWS_PRECIPITATION_TOTAL, undefOrQuantity(obs.imperial.precipTotal, ImperialUnits.INCH));
        updatePws(CH_PWS_WIND_SPEED, undefOrQuantity(obs.imperial.windSpeed, ImperialUnits.MILES_PER_HOUR));
        updatePws(CH_PWS_WIND_GUST, undefOrQuantity(obs.imperial.windGust, ImperialUnits.MILES_PER_HOUR));
        updatePws(CH_PWS_WIND_DIRECTION, undefOrQuantity(obs.winddir, SmartHomeUnits.DEGREE_ANGLE));
        updatePws(CH_PWS_SOLAR_RADIATION, undefOrQuantity(obs.solarRadiation, SmartHomeUnits.IRRADIANCE));
        updatePws(CH_PWS_UV, undefOrDecimal(obs.uv));
        updatePws(CH_PWS_OBSERVATION_TIME_LOCAL, undefOrDate(obs.obsTimeUtc));
        updatePws(CH_PWS_NEIGHBORHOOD, undefOrString(obs.neighborhood));
        updatePws(CH_PWS_STATION_ID, undefOrString(obs.stationID));
        updatePws(CH_PWS_COUNTRY, undefOrString(obs.country));
        updatePws(CH_PWS_LATITUDE, undefOrDecimal(obs.lat));
        updatePws(CH_PWS_LONGITUDE, undefOrDecimal(obs.lon));
        updatePws(CH_PWS_ELEVATION, undefOrQuantity(obs.imperial.precipTotal, ImperialUnits.FOOT));
        updatePws(CH_PWS_QC_STATUS, undefOrDecimal(obs.qcStatus));
        updatePws(CH_PWS_SOFTWARE_TYPE, undefOrString(obs.softwareType));
    }

    private void updateDaily(int day, String channelId, State state) {
        updateChannel(CH_GROUP_FORECAST_DAY + String.valueOf(day) + "#" + channelId, state);
    }

    private void updateDaypart(int daypartIndex, String dayOrNight, String channelId, State state) {
        int day = daypartIndex / 2;
        String dON = dayOrNight.equals("D") ? CH_GROUP_FORECAST_DAYPART_DAY : CH_GROUP_FORECAST_DAYPART_NIGHT;
        updateChannel(CH_GROUP_FORECAST_DAY + String.valueOf(day) + dON + "#" + channelId, state);
    }

    private void updatePws(String channelId, State state) {
        updateChannel(CH_GROUP_PWS_OBSERVATIONS + "#" + channelId, state);
    }

    private void updateChannel(String channelId, State state) {
        // Only update channel if it's linked
        if (isLinked(channelId)) {
            updateState(channelId, state);
            weatherDataCache.put(channelId, state);
        }
    }

    /*
     * Set the state to the passed value. If value is null, set the state to UNDEF
     */
    private State undefOrString(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : new StringType(value);
    }

    private State undefOrDate(@Nullable Integer value) {
        return value == null ? UnDefType.UNDEF : getLocalDateTimeType(value);
    }

    private State undefOrDate(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : getLocalDateTimeType(value);
    }

    private State undefOrDecimal(@Nullable Number value) {
        return value == null ? UnDefType.UNDEF : new DecimalType(value.doubleValue());
    }

    private State undefOrQuantity(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    /*
     * The API will request units based on openHAB's SystemOfUnits setting. Therefore,
     * when setting the QuantityType state, make sure we use the proper unit.
     */
    private Unit<?> getTempUnit() {
        return isImperial() ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;
    }

    private Unit<?> getSpeedUnit() {
        return isImperial() ? ImperialUnits.MILES_PER_HOUR : SIUnits.KILOMETRE_PER_HOUR;
    }

    private Unit<?> getLengthUnit() {
        return isImperial() ? ImperialUnits.INCH : MILLI(SIUnits.METRE);
    }

    /*
     * Execute the The Weather Channel API request
     */
    private @Nullable String executeApiRequest(@Nullable String url) {
        if (url == null) {
            logger.debug("Handler: Can't execute request because url is null");
            return null;
        }
        Request request = httpClient.newRequest(url);
        request.timeout(WEATHER_COMPANY_API_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        request.method(HttpMethod.GET);
        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip");

        String errorMsg;
        try {
            ContentResponse contentResponse = request.send();
            switch (contentResponse.getStatus()) {
                case HttpStatus.OK_200:
                    String response = contentResponse.getContentAsString();
                    String cacheControl = contentResponse.getHeaders().get(HttpHeader.CACHE_CONTROL);
                    logger.debug("Cache-Control header is {}", cacheControl);
                    return response;
                case HttpStatus.NO_CONTENT_204:
                    errorMsg = "HTTP response 400: No content. Check configuration";
                    break;
                case HttpStatus.BAD_REQUEST_400:
                    errorMsg = "HTTP response 400: Bad request";
                    break;
                case HttpStatus.UNAUTHORIZED_401:
                    errorMsg = "HTTP response 401: Unauthorized";
                    break;
                case HttpStatus.FORBIDDEN_403:
                    errorMsg = "HTTP response 403: Invalid API key";
                    break;
                case HttpStatus.NOT_FOUND_404:
                    errorMsg = "HTTP response 404: Endpoint not found";
                    break;
                case HttpStatus.METHOD_NOT_ALLOWED_405:
                    errorMsg = "HTTP response 405: Method not allowed";
                    break;
                case HttpStatus.NOT_ACCEPTABLE_406:
                    errorMsg = "HTTP response 406: Not acceptable";
                    break;
                case HttpStatus.REQUEST_TIMEOUT_408:
                    errorMsg = "HTTP response 408: Request timeout";
                    break;
                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    errorMsg = "HTTP response 500: Internal server error";
                    break;
                case HttpStatus.BAD_GATEWAY_502:
                case HttpStatus.SERVICE_UNAVAILABLE_503:
                case HttpStatus.GATEWAY_TIMEOUT_504:
                    errorMsg = String.format("HTTP response %d: Service unavailable or gateway issue",
                            contentResponse.getStatus());
                    break;
                default:
                    errorMsg = String.format("HTTP GET failed: %d, %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    break;
            }
        } catch (TimeoutException e) {
            errorMsg = "TimeoutException: Call to Weather Company API timed out";
        } catch (ExecutionException e) {
            errorMsg = String.format("ExecutionException: %s", e.getMessage());
        } catch (InterruptedException e) {
            errorMsg = String.format("InterruptedException: %s", e.getMessage());
        }
        markThingOffline(errorMsg);
        logger.debug(errorMsg);
        return null;
    }

    private void markThingOffline(String statusDescription) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, statusDescription);
    }

    /*
     * Convert UTC Unix epoch seconds to local time
     */
    private DateTimeType getLocalDateTimeType(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        ZonedDateTime localDateTime = instant.atZone(getZoneId());
        DateTimeType dateTimeType = new DateTimeType(localDateTime);
        return dateTimeType;
    }

    /*
     * Convert UTC time string to local time
     * Input string is of form 2018-12-02T10:47:00.000Z
     */
    public DateTimeType getLocalDateTimeType(String dateTimeString) {
        DateTimeType dateTimeType;
        try {
            Instant instant = Instant.parse(dateTimeString);
            ZonedDateTime localDateTime = instant.atZone(getZoneId());
            dateTimeType = new DateTimeType(localDateTime);
        } catch (DateTimeParseException e) {
            logger.debug("Error parsing date/time string: {}", e.getMessage());
            dateTimeType = new DateTimeType();
        } catch (IllegalArgumentException e) {
            logger.debug("Error converting to DateTimeType: {}", e.getMessage());
            dateTimeType = new DateTimeType();
        }
        return dateTimeType;
    }

    private ZoneId getZoneId() {
        return timeZoneProvider.getTimeZone();
    }

    /*
     * The refresh job updates the daily forecast and the PWS current
     * observations on the refresh interval set in the thing config
     */
    private void scheduleRefreshJob() {
        logger.debug("Handler: Scheduling forecast refresh job in {} seconds", REFRESH_JOB_INITIAL_DELAY_SECONDS);
        cancelRefreshJob();
        refreshJob = scheduler.scheduleWithFixedDelay(refreshRunnable, REFRESH_JOB_INITIAL_DELAY_SECONDS,
                refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    private void cancelRefreshJob() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
            logger.debug("Handler: Canceling forecast refresh job");
        }
    }

    /*
     * Called by discovery service to get TWC language based on
     * the locale configured in openHAB
     */
    public static String getWeatherCompanyLanguage(Locale locale) {
        String ohLanguage = locale.getLanguage() + "-" + locale.getCountry();
        for (String language : WEATHER_CHANNEL_LANGUAGES) {
            if (language.equals(ohLanguage)) {
                return language;
            }
        }
        return CONFIG_LANGUAGE_DEFAULT;
    }

  //@formatter:off
    private static final String[] WEATHER_CHANNEL_LANGUAGES = {
        "ar-AE",
        "az-AZ",
        "bg-BG",
        "bn-BD",
        "bn-IN",
        "bs-BA",
        "ca-ES",
        "cs-CZ",
        "da-DK",
        "de-DE",
        "el-GR",
        "en-GB",
        "en-IN",
        "en-US",
        "es-AR",
        "es-ES",
        "es-LA",
        "es-MX",
        "es-UN",
        "es-US",
        "et-EE",
        "fa-IR",
        "fi-FI",
        "fr-CA",
        "fr-FR",
        "gu-IN",
        "he-IL",
        "hi-IN",
        "hr-HR",
        "hu-HU",
        "in-ID",
        "is-IS",
        "it-IT",
        "iw-IL",
        "ja-JP",
        "jv-ID",
        "ka-GE",
        "kk-KZ",
        "kn-IN",
        "ko-KR",
        "lt-LT",
        "lv-LV",
        "mk-MK",
        "mn-MN",
        "ms-MY",
        "nl-NL",
        "no-NO",
        "pl-PL",
        "pt-BR",
        "pt-PT",
        "ro-RO",
        "ru-RU",
        "si-LK",
        "sk-SK",
        "sl-SI",
        "sq-AL",
        "sr-BA",
        "sr-ME",
        "sr-RS",
        "sv-SE",
        "sw-KE",
        "ta-IN",
        "ta-LK",
        "te-IN",
        "tg-TJ",
        "th-TH",
        "tk-TM",
        "tl-PH",
        "tr-TR",
        "uk-UA",
        "ur-PK",
        "uz-UZ",
        "vi-VN",
        "zh-CN",
        "zh-HK",
        "zh-TW"
    };
  //@formatter:on
}
