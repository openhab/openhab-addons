/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;

/**
 * The {@link Constants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Constants {
    public static final String BINDING_ID = "mercedesme";

    public static final String COMBUSTION = "combustion";
    public static final String HYBRID = "hybrid";
    public static final String BEV = "bev";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_COMB = new ThingTypeUID(BINDING_ID, COMBUSTION);
    public static final ThingTypeUID THING_TYPE_HYBRID = new ThingTypeUID(BINDING_ID, HYBRID);
    public static final ThingTypeUID THING_TYPE_BEV = new ThingTypeUID(BINDING_ID, BEV);

    public static String GROUP_RANGE = "range";
    public static String GROUP_DOORS = "doors";
    public static String GROUP_WINDOWS = "windows";
    public static String GROUP_LOCK = "lock";
    public static String GROUP_LIGHTS = "lights";
    public static String GROUP_LOCATION = "location";

    public static String MB_AUTH_URL = "https://id.mercedes-benz.com/as/authorization.oauth2";
    public static String MB_TOKEN_URL = "https://id.mercedes-benz.com/as/token.oauth2";
    public static String CALLBACK_ENDPOINT = "/mb-callback";
    public static String OAUTH_CLIENT_NAME = "#byocar";

    // https://developer.mercedes-benz.com/products/electric_vehicle_status/docs
    public static String SCOPE_EV = "mb:vehicle:mbdata:evstatus";
    // https://developer.mercedes-benz.com/products/fuel_status/docs
    public static String SCOPE_FUEL = "mb:vehicle:mbdata:fuelstatus";
    // https://developer.mercedes-benz.com/products/pay_as_you_drive_insurance/docs
    public static String SCOPE_ODO = "mb:vehicle:mbdata:payasyoudrive";
    // https://developer.mercedes-benz.com/products/vehicle_lock_status/docs
    public static String SCOPE_LOCK = "mb:vehicle:mbdata:vehiclelock";
    // https://developer.mercedes-benz.com/products/vehicle_status/docs
    public static String SCOPE_STATUS = "mb:vehicle:mbdata:vehiclestatus";
    public static String SCOPE_OFFLINE = "offline_access";

    public static String BASE_URL = "https://api.mercedes-benz.com/vehicledata/v2";
    public static String ODO_URL = BASE_URL + "/vehicles/%s/containers/payasyoudrive";
    public static String STATUS_URL = BASE_URL + "/vehicles/%s/containers/vehiclestatus";
    public static String LOCK_URL = BASE_URL + "/vehicles/%s/containers/vehiclelockstatus";
    public static String FUEL_URL = BASE_URL + "/vehicles/%s/containers/fuelstatus";
    public static String EV_URL = BASE_URL + "/vehicles/%s/containers/electricvehicle";

    public static String SPACE = " ";
    public static final String EMPTY = "";

    public static String HTTP = "http://";
    public static final String LOOPBACK_ADDRESS = "lo";
    public static final String COLON = ":";
    public static final @NonNull String NOT_SET = "not set";

    public static final String CODE = "code";

    public static final Gson GSON = new Gson();
    public static final Unit<Length> KILOMETRE_UNIT = MetricPrefix.KILO(SIUnits.METRE);
}
