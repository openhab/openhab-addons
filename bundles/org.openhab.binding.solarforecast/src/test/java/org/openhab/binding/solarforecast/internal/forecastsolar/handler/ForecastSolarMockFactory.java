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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarforecast.CallbackMock;
import org.openhab.binding.solarforecast.FileReader;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * The {@link ForecastSolarMockFactory} creates mock handler for solar.forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarMockFactory {

    public static ForecastSolarBridgeMock createBridgeHandler() {
        BridgeImpl forecastSolarBridge = new BridgeImpl(SolarForecastBindingConstants.FORECAST_SOLAR_SITE, "bridge");
        ForecastSolarBridgeMock forecastSolarSite = new ForecastSolarBridgeMock(forecastSolarBridge,
                PointType.valueOf("1,2"));
        forecastSolarBridge.setHandler(forecastSolarSite);
        CallbackMock forecastSolarSiteCallback = new CallbackMock();
        forecastSolarSiteCallback.setBridge(forecastSolarBridge);
        forecastSolarSite.setCallback(forecastSolarSiteCallback);
        forecastSolarSite.initialize();
        return forecastSolarSite;
    }

    public static ForecastSolarPlaneMock createPlaneHandler(ForecastSolarBridgeHandler bridgeHandler, String name,
            String forecastFile) {
        String contentOne = FileReader.readFileInString(forecastFile);
        ForecastSolarObject forecastSolarObject = new ForecastSolarObject(name + "-forecast", contentOne,
                Instant.now().plus(1, ChronoUnit.DAYS));
        ThingImpl forecastSolarPlaneThing = new ThingImpl(SolarForecastBindingConstants.FORECAST_SOLAR_PLANE,
                new ThingUID("test", name));
        forecastSolarPlaneThing.setBridgeUID(new ThingUID("solarforecast", "fs-site"));
        CallbackMock forecastSolarPlaneCallback = new CallbackMock();
        forecastSolarPlaneCallback.setBridge(bridgeHandler.getThing());
        ForecastSolarPlaneMock forecastSolarPlane = new ForecastSolarPlaneMock(forecastSolarPlaneThing,
                forecastSolarPlaneCallback);
        forecastSolarPlane.setCallback(forecastSolarPlaneCallback);
        forecastSolarPlane.updateConfiguration(getDefaultPlaneConfig());
        forecastSolarPlane.updateForecast(forecastSolarObject);
        forecastSolarPlane.initialize();
        forecastSolarPlaneCallback.waitForStatus(ThingStatus.ONLINE);
        return forecastSolarPlane;
    }

    private static Configuration getDefaultPlaneConfig() {
        Configuration config = new Configuration();
        config.put("declination", "90");
        config.put("azimuth", "90");
        config.put("kwp", "90");
        return config;
    }
}
