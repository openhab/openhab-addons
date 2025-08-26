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

import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarforecast.CallbackMock;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link ForecastSolarPlaneMock} mocks Plane Handler for solar.forecast
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Constructor correction
 */
@NonNullByDefault
public class ForecastSolarPlaneMock extends ForecastSolarPlaneHandler {

    public ForecastSolarPlaneMock(Thing thing, CallbackMock cm) {
        super(thing, mock(HttpClient.class));
        super.setCallback(cm);
    }

    public void updateForecast(ForecastSolarObject fso) {
        super.setForecast(fso);
    }

    @Override
    public @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }

    @Override
    public void updateConfiguration(Configuration config) {
        super.updateConfiguration(config);
    }

    public String getURL() {
        return super.buildUrl();
    }
}
