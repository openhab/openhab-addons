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
package org.openhab.binding.openweathermap.internal.connection;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapAPIConfiguration;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonAirPollutionData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonDailyForecastData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonHourlyForecastData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonUVIndexData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonWeatherData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapOneCallAPIData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapOneCallHistAPIData;
import org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapAPIHandler;
import org.openhab.core.cache.ByteArrayFileCache;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link OpenWeatherMapConnection} is responsible for handling the connections to OpenWeatherMap API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapConnection {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapConnection.class);

    private static final String PROPERTY_MESSAGE = "message";

    private static final String PNG_CONTENT_TYPE = "image/png";

    private static final String PARAM_APPID = "appid";
    private static final String PARAM_UNITS = "units";
    private static final String PARAM_LAT = "lat";
    private static final String PARAM_LON = "lon";
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_FORECAST_CNT = "cnt";
    private static final String PARAM_HISTORY_DATE = "dt";
    private static final String PARAM_EXCLUDE = "exclude";

    // Current weather data (see https://openweathermap.org/current)
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    // 5 day / 3 hour forecast (see https://openweathermap.org/forecast5)
    private static final String THREE_HOUR_FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    // 16 day / daily forecast (see https://openweathermap.org/forecast16)
    private static final String DAILY_FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast/daily";
    // UV Index (see https://openweathermap.org/api/uvi)
    private static final String UVINDEX_URL = "https://api.openweathermap.org/data/2.5/uvi";
    private static final String UVINDEX_FORECAST_URL = "https://api.openweathermap.org/data/2.5/uvi/forecast";
    // Air Pollution (see https://openweathermap.org/api/air-pollution)
    private static final String AIR_POLLUTION_URL = "https://api.openweathermap.org/data/2.5/air_pollution";
    private static final String AIR_POLLUTION_FORECAST_URL = "https://api.openweathermap.org/data/2.5/air_pollution/forecast";
    // Weather icons (see https://openweathermap.org/weather-conditions)
    private static final String ICON_URL = "https://openweathermap.org/img/w/%s.png";
    // One Call API (see https://openweathermap.org/api/one-call-api )
    private static final String ONECALL_URL = "https://api.openweathermap.org/data";
    private static final String ONECALL_DATA_SUFFIX_URL = "onecall";
    private static final String ONECALL_HISTORY_SUFFIX_URL = "onecall/timemachine";

    private final OpenWeatherMapAPIHandler handler;
    private final HttpClient httpClient;

    private static final ByteArrayFileCache IMAGE_CACHE = new ByteArrayFileCache("org.openhab.binding.openweathermap");
    private final ExpiringCacheMap<String, String> cache;

    private final Gson gson = new Gson();

    public OpenWeatherMapConnection(OpenWeatherMapAPIHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.httpClient = httpClient;

        OpenWeatherMapAPIConfiguration config = handler.getOpenWeatherMapAPIConfig();
        cache = new ExpiringCacheMap<>(TimeUnit.MINUTES.toMillis(config.refreshInterval));
    }

    /**
     * Requests the current weather data for the given location (see https://openweathermap.org/current).
     *
     * @param location location represented as {@link PointType}
     * @return the current weather data
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonWeatherData getWeatherData(@Nullable PointType location)
            throws JsonSyntaxException, CommunicationException, ConfigurationException {
        return gson.fromJson(
                getResponseFromCache(
                        buildURL(WEATHER_URL, getRequestParams(handler.getOpenWeatherMapAPIConfig(), location))),
                OpenWeatherMapJsonWeatherData.class);
    }

    /**
     * Requests the hourly forecast data for the given location (see https://openweathermap.org/forecast5).
     *
     * @param location location represented as {@link PointType}
     * @param count number of hours
     * @return the hourly forecast data
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonHourlyForecastData getHourlyForecastData(
            @Nullable PointType location, int count)
            throws JsonSyntaxException, CommunicationException, ConfigurationException {
        if (count <= 0) {
            throw new ConfigurationException("@text/offline.conf-error-not-supported-number-of-hours");
        }

        Map<String, String> params = getRequestParams(handler.getOpenWeatherMapAPIConfig(), location);
        params.put(PARAM_FORECAST_CNT, Integer.toString(count));

        return gson.fromJson(getResponseFromCache(buildURL(THREE_HOUR_FORECAST_URL, params)),
                OpenWeatherMapJsonHourlyForecastData.class);
    }

    /**
     * Requests the daily forecast data for the given location (see https://openweathermap.org/forecast16).
     *
     * @param location location represented as {@link PointType}
     * @param count number of days
     * @return the daily forecast data
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonDailyForecastData getDailyForecastData(@Nullable PointType location,
            int count) throws JsonSyntaxException, CommunicationException, ConfigurationException {
        if (count <= 0) {
            throw new ConfigurationException("@text/offline.conf-error-not-supported-number-of-days");
        }

        Map<String, String> params = getRequestParams(handler.getOpenWeatherMapAPIConfig(), location);
        params.put(PARAM_FORECAST_CNT, Integer.toString(count));

        return gson.fromJson(getResponseFromCache(buildURL(DAILY_FORECAST_URL, params)),
                OpenWeatherMapJsonDailyForecastData.class);
    }

    /**
     * Requests the UV Index data for the given location (see https://openweathermap.org/api/uvi).
     *
     * @param location location represented as {@link PointType}
     * @return the UV Index data
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonUVIndexData getUVIndexData(@Nullable PointType location)
            throws JsonSyntaxException, CommunicationException, ConfigurationException {
        return gson.fromJson(
                getResponseFromCache(
                        buildURL(UVINDEX_URL, getRequestParams(handler.getOpenWeatherMapAPIConfig(), location))),
                OpenWeatherMapJsonUVIndexData.class);
    }

    /**
     * Requests the UV Index forecast data for the given location (see https://openweathermap.org/api/uvi).
     *
     * @param location location represented as {@link PointType}
     * @return the UV Index forecast data
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable List<OpenWeatherMapJsonUVIndexData> getUVIndexForecastData(
            @Nullable PointType location, int count)
            throws JsonSyntaxException, CommunicationException, ConfigurationException {
        if (count <= 0) {
            throw new ConfigurationException("@text/offline.conf-error-not-supported-uvindex-number-of-days");
        }

        Map<String, String> params = getRequestParams(handler.getOpenWeatherMapAPIConfig(), location);
        params.put(PARAM_FORECAST_CNT, Integer.toString(count));

        return Arrays.asList(gson.fromJson(getResponseFromCache(buildURL(UVINDEX_FORECAST_URL, params)),
                OpenWeatherMapJsonUVIndexData[].class));
    }

    /**
     * Requests the Air Pollution data for the given location (see https://openweathermap.org/api/air-pollution).
     *
     * @param location location represented as {@link PointType}
     * @return the Air Pollution data
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonAirPollutionData getAirPollutionData(@Nullable PointType location)
            throws JsonSyntaxException, CommunicationException, ConfigurationException {
        return gson.fromJson(
                getResponseFromCache(
                        buildURL(AIR_POLLUTION_URL, getRequestParams(handler.getOpenWeatherMapAPIConfig(), location))),
                OpenWeatherMapJsonAirPollutionData.class);
    }

    /**
     * Requests the Air Pollution forecast data for the given location (see
     * https://openweathermap.org/api/air-pollution).
     *
     * @param location location represented as {@link PointType}
     * @return the Air Pollution forecast data
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonAirPollutionData getAirPollutionForecastData(
            @Nullable PointType location) throws JsonSyntaxException, CommunicationException, ConfigurationException {
        return gson.fromJson(
                getResponseFromCache(buildURL(AIR_POLLUTION_FORECAST_URL,
                        getRequestParams(handler.getOpenWeatherMapAPIConfig(), location))),
                OpenWeatherMapJsonAirPollutionData.class);
    }

    /**
     * Downloads the icon for the given icon id (see https://openweathermap.org/weather-conditions).
     *
     * @param iconId the id of the icon
     * @return the weather icon as {@link RawType}
     */
    public static @Nullable RawType getWeatherIcon(String iconId) {
        if (iconId.isEmpty()) {
            throw new IllegalArgumentException("Cannot download weather icon as icon id is null.");
        }

        return downloadWeatherIconFromCache(String.format(ICON_URL, iconId));
    }

    private static @Nullable RawType downloadWeatherIconFromCache(String url) {
        if (IMAGE_CACHE.containsKey(url)) {
            try {
                return new RawType(IMAGE_CACHE.get(url), PNG_CONTENT_TYPE);
            } catch (Exception e) {
                LoggerFactory.getLogger(OpenWeatherMapConnection.class)
                        .trace("Failed to download the content of URL '{}'", url, e);
            }
        } else {
            RawType image = downloadWeatherIcon(url);
            if (image != null) {
                IMAGE_CACHE.put(url, image.getBytes());
                return image;
            }
        }
        return null;
    }

    private static @Nullable RawType downloadWeatherIcon(String url) {
        return HttpUtil.downloadImage(url);
    }

    /**
     * Get Weather data from the One Call API for the given location. See https://openweathermap.org/api/one-call-api
     * for details.
     *
     * @param location location represented as {@link PointType}
     * @param excludeMinutely if true, will not fetch minutely forecast data from the server
     * @param excludeHourly if true, will not fetch hourly forecast data from the server
     * @param excludeDaily if true, will not fetch hourly forecast data from the server
     * @return
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapOneCallAPIData getOneCallAPIData(@Nullable PointType location,
            boolean excludeMinutely, boolean excludeHourly, boolean excludeDaily, boolean excludeAlerts)
            throws JsonSyntaxException, CommunicationException, ConfigurationException {
        Map<String, String> params = getRequestParams(handler.getOpenWeatherMapAPIConfig(), location);
        List<String> exclude = new ArrayList<>();
        if (excludeMinutely) {
            exclude.add("minutely");
        }
        if (excludeHourly) {
            exclude.add("hourly");
        }
        if (excludeDaily) {
            exclude.add("daily");
        }
        if (excludeAlerts) {
            exclude.add("alerts");
        }
        logger.debug("Exclude: '{}'", exclude);
        if (!exclude.isEmpty()) {
            params.put(PARAM_EXCLUDE, exclude.stream().collect(Collectors.joining(",")));
        }
        return gson.fromJson(getResponseFromCache(buildURL(buildOneCallURL(), params)),
                OpenWeatherMapOneCallAPIData.class);
    }

    /**
     * Get the historical weather data from the One Call API for the given location and the given number of days in the
     * past. As of now, OpenWeatherMap supports this function for up to 5 days in the past. However, this may change in
     * the future, so we don't enforce this limit here. See https://openweathermap.org/api/one-call-api for details.
     *
     * @param location location represented as {@link PointType}
     * @param days number of days in the past, relative to the current time.
     * @return
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapOneCallHistAPIData getOneCallHistAPIData(@Nullable PointType location,
            int days) throws JsonSyntaxException, CommunicationException, ConfigurationException {
        Map<String, String> params = getRequestParams(handler.getOpenWeatherMapAPIConfig(), location);
        // the API requests the history as timestamp in Unix time format.
        params.put(PARAM_HISTORY_DATE,
                Long.toString(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(days).toEpochSecond()));
        return gson.fromJson(getResponseFromCache(buildURL(buildOneCallHistoryURL(), params)),
                OpenWeatherMapOneCallHistAPIData.class);
    }

    private Map<String, String> getRequestParams(OpenWeatherMapAPIConfiguration config, @Nullable PointType location) {
        if (location == null) {
            throw new ConfigurationException("@text/offline.conf-error-missing-location");
        }

        Map<String, String> params = new HashMap<>();
        // API key (see https://openweathermap.org/appid)
        String apikey = config.apikey;
        if (apikey == null || (apikey = apikey.trim()).isEmpty()) {
            throw new ConfigurationException("@text/offline.conf-error-missing-apikey");
        }
        params.put(PARAM_APPID, apikey);

        // Units format (see https://openweathermap.org/current#data)
        params.put(PARAM_UNITS, "metric");

        // By geographic coordinates (see https://openweathermap.org/current#geo)
        params.put(PARAM_LAT, location.getLatitude().toString());
        params.put(PARAM_LON, location.getLongitude().toString());

        // Multilingual support (see https://openweathermap.org/current#multi)
        String language = config.language;
        if (language != null && !(language = language.trim()).isEmpty()) {
            params.put(PARAM_LANG, language.toLowerCase());
        }
        return params;
    }

    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.keySet().stream().map(key -> key + "=" + encodeParam(requestParams.get(key)))
                .collect(Collectors.joining("&", url + "?", ""));
    }

    private String buildOneCallURL() {
        var config = handler.getOpenWeatherMapAPIConfig();
        return ONECALL_URL + "/" + config.apiVersion + "/" + ONECALL_DATA_SUFFIX_URL;
    }

    private String buildOneCallHistoryURL() {
        var config = handler.getOpenWeatherMapAPIConfig();
        return ONECALL_URL + "/" + config.apiVersion + "/" + ONECALL_HISTORY_SUFFIX_URL;
    }

    private String encodeParam(@Nullable String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private @Nullable String getResponseFromCache(String url) {
        return cache.putIfAbsentAndGet(url, () -> getResponse(url));
    }

    private String getResponse(String url) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("OpenWeatherMap request: URL = '{}'", uglifyApikey(url));
            }
            ContentResponse contentResponse = httpClient.newRequest(url).method(GET).timeout(10, TimeUnit.SECONDS)
                    .send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            String errorMessage = "";
            logger.trace("OpenWeatherMap response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case OK_200:
                    return content;
                case BAD_REQUEST_400:
                case UNAUTHORIZED_401:
                case NOT_FOUND_404:
                    errorMessage = getErrorMessage(content);
                    logger.debug("OpenWeatherMap server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new ConfigurationException(errorMessage);
                case TOO_MANY_REQUESTS_429:
                    // TODO disable refresh job temporarily (see https://openweathermap.org/appid#Accesslimitation)
                default:
                    errorMessage = getErrorMessage(content);
                    logger.debug("OpenWeatherMap server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new CommunicationException(errorMessage);
            }
        } catch (ExecutionException e) {
            String errorMessage = e.getMessage();
            logger.debug("ExecutionException occurred during execution: {}", errorMessage, e);
            if (e.getCause() instanceof HttpResponseException) {
                logger.debug("OpenWeatherMap server responded with status code {}: Invalid API key.", UNAUTHORIZED_401);
                throw new ConfigurationException("@text/offline.conf-error-invalid-apikey", e.getCause());
            } else {
                throw new CommunicationException(
                        errorMessage == null ? "@text/offline.communication-error" : errorMessage, e.getCause());
            }
        } catch (TimeoutException e) {
            String errorMessage = e.getMessage();
            logger.debug("TimeoutException occurred during execution: {}", errorMessage, e);
            throw new CommunicationException(errorMessage == null ? "@text/offline.communication-error" : errorMessage,
                    e.getCause());
        } catch (InterruptedException e) {
            String errorMessage = e.getMessage();
            logger.debug("InterruptedException occurred during execution: {}", errorMessage, e);
            Thread.currentThread().interrupt();
            throw new CommunicationException(errorMessage == null ? "@text/offline.communication-error" : errorMessage,
                    e.getCause());
        }
    }

    private String uglifyApikey(String url) {
        return url.replaceAll("(appid=)+\\w+", "appid=*****");
    }

    private String getErrorMessage(String response) {
        JsonElement jsonResponse = JsonParser.parseString(response);
        if (jsonResponse.isJsonObject()) {
            JsonObject json = jsonResponse.getAsJsonObject();
            if (json.has(PROPERTY_MESSAGE)) {
                return json.get(PROPERTY_MESSAGE).getAsString();
            }
        }
        return response;
    }
}
