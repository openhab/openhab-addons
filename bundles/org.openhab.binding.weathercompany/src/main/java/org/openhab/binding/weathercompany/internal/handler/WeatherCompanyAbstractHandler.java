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
package org.openhab.binding.weathercompany.internal.handler;

import static org.openhab.binding.weathercompany.internal.WeatherCompanyBindingConstants.CONFIG_LANGUAGE_DEFAULT;
import static org.openhab.core.library.unit.MetricPrefix.MILLI;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Unit;
import javax.measure.spi.SystemOfUnits;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link WeatherCompanyAbstractHandler} contains common utilities used by
 * handlers.
 *
 * Weather Company API documentation is located here
 * - https://docs.google.com/document/d/1eKCnKXI9xnoMGRRzOL1xPCBihNV2rOet08qpE_gArAY/edit
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public abstract class WeatherCompanyAbstractHandler extends BaseThingHandler {
    protected static final int WEATHER_COMPANY_API_TIMEOUT_SECONDS = 15;
    protected static final int REFRESH_JOB_INITIAL_DELAY_SECONDS = 6;

    private final Logger logger = LoggerFactory.getLogger(WeatherCompanyAbstractHandler.class);

    protected final Gson gson = new GsonBuilder().serializeNulls().create();

    protected final Map<String, State> weatherDataCache = Collections.synchronizedMap(new HashMap<>());

    // Provided by handler factory
    private final TimeZoneProvider timeZoneProvider;
    private final HttpClient httpClient;
    private final SystemOfUnits systemOfUnits;

    public WeatherCompanyAbstractHandler(Thing thing, TimeZoneProvider timeZoneProvider, HttpClient httpClient,
            UnitProvider unitProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.httpClient = httpClient;
        this.systemOfUnits = unitProvider.getMeasurementSystem();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    protected boolean isBridgeOnline() {
        boolean bridgeStatus = false;
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            bridgeStatus = true;
        }
        return bridgeStatus;
    }

    protected String getApiKey() {
        String apiKey = "unknown";
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            WeatherCompanyBridgeHandler handler = (WeatherCompanyBridgeHandler) bridge.getHandler();
            if (handler != null) {
                String key = handler.getApiKey();
                if (key != null) {
                    apiKey = key;
                }
            }
        }
        return apiKey;
    }

    /*
     * Set either Imperial or Metric SI for the API call
     */
    protected String getUnitsQueryString() {
        return isImperial() ? "e" : "s";
    }

    /*
     * Determine the units configured in the system
     */
    protected boolean isImperial() {
        return systemOfUnits instanceof ImperialUnits ? true : false;
    }

    protected void updateChannel(String channelId, State state) {
        // Only update channel if it's linked
        if (isLinked(channelId)) {
            updateState(channelId, state);
            weatherDataCache.put(channelId, state);
        }
    }

    /*
     * Set the state to the passed value. If value is null, set the state to UNDEF
     */
    protected State undefOrString(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : new StringType(value);
    }

    protected State undefOrDate(@Nullable Integer value) {
        return value == null ? UnDefType.UNDEF : getLocalDateTimeType(value);
    }

    protected State undefOrDate(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : getLocalDateTimeType(value);
    }

    protected State undefOrDecimal(@Nullable Number value) {
        return value == null ? UnDefType.UNDEF : new DecimalType(value.doubleValue());
    }

    protected State undefOrQuantity(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    protected State undefOrPoint(@Nullable Number lat, @Nullable Number lon) {
        return lat != null && lon != null
                ? new PointType(new DecimalType(lat.doubleValue()), new DecimalType(lon.doubleValue()))
                : UnDefType.UNDEF;
    }

    /*
     * The API will request units based on openHAB's SystemOfUnits setting. Therefore,
     * when setting the QuantityType state, make sure we use the proper unit.
     */
    protected Unit<?> getTempUnit() {
        return isImperial() ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;
    }

    protected Unit<?> getSpeedUnit() {
        return isImperial() ? ImperialUnits.MILES_PER_HOUR : Units.METRE_PER_SECOND;
    }

    protected Unit<?> getLengthUnit() {
        return isImperial() ? ImperialUnits.INCH : MILLI(SIUnits.METRE);
    }

    /*
     * Execute the The Weather Channel API request
     */
    protected @Nullable String executeApiRequest(@Nullable String url) {
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
            errorMsg = "@text/offline.comm-error-timeout";
        } catch (ExecutionException e) {
            errorMsg = String.format("ExecutionException: %s", e.getMessage());
        } catch (InterruptedException e) {
            errorMsg = String.format("InterruptedException: %s", e.getMessage());
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMsg);
        return null;
    }

    /*
     * Convert UTC Unix epoch seconds to local time
     */
    protected DateTimeType getLocalDateTimeType(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        ZonedDateTime localDateTime = instant.atZone(getZoneId());
        return new DateTimeType(localDateTime);
    }

    /*
     * Convert UTC time string to local time
     * Input string is of form 2018-12-02T10:47:00.000Z
     */
    protected State getLocalDateTimeType(String dateTimeString) {
        State dateTimeType;
        try {
            Instant instant = Instant.parse(dateTimeString);
            ZonedDateTime localDateTime = instant.atZone(getZoneId());
            dateTimeType = new DateTimeType(localDateTime);
        } catch (DateTimeParseException e) {
            logger.debug("Error parsing date/time string: {}", e.getMessage());
            dateTimeType = UnDefType.UNDEF;
        }
        return dateTimeType;
    }

    private ZoneId getZoneId() {
        return timeZoneProvider.getTimeZone();
    }

    /*
     * Called by discovery service to get TWC language based on
     * the locale configured in openHAB
     */
    public static String lookupLanguage(Locale locale) {
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
