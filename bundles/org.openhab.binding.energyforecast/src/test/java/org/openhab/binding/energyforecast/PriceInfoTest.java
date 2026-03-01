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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.energyforecast.internal.config.EnergyForecastConfiguration;
import org.openhab.binding.energyforecast.internal.dto.PriceInfo;
import org.openhab.core.i18n.TimeZoneProvider;
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

    @Test
    void test() {
        CurrencyUnits.addUnit(CurrencyUnits.createCurrency("EUR", "Euro"));
        VolatileStorage<String> storage = new VolatileStorage<>();
        PriceInfo priceInfo = new PriceInfo(new EnergyForecastConfiguration(), storage, tzp);

        priceInfo.newPriceSeries(readFile("src/test/resources/2026-02-26-response.json"));
        Map<String, TimeSeries> timeSeriesMap = priceInfo.getTimeSeries();
        System.out.println(timeSeriesMap);
        TimeSeries maeSeries = timeSeriesMap.get(CHANNEL_METRIC_MAE);
        assertNotNull(maeSeries);
        assertEquals(0, maeSeries.size());

        priceInfo.newPriceSeries(readFile("src/test/resources/2026-02-27-response.json"));
        timeSeriesMap = priceInfo.getTimeSeries();
        System.out.println(timeSeriesMap);
        maeSeries = timeSeriesMap.get(CHANNEL_METRIC_MAE);
        assertNotNull(maeSeries);
        assertEquals(1, maeSeries.size());

        maeSeries.getStates().forEach(entry -> {
            System.out.println("MAE for " + entry.timestamp() + ": " + entry.state());
        });

        TimeSeries mapeSeries = timeSeriesMap.get(CHANNEL_METRIC_MAPE);
        assertNotNull(mapeSeries);
        assertEquals(1, mapeSeries.size());
        mapeSeries.getStates().forEach(entry -> {
            System.out.println("MAE for " + entry.timestamp() + ": " + entry.state());
        });
    }
}
