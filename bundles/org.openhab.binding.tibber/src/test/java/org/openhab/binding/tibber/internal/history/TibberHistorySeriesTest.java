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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.history.TibberHistorySeries.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.TimeSeries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Unit tests for {@link TibberHistorySeries}: JSON storage round trip, inclusive time series extraction and
 * merging of GraphQL edges.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TibberHistorySeriesTest {

    private static final Instant TIMESTAMP = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);

    @Test
    void whenSeriesIsSerializedThenConstructorRestoresEntries() {
        // Arrange
        TibberHistorySeries stored = new TibberHistorySeries(null);
        stored.put(TIMESTAMP, data("2.033 kWh", "0.9612", "0.5 kWh"));

        // Act
        TibberHistorySeries restored = new TibberHistorySeries(stored.toString());

        // Assert
        assertEquals(stored.toString(), restored.toString());
        assertEquals(1, restored.size());
        assertEquals(QuantityType.valueOf("2.033 kWh"),
                QuantityType.valueOf(storedEntry(restored, TIMESTAMP).get(PURPOSE_CONSUMPTION).getAsString()));
        assertEquals(QuantityType.valueOf("0.9612"),
                QuantityType.valueOf(storedEntry(restored, TIMESTAMP).get(PURPOSE_COST).getAsString()));
    }

    @Test
    void whenStartEqualsEntryTimestampThenEntryIsIncluded() {
        // Arrange
        TibberHistorySeries stored = new TibberHistorySeries(null);
        stored.put(TIMESTAMP, data("1.5 kWh", "0.3", "0.1 kWh"));
        stored.put(TIMESTAMP.plus(1, ChronoUnit.HOURS), data("2.5 kWh", "0.5", "0.2 kWh"));

        // Act
        TimeSeries series = stored.getTimeSeries(TIMESTAMP, PURPOSE_CONSUMPTION);

        // Assert
        List<TimeSeries.Entry> entries = series.getStates().toList();
        assertEquals(2, entries.size());
        assertEquals(TIMESTAMP, entries.get(0).timestamp());
        assertEquals(QuantityType.valueOf("1.5 kWh"), entries.get(0).state());
    }

    @Test
    void whenStartIsAfterEntryTimestampThenEntryIsExcluded() {
        // Arrange
        TibberHistorySeries stored = new TibberHistorySeries(null);
        stored.put(TIMESTAMP, data("1.5 kWh", "0.3", "0.1 kWh"));

        // Act
        TimeSeries series = stored.getTimeSeries(TIMESTAMP.plus(1, ChronoUnit.SECONDS), PURPOSE_CONSUMPTION);

        // Assert
        assertEquals(0, series.size());
    }

    @Test
    void whenPurposeHasNoDataThenEntryIsSkipped() {
        // Arrange
        TibberHistorySeries stored = new TibberHistorySeries(null);
        stored.put(TIMESTAMP, data("1.5 kWh", "0.3", "0.1 kWh"));
        JsonObject consumptionOnly = new JsonObject();
        consumptionOnly.addProperty(PURPOSE_CONSUMPTION, "7.0 kWh");
        stored.put(TIMESTAMP.plus(1, ChronoUnit.HOURS), consumptionOnly);

        // Act
        TimeSeries consumptionSeries = stored.getTimeSeries(Instant.MIN, PURPOSE_CONSUMPTION);
        TimeSeries costSeries = stored.getTimeSeries(Instant.MIN, PURPOSE_COST);
        TimeSeries productionSeries = stored.getTimeSeries(Instant.MIN, PURPOSE_PRODUCTION);

        // Assert
        assertEquals(2, consumptionSeries.size());
        assertEquals(1, costSeries.size());
        assertEquals(1, productionSeries.size());
    }

    @Test
    void whenEdgesShareTimestampThenPurposeDataIsMerged() {
        // Arrange
        TibberHistorySeries stored = new TibberHistorySeries(null);
        JsonObject consumptionNode = node(TIMESTAMP.toString());
        consumptionNode.addProperty("consumption", "2.033");
        consumptionNode.addProperty("consumptionUnit", "kWh");
        JsonObject costAndProductionNode = node(TIMESTAMP.toString());
        costAndProductionNode.addProperty("cost", "0.9612");
        costAndProductionNode.addProperty("currency", "XYZ");
        costAndProductionNode.addProperty("production", "0.5");
        costAndProductionNode.addProperty("productionUnit", "kWh");

        // Act
        stored.addData(edges(consumptionNode, costAndProductionNode));

        // Assert
        assertEquals(1, stored.size());
        JsonObject merged = storedEntry(stored, TIMESTAMP);
        assertEquals(QuantityType.valueOf("2.033 kWh"),
                QuantityType.valueOf(merged.get(PURPOSE_CONSUMPTION).getAsString()));
        assertEquals(QuantityType.valueOf("0.9612"), QuantityType.valueOf(merged.get(PURPOSE_COST).getAsString()));
        assertEquals(QuantityType.valueOf("0.5 kWh"),
                QuantityType.valueOf(merged.get(PURPOSE_PRODUCTION).getAsString()));
    }

    @Test
    void whenCurrencyIsUnknownThenCostIsStoredWithoutUnit() {
        // Arrange
        TibberHistorySeries stored = new TibberHistorySeries(null);
        JsonObject costNode = node(TIMESTAMP.toString());
        costNode.addProperty("cost", "0.9612");
        costNode.addProperty("currency", "XYZ");

        // Act
        stored.addData(edges(costNode));

        // Assert
        String rawCost = storedEntry(stored, TIMESTAMP).get(PURPOSE_COST).getAsString();
        assertFalse(rawCost.contains(" "), "cost must be stored without unit for unknown currencies");
        assertEquals(QuantityType.valueOf("0.9612"), QuantityType.valueOf(rawCost));
    }

    @Test
    void whenEdgeIsMalformedThenItIsSkipped() {
        // Arrange
        TibberHistorySeries stored = new TibberHistorySeries(null);
        JsonObject validNode = node(TIMESTAMP.toString());
        validNode.addProperty("consumption", "1.0");
        validNode.addProperty("consumptionUnit", "kWh");
        JsonObject validEdge = new JsonObject();
        validEdge.add("node", validNode);
        JsonArray edges = new JsonArray();
        edges.add(new JsonObject());
        edges.add(validEdge);

        // Act
        stored.addData(edges);

        // Assert
        assertEquals(1, stored.size());
        assertEquals(QuantityType.valueOf("1.0 kWh"),
                QuantityType.valueOf(storedEntry(stored, TIMESTAMP).get(PURPOSE_CONSUMPTION).getAsString()));
    }

    private static JsonObject storedEntry(TibberHistorySeries series, Instant timestamp) {
        return Objects.requireNonNull(series.get(timestamp));
    }

    private static JsonObject data(String consumption, String cost, String production) {
        JsonObject data = new JsonObject();
        data.addProperty(PURPOSE_CONSUMPTION, consumption);
        data.addProperty(PURPOSE_COST, cost);
        data.addProperty(PURPOSE_PRODUCTION, production);
        return data;
    }

    private static JsonArray edges(JsonObject... nodes) {
        JsonArray edges = new JsonArray();
        for (JsonObject node : nodes) {
            JsonObject edge = new JsonObject();
            edge.add("node", node);
            edges.add(edge);
        }
        return edges;
    }

    private static JsonObject node(String from) {
        JsonObject node = new JsonObject();
        node.addProperty("from", from);
        return node;
    }
}
