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
package org.openhab.binding.solarforecast.internal.solcast.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONArray;
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

    public static JSONArray getTodaysJson(JSONArray forecast) {
        return getTodaysValues(forecast);
    }

    public static JSONArray merge(JSONArray actuals, JSONArray forecast) {
        return mergeArrays(actuals, forecast);
    }
}
