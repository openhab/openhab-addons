/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarforecast.CallbackMock;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * The {@link ForecastSolarPlaneMock} mocks Plane Handler for solar.forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarPlaneMock extends ForecastSolarPlaneHandler {

    public ForecastSolarPlaneMock(ForecastSolarObject fso) {
        super(new ThingImpl(SolarForecastBindingConstants.FORECAST_SOLAR_PLANE, new ThingUID("test", "plane")),
                mock(HttpClient.class));
        super.setCallback(new CallbackMock());
        setLocation(PointType.valueOf("1.23,9.87"));
        super.setForecast(fso);
    }

    public void updateForecast(ForecastSolarObject fso) {
        super.setForecast(fso);
    }
}
