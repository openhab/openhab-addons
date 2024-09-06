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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONObject;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Thing;

/**
 * The {@link SolcastPlaneMock} mocks Plane Handler for solcast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastPlaneMock extends SolcastPlaneHandler {

    public SolcastPlaneMock(Thing thing, HttpClient hc, Storage<String> storage) {
        super(thing, hc, storage);
    }

    public void updateConfig(Configuration config) {
        super.updateConfiguration(config);
    }

    public static JSONObject getTodaysJson(JSONObject forecast) {
        return getTodaysValues(forecast.toString());
    }
    /*
     * Bridge bridge;
     * 
     * // solarforecast:sc-site:bridge
     * public SolcastPlaneMock(BridgeImpl b) {
     * super(new ThingImpl(SolarForecastBindingConstants.SOLCAST_PLANE,
     * new ThingUID("solarforecast", "sc-plane", "thing")), mock(HttpClient.class), mock(Storage.class));
     * bridge = b;
     * }
     * 
     * @Override
     * public @Nullable Bridge getBridge() {
     * return bridge;
     * }
     * 
     * @Override
     * protected SolcastObject fetchData() {
     * forecastOptional.ifPresent(forecastObject -> {
     * if (forecastObject.isExpired()) {
     * String content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
     * SolcastObject sco1 = new SolcastObject("sc-test", new JSONObject(content),
     * Instant.now().plusSeconds(3600), new TimeZP(), mock(Storage.class));
     * super.setForecast(sco1);
     * // new forecast
     * } else {
     * super.updateChannels(forecastObject);
     * }
     * });
     * return forecastOptional.get();
     * }
     */
}
