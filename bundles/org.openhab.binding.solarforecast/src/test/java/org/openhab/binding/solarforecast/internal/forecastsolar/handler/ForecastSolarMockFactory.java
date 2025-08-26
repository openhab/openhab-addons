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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarforecast.CallbackMock;
import org.openhab.binding.solarforecast.FileReader;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.core.library.types.PointType;
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
        BridgeImpl bridge = new BridgeImpl(SolarForecastBindingConstants.FORECAST_SOLAR_SITE, "bridge");
        ForecastSolarBridgeMock fsbh = new ForecastSolarBridgeMock(bridge, PointType.valueOf("1,2"));
        bridge.setHandler(fsbh);
        CallbackMock cmSite = new CallbackMock();
        cmSite.setBridge(bridge);
        fsbh.setCallback(cmSite);
        fsbh.initialize();
        return fsbh;
    }

    public static ForecastSolarPlaneMock createPlaneHandler(ForecastSolarBridgeHandler bridgeHandler, String name,
            String forecastFile) {
        String contentOne = FileReader.readFileInString(forecastFile);
        ForecastSolarObject fso1One = new ForecastSolarObject("fs-test", contentOne,
                Instant.now().plus(1, ChronoUnit.DAYS));
        ThingImpl thingOne = new ThingImpl(SolarForecastBindingConstants.FORECAST_SOLAR_PLANE,
                new ThingUID("test", name));
        thingOne.setBridgeUID(new ThingUID("solarforecast", "fs-site"));
        CallbackMock cmPlane1 = new CallbackMock();
        cmPlane1.setBridge(bridgeHandler.getThing());
        ForecastSolarPlaneMock fsph1 = new ForecastSolarPlaneMock(thingOne, cmPlane1);
        fsph1.setCallback(cmPlane1);
        fsph1.updateForecast(fso1One);
        fsph1.initialize();
        cmPlane1.waitForOnline();
        return fsph1;
    }
}
