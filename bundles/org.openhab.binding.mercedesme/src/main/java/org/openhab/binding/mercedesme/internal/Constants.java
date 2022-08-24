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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Constants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
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

    public static final String GROUP_RANGE = "range";
    public static final String GROUP_DOORS = "doors";
    public static final String GROUP_WINDOWS = "windows";
    public static final String GROUP_LOCK = "lock";
    public static final String GROUP_LIGHTS = "lights";
    public static final String GROUP_LOCATION = "location";
    public static final String GROUP_IMAGE = "image";

    public static final String MB_AUTH_URL = "https://id.mercedes-benz.com/as/authorization.oauth2";
    public static final String MB_TOKEN_URL = "https://id.mercedes-benz.com/as/token.oauth2";
    public static final String CALLBACK_ENDPOINT = "/mb-callback";
    public static final String OAUTH_CLIENT_NAME = "#byocar";

    // https://developer.mercedes-benz.com/products/electric_vehicle_status/docs
    public static final String SCOPE_EV = "mb:vehicle:mbdata:evstatus";
    // https://developer.mercedes-benz.com/products/fuel_status/docs
    public static final String SCOPE_FUEL = "mb:vehicle:mbdata:fuelstatus";
    // https://developer.mercedes-benz.com/products/pay_as_you_drive_insurance/docs
    public static final String SCOPE_ODO = "mb:vehicle:mbdata:payasyoudrive";
    // https://developer.mercedes-benz.com/products/vehicle_lock_status/docs
    public static final String SCOPE_LOCK = "mb:vehicle:mbdata:vehiclelock";
    // https://developer.mercedes-benz.com/products/vehicle_status/docs
    public static final String SCOPE_STATUS = "mb:vehicle:mbdata:vehiclestatus";
    public static final String SCOPE_OFFLINE = "offline_access";

    public static final String BASE_URL = "https://api.mercedes-benz.com/vehicledata/v2";
    public static final String ODO_URL = BASE_URL + "/vehicles/%s/containers/payasyoudrive";
    public static final String STATUS_URL = BASE_URL + "/vehicles/%s/containers/vehiclestatus";
    public static final String LOCK_URL = BASE_URL + "/vehicles/%s/containers/vehiclelockstatus";
    public static final String FUEL_URL = BASE_URL + "/vehicles/%s/containers/fuelstatus";
    public static final String EV_URL = BASE_URL + "/vehicles/%s/containers/electricvehicle";

    // https://developer.mercedes-benz.com/content-page/api_migration_guide
    public static final String IMAGE_BASE_URL = "https://api.mercedes-benz.com/vehicle_images/v2";
    public static final String IMAGE_EXTERIOR_RESOURCE_URL = IMAGE_BASE_URL + "/vehicles/%s";

    public static final String STATUS_TEXT_PREFIX = "@text/mercedesme.";
    public static final String STATUS_AUTH_NEEDED = ".status.authorization-needed";
    public static final String STATUS_IP_MISSING = ".status.ip-missing";
    public static final String STATUS_PORT_MISSING = ".status.port-missing";
    public static final String STATUS_CLIENT_ID_MISSING = ".status.client-id-missing";
    public static final String STATUS_CLIENT_SECRET_MISSING = ".status.client-secret-missing";
    public static final String STATUS_SERVER_RESTART = ".status.server-restart";
    public static final String STATUS_BRIDGE_MISSING = ".status.bridge-missing";
    public static final String STATUS_BRIDGE_ATHORIZATION = ".status.bridge-authoriziation";

    public static final String SPACE = " ";
    public static final String EMPTY = "";
    public static final String COLON = ":";
    public static final String NOT_SET = "not set";

    public static final String CODE = "code";
    public static final String MIME_PREFIX = "image/";

    public static final Unit<Length> KILOMETRE_UNIT = MetricPrefix.KILO(SIUnits.METRE);
}
