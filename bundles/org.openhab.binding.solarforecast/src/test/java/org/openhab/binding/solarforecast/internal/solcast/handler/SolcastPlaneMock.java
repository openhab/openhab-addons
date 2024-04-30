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
package org.openhab.binding.solarforecast.internal.solcast.handler;

import static org.mockito.Mockito.mock;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarforecast.FileReader;
import org.openhab.binding.solarforecast.TimeZP;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * The {@link SolcastPlaneMock} mocks Plane Handler for solcast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastPlaneMock extends SolcastPlaneHandler {
    Bridge bridge;

    // solarforecast:sc-site:bridge
    public SolcastPlaneMock(BridgeImpl b) {
        super(new ThingImpl(SolarForecastBindingConstants.SOLCAST_PLANE,
                new ThingUID("solarforecast", "sc-plane", "thing")), mock(HttpClient.class));
        bridge = b;
    }

    @Override
    public @Nullable Bridge getBridge() {
        return bridge;
    }

    @Override
    protected SolcastObject fetchData() {
        forecast.ifPresent(forecastObject -> {
            if (forecastObject.isExpired()) {
                String content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
                SolcastObject sco1 = new SolcastObject("sc-test", content, Instant.now().plusSeconds(3600),
                        new TimeZP());
                super.setForecast(sco1);
                // new forecast
            } else {
                super.updateChannels(forecastObject);
            }
        });
        return forecast.get();
    }
}
