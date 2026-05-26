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
package org.openhab.binding.tibber.internal.history;

import java.time.Instant;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TibberHistorySeries} provides storage for energy consumption, cost and production.
 * It is a {@link TreeMap} keyed by {@link Instant} containing merged data entries from both
 * the consumption and production GraphQL queries.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Add history channel group
 */
@NonNullByDefault
public class TibberHistorySeries extends TreeMap<Instant, JsonObject> {

    private static final long serialVersionUID = -6635491873715495757L;
    private static final Gson GSON = new Gson();
    private final Logger logger = LoggerFactory.getLogger(TibberHistorySeries.class);

    public static final String DATE_TIME = "dateTime";
    public static final String DATA = "data";
    public static final String PURPOSE_CONSUMPTION = "consumption";
    public static final String PURPOSE_COST = "cost";
    public static final String PURPOSE_PRODUCTION = "production";

    /**
     * Creates a new TibberHistorySeries, optionally pre-populated from a stored JSON string.
     *
     * @param storedData JSON string previously produced by {@link #toString()}, or null for an empty series
     */
    public TibberHistorySeries(@Nullable String storedData) {
        if (storedData != null) {
            try {
                JsonArray storedArray = (JsonArray) JsonParser.parseString(storedData);
                storedArray.forEach(entry -> {
                    Instant key = Instant.parse(entry.getAsJsonObject().get(DATE_TIME).getAsString());
                    JsonObject value = entry.getAsJsonObject().get(DATA).getAsJsonObject();
                    put(key, value);
                });
            } catch (JsonSyntaxException jse) {
                logger.warn("Couldn't parse stored history data: {}", storedData);
            }
        }
    }

    /**
     * Merges a GraphQL edges array into this series.
     * Each edge node may carry consumption, cost and/or production fields.
     *
     * @param data the {@code edges} JsonArray from the GraphQL response
     */
    public void addData(JsonArray data) {
        data.forEach(element -> {
            try {
                JsonObject entry = element.getAsJsonObject();
                JsonObject arrayEntry = entry.get("node").getAsJsonObject();
                String startTime = arrayEntry.get("from").getAsString();
                Instant key = Instant.parse(startTime);
                JsonObject historyDataElement = get(key);
                if (historyDataElement == null) {
                    historyDataElement = new JsonObject();
                }
                if (arrayEntry.has(PURPOSE_CONSUMPTION) && !arrayEntry.get(PURPOSE_CONSUMPTION).isJsonNull()) {
                    QuantityType<?> consumptionState = QuantityType
                            .valueOf(arrayEntry.get(PURPOSE_CONSUMPTION).getAsString() + " "
                                    + arrayEntry.get("consumptionUnit").getAsString());
                    historyDataElement.addProperty(PURPOSE_CONSUMPTION, consumptionState.toFullString());
                }
                if (arrayEntry.has(PURPOSE_COST) && !arrayEntry.get(PURPOSE_COST).isJsonNull()) {
                    State costState;
                    Unit<?> currencyUnit = CurrencyUnits.getInstance()
                            .getUnit(arrayEntry.get("currency").getAsString());
                    if (currencyUnit != null) {
                        costState = QuantityType.valueOf(
                                arrayEntry.get("cost").getAsString() + " " + arrayEntry.get("currency").getAsString());
                    } else {
                        costState = DecimalType.valueOf(arrayEntry.get("cost").getAsString());
                    }
                    historyDataElement.addProperty(PURPOSE_COST, costState.toFullString());
                }
                if (arrayEntry.has(PURPOSE_PRODUCTION) && !arrayEntry.get(PURPOSE_PRODUCTION).isJsonNull()) {
                    QuantityType<?> productionState = QuantityType
                            .valueOf(arrayEntry.get(PURPOSE_PRODUCTION).getAsString() + " "
                                    + arrayEntry.get("productionUnit").getAsString());
                    historyDataElement.addProperty(PURPOSE_PRODUCTION, productionState.toFullString());
                }
                put(key, historyDataElement);
            } catch (RuntimeException e) {
                logger.warn("Skipping history entry due to parse error: {} — entry: {}", e.getMessage(), element);
            }
        });
    }

    /**
     * Returns a {@link TimeSeries} for the given purpose, containing only entries at or after {@code start}.
     *
     * @param start earliest timestamp to include (use {@link Instant#MIN} for all entries)
     * @param purpose one of {@link #PURPOSE_CONSUMPTION}, {@link #PURPOSE_COST}, {@link #PURPOSE_PRODUCTION}
     * @return a TimeSeries with REPLACE policy
     */
    public TimeSeries getTimeSeries(Instant start, String purpose) {
        TimeSeries series = new TimeSeries(TimeSeries.Policy.REPLACE);
        // Defensive copy of the live-backed tailMap view to avoid ConcurrentModificationException
        // if another thread merges new data into this series while we iterate.
        SortedMap<Instant, JsonObject> snapshot = new TreeMap<>(this.tailMap(start));
        snapshot.forEach((time, historyElement) -> {
            if (historyElement.has(purpose)) {
                series.add(time, QuantityType.valueOf(historyElement.get(purpose).getAsString()));
            }
        });
        return series;
    }

    /**
     * Serialises this series to a JSON string that can be stored and later restored
     * via {@link #TibberHistorySeries(String)}.
     */
    @Override
    public String toString() {
        JsonArray array = new JsonArray();
        forEach((time, historyElement) -> {
            JsonObject elem = new JsonObject();
            elem.addProperty(DATE_TIME, time.toString());
            elem.add(DATA, historyElement);
            array.add(elem);
        });
        return GSON.toJson(array);
    }
}
