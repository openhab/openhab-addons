/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AirQualityBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirQualityBindingConstants {

    private static final String BINDING_ID = "airquality";
    public static final String LOCAL = "local";

    // List of thing properties
    public static final String ATTRIBUTIONS = "Attributions";
    public static final String DISTANCE = "Distance";

    // List of all Channel groups id's
    public static final String AQI = "aqi";
    public static final String SENSITIVE = "sensitive-group";

    // List of all Channel id's
    public static final String INDEX = "index";
    public static final String VALUE = "value";
    public static final String ALERT_LEVEL = "alert-level";
    public static final String TEMPERATURE = "temperature";
    public static final String PRESSURE = "pressure";
    public static final String HUMIDITY = "humidity";
    public static final String DEW_POINT = "dew-point";
    public static final String WIND_SPEED = "wind-speed";
    public static final String TIMESTAMP = "timestamp";
    public static final String DOMINENT = "dominent";
    public static final String ICON = "icon";
    public static final String COLOR = "color";

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_STATION = new ThingTypeUID(BINDING_ID, "station");
    public static final ThingTypeUID BRIDGE_TYPE_API = new ThingTypeUID(BINDING_ID, "api");
}
