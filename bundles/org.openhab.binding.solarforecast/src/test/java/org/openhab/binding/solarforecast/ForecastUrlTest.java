/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarBridgeMock;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarMockFactory;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarPlaneMock;
import org.openhab.core.config.core.Configuration;

/**
 * The {@link ForecastUrlTest} test urls for different scenarios
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ForecastUrlTest {

    @Test
    void testBaseUrl() {
        ForecastSolarBridgeMock fsBridgeHandler = ForecastSolarMockFactory.createBridgeHandler();
        assertEquals("https://api.forecast.solar/estimate/1/2/", fsBridgeHandler.getBaseUrl(), "Base URL");

        Configuration config = new Configuration();
        config.put("apiKey", "xyz");
        fsBridgeHandler.updateConfiguration(config);
        fsBridgeHandler.dispose();
        fsBridgeHandler.initialize();
        assertEquals("https://api.forecast.solar/xyz/estimate/1/2/", fsBridgeHandler.getBaseUrl(), "Base URL");

        config.put("location", "1.234,9.876");
        fsBridgeHandler.updateConfiguration(config);
        fsBridgeHandler.dispose();
        fsBridgeHandler.initialize();
        assertEquals("https://api.forecast.solar/xyz/estimate/1.234/9.876/", fsBridgeHandler.getBaseUrl(), "Base URL");
    }

    @Test
    void testFullUrl() {
        ForecastSolarBridgeMock fsBridgeHandler = ForecastSolarMockFactory.createBridgeHandler();
        Configuration siteConfig = new Configuration();
        siteConfig.put("location", "1.234,9.876");
        siteConfig.put("apiKey", "xyz");
        fsBridgeHandler.updateConfiguration(siteConfig);
        fsBridgeHandler.dispose();
        fsBridgeHandler.initialize();

        ForecastSolarPlaneMock fsPlaneHandler1 = ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane1",
                "src/test/resources/forecastsolar/result.json");
        Configuration planeConfig = new Configuration();
        planeConfig.put("declination", "45");
        planeConfig.put("azimuth", "-10");
        planeConfig.put("kwp", "5.5");
        planeConfig.put("dampAM", "0.5");
        planeConfig.put("dampPM", "0.3");
        fsPlaneHandler1.updateConfiguration(planeConfig);
        fsPlaneHandler1.dispose();
        fsPlaneHandler1.initialize();
        assertEquals("https://api.forecast.solar/xyz/estimate/1.234/9.876/45/-10/5.5?damping=0.5,0.3&full=1",
                fsPlaneHandler1.getURL(), "Full URL");

        siteConfig.put("inverterKwp", "0.8");
        fsBridgeHandler.updateConfiguration(siteConfig);
        fsBridgeHandler.dispose();
        fsBridgeHandler.initialize();
        assertEquals(
                "https://api.forecast.solar/xyz/estimate/1.234/9.876/45/-10/5.5?damping=0.5,0.3&inverter=0.8&full=1",
                fsPlaneHandler1.getURL(), "Full URL");

        planeConfig.put("horizon", "0,0,0,0,0,0,10,20,20,20,20,20");
        fsPlaneHandler1.updateConfiguration(planeConfig);
        fsPlaneHandler1.dispose();
        fsPlaneHandler1.initialize();
        assertEquals(
                "https://api.forecast.solar/xyz/estimate/1.234/9.876/45/-10/5.5?damping=0.5,0.3&horizon=0,0,0,0,0,0,10,20,20,20,20,20&inverter=0.8&full=1",
                fsPlaneHandler1.getURL(), "Full URL");
    }
}
