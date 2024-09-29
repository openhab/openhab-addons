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
package org.openhab.binding.lametrictime.internal.api.dto;

import org.openhab.binding.lametrictime.internal.api.local.dto.UpdateAction;

/**
 * Implementation class for the WeatherApp.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class WeatherApp extends CoreApplication {
    private static final String NAME = "com.lametric.weather";

    private static final String ACTION_FORECAST = "weather.forecast";

    public WeatherApp() {
        super(NAME);
    }

    public CoreAction forecast() {
        return new CoreAction(this, new UpdateAction().withId(ACTION_FORECAST));
    }
}
