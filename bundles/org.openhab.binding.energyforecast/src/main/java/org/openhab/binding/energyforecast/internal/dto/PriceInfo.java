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
package org.openhab.binding.energyforecast.internal.dto;

import static org.openhab.binding.energyforecast.internal.EnergyForecastBindingConstants.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.energyforecast.internal.config.EnergyForecastConfiguration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.storage.Storage;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PriceInfo} class is responsible for storing the price information, calculating the metrics and providing
 * timeseries
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PriceInfo {

    private static final String JSON_KEY_FORECAST = "forecast";
    private static final String JSON_KEY_MARKET = "market";

    private final Logger logger = LoggerFactory.getLogger(PriceInfo.class);
    private final Storage<String> store;
    private final TimeZoneProvider tzp;

    private EnergyForecastConfiguration config;

    // map with market and forecast prices
    private SortedMap<Instant, Double> priceSeries = new TreeMap<>();
    private SortedMap<Instant, String> originSeries = new TreeMap<>();

    // for metrics
    // map keeping all forecast data
    private SortedMap<Instant, Double> forecastSeries = new TreeMap<>();
    private SortedMap<Instant, Double> forecastErrorSeries = new TreeMap<>();
    private SortedMap<Instant, Double> percentErrorSeries = new TreeMap<>();
    private Double maeValue = Double.NaN;
    private Double mapeValue = Double.NaN;

    public PriceInfo(EnergyForecastConfiguration config, Storage<String> store, TimeZoneProvider tzp) {
        this.config = config;
        this.store = store;
        this.tzp = tzp;
        restore();
    }

    private void restore() {
        String forecastString = store.get(JSON_KEY_FORECAST);
        if (forecastString != null) {
            JSONObject forecastJson = new JSONObject(forecastString);
            forecastJson.keySet().forEach(key -> {
                forecastSeries.put(Instant.parse(key), forecastJson.getDouble(key));
            });
            logger.debug("Restored {} forecast entries from storage", forecastJson.length());
        } else {
            logger.debug("No forecast data found in storage, starting with empty series.");
        }
    }

    public synchronized void newPriceSeries(String prices) {
        JSONArray priceArray = new JSONArray(prices);
        priceArray.forEach(item -> {
            JSONObject jsonObject = (JSONObject) item;
            Instant start = Instant.parse(jsonObject.getString("start"));
            Double price = jsonObject.getDouble("price");
            String origin = jsonObject.getString("price_origin");
            priceSeries.put(start, price);
            originSeries.put(start, origin);
            if (JSON_KEY_FORECAST.equals(origin)) {
                forecastSeries.put(start, price);
            }
        });
        store.put(JSON_KEY_FORECAST, new JSONObject(forecastSeries).toString());
        calculateMetrics();
    }

    private void calculateMetrics() {
        originSeries.forEach((time, orgin) -> {
            if (JSON_KEY_MARKET.equals(orgin)) {
                Double marketPrice = priceSeries.get(time);
                Double forecastPrice = forecastSeries.get(time);
                if (marketPrice != null && forecastPrice != null) {
                    // forecast error
                    double forecastError = marketPrice - forecastPrice;
                    forecastErrorSeries.put(time, forecastError);

                    // percentage error
                    double percentageError = 0;
                    if (marketPrice == 0) {
                        // cannot use UNDEF here, a value is mandatory
                        percentageError = Double.MIN_VALUE;
                    } else {
                        percentageError = (forecastError / marketPrice) * 100;
                    }
                    percentErrorSeries.put(time, percentageError);
                }
            }
        });
        maeValue = forecastErrorSeries.values().stream().mapToDouble(value -> Math.abs(value)).sum()
                / forecastErrorSeries.size();
        // remove all undefined values for mape calculation
        SortedMap<Instant, Double> cleanPercentErrorSeries = percentErrorSeries.entrySet().stream()
                .filter(entry -> entry.getValue() != Double.MIN_VALUE)
                .collect(TreeMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), TreeMap::putAll);
        mapeValue = cleanPercentErrorSeries.values().stream().filter(value -> value != Double.MIN_VALUE)
                .mapToDouble(value -> Math.abs(value)).sum() / cleanPercentErrorSeries.size();
    }

    public synchronized void consolidate() {
        Instant startOfDay = LocalDate.now(tzp.getTimeZone()).atStartOfDay(tzp.getTimeZone()).toInstant();
        priceSeries = priceSeries.tailMap(startOfDay);
        originSeries = originSeries.tailMap(startOfDay);
        forecastSeries = forecastSeries.tailMap(startOfDay);
        forecastErrorSeries = forecastErrorSeries.tailMap(startOfDay);
        percentErrorSeries = percentErrorSeries.tailMap(startOfDay);
    }

    public synchronized Map<String, TimeSeries> getTimeSeries() {
        Map<String, TimeSeries> timeSeriesMap = new HashMap<>();
        timeSeriesMap.put(CHANNEL_PRICE_SERIES, getPriceTimeSeries(priceSeries, true));
        timeSeriesMap.put(CHANNEL_PRICE_ORIGIN, getOriginsTimeSeries(originSeries));
        timeSeriesMap.put(CHANNEL_METRIC_FORECAST, getPriceTimeSeries(forecastSeries, true));
        timeSeriesMap.put(CHANNEL_METRIC_FORECAST_ERROR, getPriceTimeSeries(forecastErrorSeries, false));
        timeSeriesMap.put(CHANNEL_METRIC_PERCENT_ERROR, getPercentTimeSeries(percentErrorSeries));
        timeSeriesMap.put(CHANNEL_METRIC_MAE, getSingleValueSeries(maeValue, " EUR/kWh"));
        timeSeriesMap.put(CHANNEL_METRIC_MAPE, getSingleValueSeries(mapeValue, " %"));
        return timeSeriesMap;
    }

    private TimeSeries getPercentTimeSeries(SortedMap<Instant, Double> source) {
        TimeSeries series = new TimeSeries(TimeSeries.Policy.REPLACE);
        source.forEach((time, value) -> {
            series.add(time, QuantityType.valueOf(value + " %"));
        });
        return series;
    }

    private TimeSeries getPriceTimeSeries(SortedMap<Instant, Double> source, boolean priceCorrection) {
        TimeSeries series = new TimeSeries(TimeSeries.Policy.REPLACE);
        source.forEach((time, value) -> {
            double price = priceCorrection ? (value + (config.fixCost / 100)) : value;
            series.add(time, QuantityType.valueOf(price + " EUR/kWh"));
        });
        return series;
    }

    private TimeSeries getOriginsTimeSeries(SortedMap<Instant, String> source) {
        TimeSeries series = new TimeSeries(TimeSeries.Policy.REPLACE);
        source.forEach((time, value) -> {
            int origin = JSON_KEY_MARKET.equals(value) ? 0 : 1;
            series.add(time, new DecimalType(origin));
        });
        return series;
    }

    private TimeSeries getSingleValueSeries(Double value, String unit) {
        TimeSeries series = new TimeSeries(TimeSeries.Policy.REPLACE);
        if (!value.isNaN()) {
            series.add(Instant.now(), QuantityType.valueOf(value + unit));
        }
        return series;
    }

    public void handleRemoval() {
        store.remove(JSON_KEY_FORECAST);
    }
}
