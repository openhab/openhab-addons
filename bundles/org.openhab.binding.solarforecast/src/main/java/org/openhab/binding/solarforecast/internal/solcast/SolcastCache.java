/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast.internal.solcast;

import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolcastCache} cache values from Solcast API responses and store them for usage to reduce API calls
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastCache {
    public static final String FORECAST_APPENDIX = "-forecast";

    private final Logger logger = LoggerFactory.getLogger(SolcastCache.class);
    private final Storage<String> storage;
    private final String identifier;
    private JSONArray cache = new JSONArray();
    Instant cacheStart = Instant.MAX;
    Instant cacheEnd = Instant.MIN;

    public SolcastCache(String identifier, Storage<String> storage) {
        this.identifier = identifier;
        this.storage = storage;
        restore();
    }

    public JSONArray getForecast() {
        return cache;
    }

    public boolean isFilled() {
        Instant todayStart = Utils.startOfDayInstant();
        return !cacheStart.isAfter(todayStart) && cacheEnd.isAfter(todayStart.plus(1, ChronoUnit.DAYS));
    }

    public void update(JSONObject newForecast) {
        JSONArray newForecastArray;
        if (newForecast.has(KEY_ACTUALS)) {
            newForecastArray = newForecast.getJSONArray(KEY_ACTUALS);
        } else if (newForecast.has(KEY_FORECAST)) {
            newForecastArray = newForecast.getJSONArray(KEY_FORECAST);
        } else {
            logger.warn("No valid forecast data found in the provided JSON object");
            return;
        }

        assureValues(newForecastArray);
        cache = merge(cache, newForecastArray);
        compactCache();
    }

    public void assureValues(JSONArray newForecastArray) {
        newForecastArray.forEach(entry -> {
            JSONObject forecastEntry = (JSONObject) entry;
            if (!forecastEntry.has(KEY_ESTIMATE10)) {
                forecastEntry.put(KEY_ESTIMATE10, forecastEntry.getDouble(KEY_ESTIMATE));
            }
            if (!forecastEntry.has(KEY_ESTIMATE90)) {
                forecastEntry.put(KEY_ESTIMATE90, forecastEntry.getDouble(KEY_ESTIMATE));
            }
        });
    }

    private void restore() {
        if (storage.containsKey(identifier + FORECAST_APPENDIX)) {
            cache = new JSONArray(storage.get(identifier + FORECAST_APPENDIX));
            compactCache();
        }
    }

    private void compactCache() {
        JSONArray compactedCache = new JSONArray();
        Instant todayStart = Utils.startOfDayInstant();
        for (int i = 0; i < cache.length(); i++) {
            JSONObject forecastJson = cache.getJSONObject(i);
            String periodEnd = forecastJson.getString(KEY_PERIOD_END);
            Instant timestamp = Instant.parse(periodEnd);
            if (!timestamp.isBefore(todayStart)) {
                compactedCache.put(forecastJson);
            }
        }
        cache = compactedCache;
        calculateStartEnd();
        // store data in storage for later use e.g. after restart
        storage.put(identifier + FORECAST_APPENDIX, cache.toString());
    }

    private void calculateStartEnd() {
        Instant cacheStart = Instant.MAX;
        Instant cacheEnd = Instant.MIN;
        for (int i = 0; i < cache.length(); i++) {
            JSONObject forecastJson = cache.getJSONObject(i);
            String periodEnd = forecastJson.getString(KEY_PERIOD_END);
            Instant timeStmp = Instant.parse(periodEnd);
            if (timeStmp.isBefore(cacheStart)) {
                cacheStart = timeStmp;
            }
            if (timeStmp.isAfter(cacheEnd)) {
                cacheEnd = timeStmp;
            }
        }
        this.cacheStart = cacheStart;
        this.cacheEnd = cacheEnd;
    }

    public static JSONArray merge(JSONArray first, JSONArray second) {
        TreeMap<Instant, JSONObject> mergedMap = new TreeMap<>();
        first.forEach(entry -> {
            JSONObject forecastJson = (JSONObject) entry;
            String periodEnd = forecastJson.getString(KEY_PERIOD_END);
            mergedMap.put(Instant.parse(periodEnd), forecastJson);
        });
        second.forEach(entry -> {
            JSONObject forecastJson = (JSONObject) entry;
            String periodEnd = forecastJson.getString(KEY_PERIOD_END);
            mergedMap.put(Instant.parse(periodEnd), forecastJson);
        });
        return new JSONArray(mergedMap.values());
    }

    @Override
    public String toString() {
        return "SolcastObjectCache [identifier=" + identifier + ", cacheStart=" + cacheStart + ", cacheEnd=" + cacheEnd
                + ", cache length=" + cache.length() + "]";
    }
}
