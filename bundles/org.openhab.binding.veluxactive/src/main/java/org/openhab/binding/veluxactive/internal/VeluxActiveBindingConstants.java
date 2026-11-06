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
package org.openhab.binding.veluxactive.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Constants for the VELUX ACTIVE binding.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class VeluxActiveBindingConstants {
    public static final String BINDING_ID = "veluxactive";

    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_INDOOR_SENSOR = new ThingTypeUID(BINDING_ID, "indoor-sensor");
    public static final ThingTypeUID THING_TYPE_DEPARTURE_SWITCH = new ThingTypeUID(BINDING_ID, "departure-switch");
    public static final ThingTypeUID THING_TYPE_OPENING = new ThingTypeUID(BINDING_ID, "opening");

    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_CO2 = "co2";
    public static final String CHANNEL_ILLUMINANCE = "illuminance";
    public static final String CHANNEL_BATTERY = "battery-level";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";
    public static final String CHANNEL_SIGNAL = "signal-strength";
    public static final String CHANNEL_LOCKED = "locked";
    public static final String CHANNEL_LAST_SEEN = "last-seen";

    public static final String PROPERTY_VELUX_TYPE = "veluxType";

    public static final String TOKEN_URL = "https://api.netatmo.com/oauth2/token";
    public static final String SCOPE = "velux_scopes";
    public static final String APP_VERSION = "791302006";
    public static final String CLIENT_ID = "5931426da127d981e76bdd3f";
    public static final String CLIENT_SECRET = "6ae2d89d15e767ae5c56b456b452d319";
}
