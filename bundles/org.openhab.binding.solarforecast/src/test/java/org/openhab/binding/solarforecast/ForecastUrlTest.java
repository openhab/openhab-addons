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
package org.openhab.binding.solarforecast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarBridgeMock;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarMockFactory;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarPlaneMock;
import org.openhab.binding.solarforecast.internal.utils.Utils;
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
        String url = fsBridgeHandler.getBaseUrl();
        assertEquals("https://api.forecast.solar/estimate/1/2/", url, "Base URL");
        assertEquals("https://api.forecast.solar/estimate/1/2/", Utils.redactUrlForLog(url), "Base URL");

        Configuration config = new Configuration();
        config.put("apiKey", "xyz");
        newSiteConfig(fsBridgeHandler, config);
        url = fsBridgeHandler.getBaseUrl();
        assertEquals("https://api.forecast.solar/xyz/estimate/1/2/", fsBridgeHandler.getBaseUrl(), "Base URL");
        assertEquals("https://api.forecast.solar/****/estimate/1/2/", Utils.redactUrlForLog(url), "Base URL");

        config.put("location", "1.234,9.876");
        newSiteConfig(fsBridgeHandler, config);
        url = fsBridgeHandler.getBaseUrl();
        assertEquals("https://api.forecast.solar/xyz/estimate/1.234/9.876/", fsBridgeHandler.getBaseUrl(), "Base URL");
        assertEquals("https://api.forecast.solar/****/estimate/1.234/9.876/", Utils.redactUrlForLog(url), "Base URL");
    }

    @Test
    void testFullUrl() {
        ForecastSolarBridgeMock fsBridgeHandler = ForecastSolarMockFactory.createBridgeHandler();

        Configuration siteConfig = new Configuration();
        siteConfig.put("location", "1.234,9.876");
        siteConfig.put("apiKey", "xyz");
        newSiteConfig(fsBridgeHandler, siteConfig);

        ForecastSolarPlaneMock fsPlaneHandler1 = ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane1",
                "src/test/resources/forecastsolar/result.json");
        Configuration planeConfig = new Configuration();
        planeConfig.put("declination", "45");
        planeConfig.put("azimuth", "-10");
        planeConfig.put("kwp", "5.5");
        planeConfig.put("dampAM", "0.5");
        planeConfig.put("dampPM", "0.3");
        newPlaneConfig(fsPlaneHandler1, planeConfig);
        assertEquals("https://api.forecast.solar/xyz/estimate/1.234/9.876/45/-10/5.5?damping=0.5,0.3&full=1",
                fsPlaneHandler1.getURL(), "Full URL");

        siteConfig.put("inverterKwp", "0.8");
        newSiteConfig(fsBridgeHandler, siteConfig);
        assertEquals(
                "https://api.forecast.solar/xyz/estimate/1.234/9.876/45/-10/5.5?damping=0.5,0.3&inverter=0.8&full=1",
                fsPlaneHandler1.getURL(), "Full URL");

        planeConfig.put("horizon", "0,0,0,0,0,0,10,20,20,20,20,20");
        newPlaneConfig(fsPlaneHandler1, planeConfig);
        assertEquals(
                "https://api.forecast.solar/xyz/estimate/1.234/9.876/45/-10/5.5?damping=0.5,0.3&horizon=0,0,0,0,0,0,10,20,20,20,20,20&inverter=0.8&full=1",
                fsPlaneHandler1.getURL(), "Full URL");
    }

    private void newSiteConfig(ForecastSolarBridgeMock handler, Configuration config) {
        handler.updateConfiguration(config);
        handler.dispose();
        handler.initialize();
        // no need to wait for states as URL is built during initialize
    }

    private void newPlaneConfig(ForecastSolarPlaneMock handler, Configuration config) {
        handler.updateConfiguration(config);
        handler.dispose();
        handler.initialize();
        // no need to wait for states as URL is built during initialize
    }
}
