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
package org.openhab.binding.energyforecast;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.energyforecast.internal.EnergyForecastBindingConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.energyforecast.internal.config.EnergyForecastConfiguration;
import org.openhab.binding.energyforecast.internal.dto.PriceInfo;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.types.TimeSeries;

/**
 * {@link PriceInfoTest} to check test calculations based on predefined responses.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class PriceInfoTest {
    private TimeZoneProvider tzp = new TimeZoneProvider() {
        @Override
        public ZoneId getTimeZone() {
            return ZoneId.of("Europe/Berlin");
        }
    };

    String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            fail(e.getMessage());
            return "";
        }
    }

    PriceInfo createPriceInfo(EnergyForecastConfiguration config, VolatileStorage<String> store) {
        CurrencyUnits.addUnit(CurrencyUnits.createCurrency("EUR", "Euro"));
        PriceInfo priceInfo = new PriceInfo(config, store, tzp);

        priceInfo.newPriceSeries(readFile("src/test/resources/2026-02-26-response.json"));
        Map<String, TimeSeries> timeSeriesMap = priceInfo.getTimeSeries();
        TimeSeries maeSeries = timeSeriesMap.get(CHANNEL_METRIC_MAE);
        assertNotNull(maeSeries);
        assertEquals(0, maeSeries.size());
        return priceInfo;
    }

    @Test
    void testMapeValue() {
        PriceInfo priceInfo = createPriceInfo(new EnergyForecastConfiguration(), new VolatileStorage<>());
        priceInfo.newPriceSeries(readFile("src/test/resources/2026-02-27-response.json"));
        Map<String, TimeSeries> timeSeriesMap = priceInfo.getTimeSeries();

        TimeSeries mapeSeries = timeSeriesMap.get(CHANNEL_METRIC_MAPE);
        assertNotNull(mapeSeries);
        assertEquals(1, mapeSeries.size());
        mapeSeries.getStates().forEach(entry -> {
            System.out.println("MAE for " + entry.timestamp() + ": " + entry.state());
        });
    }

    @Test
    void testStorage() {
        VolatileStorage<String> store = new VolatileStorage<>();
        PriceInfo priceInfo = createPriceInfo(new EnergyForecastConfiguration(), store);
        System.out.println(store.get(CHANNEL_METRIC_FORECAST));
        String storeString = store.get(CHANNEL_METRIC_FORECAST);
        JSONObject storedForecast = new JSONObject(storeString);
        assertEquals(23, storedForecast.length(), "Stored forecast should have 23 entries");
    }

    @Test
    void testFixCost() {
        double fixCost = 12.3; // ct/kWh
        EnergyForecastConfiguration configNoFixCosts = new EnergyForecastConfiguration();
        PriceInfo priceInfoNoFixCosts = createPriceInfo(configNoFixCosts, new VolatileStorage<>());
        EnergyForecastConfiguration configWithFixCost = new EnergyForecastConfiguration();
        configWithFixCost.fixCost = fixCost;
        PriceInfo priceInfoWithFixCosts = createPriceInfo(configWithFixCost, new VolatileStorage<>());

        Map<String, TimeSeries> timeSeriesMapNoFixCost = priceInfoNoFixCosts.getTimeSeries();
        TimeSeries priceTimeSeriesNoFixCost = timeSeriesMapNoFixCost.get(CHANNEL_PRICE_SERIES);
        assertNotNull(priceTimeSeriesNoFixCost);

        Map<String, TimeSeries> timeSeriesMapWithFixCost = priceInfoWithFixCosts.getTimeSeries();
        TimeSeries priceTimeSeriesWithFixCost = timeSeriesMapWithFixCost.get(CHANNEL_PRICE_SERIES);
        assertNotNull(priceTimeSeriesWithFixCost);

        Iterator<TimeSeries.Entry> noFixCostIterator = priceTimeSeriesNoFixCost.getStates().iterator();
        Iterator<TimeSeries.Entry> fixCostIterator = priceTimeSeriesWithFixCost.getStates().iterator();

        double EuroPerKWhFixCost = fixCost / 100;
        while (noFixCostIterator.hasNext() && fixCostIterator.hasNext()) {
            TimeSeries.Entry noFixCostEntry = noFixCostIterator.next();
            TimeSeries.Entry fixCostEntry = fixCostIterator.next();

            double noFixCostPrice = ((QuantityType<?>) noFixCostEntry.state()).doubleValue();
            double fixCostPrice = ((QuantityType<?>) fixCostEntry.state()).doubleValue();
            System.out.println("Price with fix cost: " + fixCostPrice + ", Price without fix cost: " + noFixCostPrice);
            assertEquals(EuroPerKWhFixCost, fixCostPrice - noFixCostPrice, 0.0001,
                    "Price difference should be equal to fix cost");
        }
    }
}
