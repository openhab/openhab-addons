/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static java.util.stream.Collectors.joining;
import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapAPIConfiguration;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonDailyForecastData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonHourlyForecastData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonUVIndexData;
import org.openhab.binding.openweathermap.internal.dto.OpenWeatherMapJsonWeatherData;
import org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapAPIHandler;
import org.openhab.core.cache.ByteArrayFileCache;
import org.openhab.core.cache.ExpiringCacheMap;
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

    // Current weather data (see https://openweathermap.org/current)
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    // 5 day / 3 hour forecast (see https://openweathermap.org/forecast5)
    private static final String THREE_HOUR_FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    // 16 day / daily forecast (see https://openweathermap.org/forecast16)
    private static final String DAILY_FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast/daily";
    // UV Index (see https://openweathermap.org/api/uvi)
    private static final String UVINDEX_URL = "https://api.openweathermap.org/data/2.5/uvi";
    private static final String UVINDEX_FORECAST_URL = "https://api.openweathermap.org/data/2.5/uvi/forecast";
    // Weather icons (see https://openweathermap.org/weather-conditions)
    private static final String ICON_URL = "https://openweathermap.org/img/w/%s.png";

    private final OpenWeatherMapAPIHandler handler;
    private final HttpClient httpClient;

    private static final ByteArrayFileCache IMAGE_CACHE = new ByteArrayFileCache("org.openhab.binding.openweathermap");
    private final ExpiringCacheMap<String, String> cache;

    private final JsonParser parser = new JsonParser();
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
     * @throws OpenWeatherMapCommunicationException
     * @throws OpenWeatherMapConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonWeatherData getWeatherData(@Nullable PointType location)
            throws JsonSyntaxException, OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
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
     * @throws OpenWeatherMapCommunicationException
     * @throws OpenWeatherMapConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonHourlyForecastData getHourlyForecastData(
            @Nullable PointType location, int count)
            throws JsonSyntaxException, OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        if (count <= 0) {
            throw new OpenWeatherMapConfigurationException("@text/offline.conf-error-not-supported-number-of-hours");
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
     * @throws OpenWeatherMapCommunicationException
     * @throws OpenWeatherMapConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonDailyForecastData getDailyForecastData(@Nullable PointType location,
            int count)
            throws JsonSyntaxException, OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        if (count <= 0) {
            throw new OpenWeatherMapConfigurationException("@text/offline.conf-error-not-supported-number-of-days");
        }

        Map<String, String> params = getRequestParams(handler.getOpenWeatherMapAPIConfig(), location);
        params.put(PARAM_FORECAST_CNT, Integer.toString(count));

        return gson.fromJson(getResponseFromCache(buildURL(DAILY_FORECAST_URL, params)),
                OpenWeatherMapJsonDailyForecastData.class);
    }

    /**
     * Requests the UV Index data for the given location (see https://api.openweathermap.org/data/2.5/uvi).
     *
     * @param location location represented as {@link PointType}
     * @return the UV Index data
     * @throws JsonSyntaxException
     * @throws OpenWeatherMapCommunicationException
     * @throws OpenWeatherMapConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonUVIndexData getUVIndexData(@Nullable PointType location)
            throws JsonSyntaxException, OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        return gson.fromJson(
                getResponseFromCache(
                        buildURL(UVINDEX_URL, getRequestParams(handler.getOpenWeatherMapAPIConfig(), location))),
                OpenWeatherMapJsonUVIndexData.class);
    }

    /**
     * Requests the UV Index forecast data for the given location (see https://api.openweathermap.org/data/2.5/uvi).
     *
     * @param location location represented as {@link PointType}
     * @return the UV Index forecast data
     * @throws JsonSyntaxException
     * @throws OpenWeatherMapCommunicationException
     * @throws OpenWeatherMapConfigurationException
     */
    public synchronized @Nullable List<OpenWeatherMapJsonUVIndexData> getUVIndexForecastData(
            @Nullable PointType location, int count)
            throws JsonSyntaxException, OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        if (count <= 0) {
            throw new OpenWeatherMapConfigurationException(
                    "@text/offline.conf-error-not-supported-uvindex-number-of-days");
        }

        Map<String, String> params = getRequestParams(handler.getOpenWeatherMapAPIConfig(), location);
        params.put(PARAM_FORECAST_CNT, Integer.toString(count));

        return Arrays.asList(gson.fromJson(getResponseFromCache(buildURL(UVINDEX_FORECAST_URL, params)),
                OpenWeatherMapJsonUVIndexData[].class));
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
            } catch (IOException e) {
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

    private Map<String, String> getRequestParams(OpenWeatherMapAPIConfiguration config, @Nullable PointType location) {
        if (location == null) {
            throw new OpenWeatherMapConfigurationException("@text/offline.conf-error-missing-location");
        }

        Map<String, String> params = new HashMap<>();
        // API key (see http://openweathermap.org/appid)
        String apikey = config.apikey;
        if (apikey == null || (apikey = apikey.trim()).isEmpty()) {
            throw new OpenWeatherMapConfigurationException("@text/offline.conf-error-missing-apikey");
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
                .collect(joining("&", url + "?", ""));
    }

    private String encodeParam(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.debug("UnsupportedEncodingException occurred during execution: {}", e.getLocalizedMessage(), e);
            return "";
        }
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
                    throw new OpenWeatherMapConfigurationException(errorMessage);
                case TOO_MANY_REQUESTS_429:
                    // TODO disable refresh job temporarily (see https://openweathermap.org/appid#Accesslimitation)
                default:
                    errorMessage = getErrorMessage(content);
                    logger.debug("OpenWeatherMap server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new OpenWeatherMapCommunicationException(errorMessage);
            }
        } catch (ExecutionException e) {
            String errorMessage = e.getLocalizedMessage();
            logger.trace("Exception occurred during execution: {}", errorMessage, e);
            if (e.getCause() instanceof HttpResponseException) {
                logger.debug("OpenWeatherMap server responded with status code {}: Invalid API key.", UNAUTHORIZED_401);
                throw new OpenWeatherMapConfigurationException("@text/offline.conf-error-invalid-apikey", e.getCause());
            } else {
                throw new OpenWeatherMapCommunicationException(errorMessage, e.getCause());
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new OpenWeatherMapCommunicationException(e.getLocalizedMessage(), e.getCause());
        }
    }

    private String uglifyApikey(String url) {
        return url.replaceAll("(appid=)+\\w+", "appid=*****");
    }

    private String getErrorMessage(String response) {
        JsonElement jsonResponse = parser.parse(response);
        if (jsonResponse.isJsonObject()) {
            JsonObject json = jsonResponse.getAsJsonObject();
            if (json.has(PROPERTY_MESSAGE)) {
                return json.get(PROPERTY_MESSAGE).getAsString();
            }
        }
        return response;
    }
}
