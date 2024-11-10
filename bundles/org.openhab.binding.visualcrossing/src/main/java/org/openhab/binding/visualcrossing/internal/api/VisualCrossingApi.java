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
package org.openhab.binding.visualcrossing.internal.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.openhab.binding.visualcrossing.internal.api.VisualCrossingApi.UnitGroup.METRIC;

import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.visualcrossing.internal.api.dto.Cost;
import org.openhab.binding.visualcrossing.internal.api.dto.WeatherResponse;
import org.openhab.binding.visualcrossing.internal.api.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Documentation: https://www.visualcrossing.com/resources/documentation/weather-api/timeline-weather-api/
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class VisualCrossingApi {
    private final Logger logger = LoggerFactory.getLogger(VisualCrossingApi.class);
    private final AtomicLong cost = new AtomicLong();
    private final String baseUrl;
    private final String apiKey;
    private final RestClient restClient;
    private final Gson gson;

    public VisualCrossingApi(String baseUrl, String apiKey, RestClient restClient, Gson gson) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.restClient = restClient;
        this.gson = gson;
    }

    /**
     * The Timeline Weather API is the simplest and most powerful way to retrieve weather data. You can request data
     * over any time window including windows that span the past, present, and future. The API will take care of the
     * combining historical observations, current 15-day forecasts, and statistical weather forecasts to create a
     * single, consolidated dataset via a single API call.
     * <p>
     * The Timeline API offers complete, global weather data coverage both geographically and chronologically. It always
     * picks the best available data sources to answer any weather API query. These sources include:
     * <ol>
     * <li>Current weather conditions</li>
     * <li>Daily historical, forecast and statistical forecast data (depending on dates requested)</li>
     * <li>Hourly historical observations and 15-day forecast</li>
     * <li>Weather alerts</li>
     * <li>Astronomical observations including sunrise, sunset and moon phase.</li>
     * </ol>
     * <p>
     * Result data is provided in a common JSON format allowing you to ignore to complex underlying data sources and
     * focus entirely on your weather data use case. You can also request the result in CSV text format if you prefer.
     *
     * @param location (required) Is the address, partial address or latitude,longitude location for which to retrieve
     *            weather data. You can also use US ZIP Codes. If you would like to submit multiple locations in the
     *            same request, consider our Multiple Location Timeline Weather API.
     * @param unitGroup (optional) The system of units used for the output data. Supported values are us, uk, metric,
     *            base. See Unit groups and measurement units for more information. Defaults to US system of units.
     * @param lang (optional) Sets the language of the translatable parts of the output such as the conditions field.
     *            Available languages include: ar (Arabic), bg (Bulgiarian), cs (Czech), da (Danish), de (German), el
     *            (Greek Modern), en (English), es (Spanish) ), fa (Farsi), fi (Finnish), fr (French), he Hebrew), hu,
     *            (Hungarian), it (Italian), ja (Japanese), ko (Korean), nl (Dutch), pl (Polish), pt (Portuguese), ru
     *            (Russian), sk (Slovakian), sr (Serbian), sv (Swedish), tr (Turkish), uk (Ukranian), vi (Vietnamese)
     *            and zh (Chinese). In addition passing in ‘id’ will result in the raw descriptor IDs. See <a href=
     *            "https://www.visualcrossing.com/resources/documentation/weather-api/how-to-create-or-modify-language-files/">How
     *            to create or modify language files</a> for more information on how to help add additional languages.
     * @param dateFrom (optional) Is the start date for which to retrieve weather data. If a date2 value is also given,
     *            then it represents the first date for which to retrieve weather data. If no date2 is specified then
     *            weather data for a single day is retrieved, and that date is specified in date1. All dates and times
     *            are in local time of the location specified. Dates should be in the format yyyy-MM-dd. For example
     *            2020-10-19 for October 19th, 2020 or 2017-02-03 for February 3rd, 2017. Instead of an exact date, you
     *            can specify a <a href=
     *            "https://www.visualcrossing.com/resources/documentation/weather-api/using-the-time-period-parameter-to-specify-dynamic-dates-for-weather-api-requests/">dynamic
     *            date period</a>. See below for more details. You may also supply the in “UNIX time”. In this case
     *            provide the number of seconds since 1st January 1970 UTC. For example 1612137600 for Midnight on 1st
     *            February 2021. You can also request the information for a specific time for a single date by including
     *            time into the date1 field using the format yyyy-MM-ddTHH:mm:ss. For example 2020-10-19T13:00:00.The
     *            results are returned in the ‘currentConditions’ field and are truncated to the hour requested (i.e.
     *            2020-10-19T13:59:00 will return data at 2020-10-19T13:00:00).
     * @param dateTo (optional) Is the end date for which to retrieve weather data. This value may only be used when a
     *            date1 value is given. When both date1 and date2 values are given, the query is inclusive of date2 and
     *            the weather data request period will end on midnight of the date2 value. All dates and times are in
     *            local time of the specified location and should be in the format yyyy-MM-dd.
     * @return WeatherResponse from server
     * @throws VisualCrossingApiException in case of an error
     * @throws VisualCrossingAuthException if the API key is not correct
     * @throws VisualCrossingRateException if the rate limit was exceeded
     * @see <a href="https://www.visualcrossing.com/resources/documentation/weather-api/timeline-weather-api/">Timeline
     *      Weather API</a>
     */
    public WeatherResponse timeline(String location, @Nullable UnitGroup unitGroup, @Nullable String lang,
            @Nullable String dateFrom, @Nullable String dateTo)
            throws VisualCrossingApiException, VisualCrossingAuthException, VisualCrossingRateException {
        if (unitGroup == null) {
            unitGroup = METRIC;
        }
        if (dateFrom == null && dateTo != null) {
            throw new VisualCrossingApiException("When passing dateTo you also need to pass dateFrom!");
        }

        var escapedLocation = URLEncoder.encode(location, UTF_8).replaceAll("\\+", "%20");

        var url = new StringBuilder(baseUrl)//
                .append("/VisualCrossingWebServices/rest/services/timeline/")//
                .append(escapedLocation);
        if (dateFrom != null) {
            url.append("/").append(dateFrom);
            if (dateTo != null) {
                url.append("/").append(dateTo);
            }
        }
        url.append("?key=").append(apiKey)//
                .append("&contentType=json")//
                .append("&unitGroup=").append(unitGroup.unit);
        if (lang != null && !lang.isEmpty()) {
            url.append("&lang=").append(lang);
        }
        var response = restClient.get(url.toString());

        try {
            var weatherResponse = requireNonNull(gson.fromJson(response, WeatherResponse.class));
            addCost(weatherResponse);
            return weatherResponse;
        } catch (JsonSyntaxException e) {
            var target = WeatherResponse.class.getSimpleName();
            if (logger.isTraceEnabled()) {
                logger.trace("Cannot parse {} from JSON:\n{}", target, response, e);
            } else {
                logger.debug("Cannot parse {} from JSON", target, e);
            }
            throw new VisualCrossingApiException("Cannot parse %s from JSON!".formatted(target), e);
        }
    }

    private void addCost(Cost cost) {
        var c = cost.queryCost();
        if (c != null) {
            this.cost.addAndGet(c);
        }
    }

    public long getCurrentCost() {
        return cost.get();
    }

    /**
     * @see <a href=
     *      "https://www.visualcrossing.com/resources/documentation/weather-api/unit-groups-and-measurement-units/">Unit
     *      groups and measurement units</a>
     */
    public enum UnitGroup {
        METRIC("metric"),
        US("us"),
        UK("uk");

        private final String unit;

        UnitGroup(String unit) {
            this.unit = unit;
        }
    }
}
