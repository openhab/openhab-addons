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
package org.openhab.binding.darksky.internal.connection;

import static java.util.stream.Collectors.joining;
import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.darksky.internal.config.DarkSkyAPIConfiguration;
import org.openhab.binding.darksky.internal.handler.DarkSkyAPIHandler;
import org.openhab.binding.darksky.internal.model.DarkSkyJsonWeatherData;
import org.openhab.core.cache.ByteArrayFileCache;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link DarkSkyConnection} is responsible for handling the connections to Dark Sky API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DarkSkyConnection {

    private final Logger logger = LoggerFactory.getLogger(DarkSkyConnection.class);

    private static final String PNG_CONTENT_TYPE = "image/png";

    private static final String PARAM_EXCLUDE = "exclude";
    private static final String PARAM_UNITS = "units";
    private static final String PARAM_LANG = "lang";

    // Current weather data (see https://darksky.net/dev/docs#forecast-request)
    private static final String WEATHER_URL = "https://api.darksky.net/forecast/%s/%f,%f";
    // Weather icons (see https://darksky.net/dev/docs/faq#icons)
    private static final String ICON_URL = "https://darksky.net/images/weather-icons/%s.png";

    private final DarkSkyAPIHandler handler;
    private final HttpClient httpClient;

    private static final ByteArrayFileCache IMAGE_CACHE = new ByteArrayFileCache("org.openhab.binding.darksky");
    private final ExpiringCacheMap<String, String> cache;

    private final Gson gson = new Gson();

    public DarkSkyConnection(DarkSkyAPIHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.httpClient = httpClient;

        DarkSkyAPIConfiguration config = handler.getDarkSkyAPIConfig();
        cache = new ExpiringCacheMap<>(TimeUnit.MINUTES.toMillis(config.refreshInterval));
    }

    /**
     * Requests the current weather data for the given location (see https://darksky.net/dev/docs#forecast-request).
     *
     * @param location location represented as {@link PointType}
     * @return the current weather data
     * @throws JsonSyntaxException
     * @throws DarkSkyCommunicationException
     * @throws DarkSkyConfigurationException
     */
    public synchronized @Nullable DarkSkyJsonWeatherData getWeatherData(@Nullable PointType location)
            throws JsonSyntaxException, DarkSkyCommunicationException, DarkSkyConfigurationException {
        if (location == null) {
            throw new DarkSkyConfigurationException("@text/offline.conf-error-missing-location");
        }

        DarkSkyAPIConfiguration config = handler.getDarkSkyAPIConfig();
        String apikey = config.apikey;
        if (apikey == null || (apikey = apikey.trim()).isEmpty()) {
            throw new DarkSkyConfigurationException("@text/offline.conf-error-missing-apikey");
        }

        String url = String.format(Locale.ROOT, WEATHER_URL, apikey, location.getLatitude().doubleValue(),
                location.getLongitude().doubleValue());

        return gson.fromJson(getResponseFromCache(buildURL(url, getRequestParams(config))),
                DarkSkyJsonWeatherData.class);
    }

    /**
     * Downloads the icon for the given icon id (see https://darksky.net/dev/docs/faq#icons).
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
                LoggerFactory.getLogger(DarkSkyConnection.class).trace("Failed to download the content of URL '{}'",
                        url, e);
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

    private Map<String, String> getRequestParams(DarkSkyAPIConfiguration config) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_EXCLUDE, "minutely,flags");

        // params.put(PARAM_EXTEND, "hourly");

        params.put(PARAM_UNITS, "si");

        String language = config.language;
        if (language != null && !(language = language.trim()).isEmpty()) {
            params.put(PARAM_LANG, language.toLowerCase());
        }
        return params;
    }

    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.entrySet().stream().map(e -> e.getKey() + "=" + encodeParam(e.getValue()))
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
                logger.trace("Dark Sky request: URL = '{}'", uglifyApikey(url));
            }
            ContentResponse contentResponse = httpClient.newRequest(url).method(GET).timeout(10, TimeUnit.SECONDS)
                    .send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            logger.trace("Dark Sky response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case OK_200:
                    return content;
                case BAD_REQUEST_400:
                case UNAUTHORIZED_401:
                case NOT_FOUND_404:
                    logger.debug("Dark Sky server responded with status code {}: {}", httpStatus, content);
                    throw new DarkSkyConfigurationException(content);
                default:
                    logger.debug("Dark Sky server responded with status code {}: {}", httpStatus, content);
                    throw new DarkSkyCommunicationException(content);
            }
        } catch (ExecutionException e) {
            String errorMessage = e.getLocalizedMessage();
            logger.trace("Exception occurred during execution: {}", errorMessage, e);
            if (e.getCause() instanceof HttpResponseException) {
                logger.debug("Dark Sky server responded with status code {}: Invalid API key.", UNAUTHORIZED_401);
                throw new DarkSkyConfigurationException("@text/offline.conf-error-invalid-apikey", e.getCause());
            } else {
                throw new DarkSkyCommunicationException(errorMessage, e.getCause());
            }
        } catch (TimeoutException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new DarkSkyCommunicationException(e.getLocalizedMessage(), e.getCause());
        } catch (InterruptedException e) {
            logger.debug("Execution interrupted: {}", e.getLocalizedMessage(), e);
            Thread.currentThread().interrupt();
            throw new DarkSkyCommunicationException(e.getLocalizedMessage(), e.getCause());
        }
    }

    private String uglifyApikey(String url) {
        return url.replaceAll("(appid=)+\\w+", "appid=*****");
    }
}
