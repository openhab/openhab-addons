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
package org.openhab.binding.mercedesme.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import javax.measure.quantity.Pressure;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
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

    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_COMB, THING_TYPE_HYBRID, THING_TYPE_BEV).collect(Collectors.toSet()));

    public static final String GROUP_VEHICLE = "vehicle";
    public static final String GROUP_DOORS = "doors";
    public static final String GROUP_LOCK = "lock";
    public static final String GROUP_WINDOWS = "windows";
    public static final String GROUP_HVAC = "hvac";
    public static final String GROUP_SERVICE = "service";
    public static final String GROUP_RANGE = "range";
    public static final String GROUP_CHARGE = "charge";
    public static final String GROUP_TRIP = "trip";
    public static final String GROUP_POSITION = "position";
    public static final String GROUP_TIRES = "tires";
    public static final String GROUP_COMMAND = "command";

    public static final String CALLBACK_ENDPOINT = "/mb-auth";
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
    public static final String GUID = "guid";
    public static final String PIN = "pin";
    public static final String MIME_PREFIX = "image/";

    public static final Unit<Length> KILOMETRE_UNIT = MetricPrefix.KILO(SIUnits.METRE);
    public static final Unit<Power> KILOWATT_UNIT = MetricPrefix.KILO(Units.WATT);
    public static final Unit<Energy> KILOWATT_HOUR_UNIT = MetricPrefix.KILO(Units.WATT_HOUR);
    public static final Unit<Pressure> KPA_UNIT = MetricPrefix.KILO(SIUnits.PASCAL);

    public static final String LOGIN_APP_ID = "01398c1c-dc45-4b42-882b-9f5ba9f175f1";
    public static final String LOGIN_APP_ID_EU = "01398c1c-dc45-4b42-882b-9f5ba9f175f1";
    public static final String LOGIN_APP_ID_CN = "3f36efb1-f84b-4402-b5a2-68a118fec33e";
    public static final String LOGIN_BASE_URI = "https://id.mercedes-benz.com";
    public static final String LOGIN_BASE_URI_CN = "https://ciam-1.mercedes-benz.com.cn";
    public static final String LOGIN_BASE_URI_NA = "https://id.mercedes-benz.com";
    public static final String LOGIN_BASE_URI_PA = "https://id.mercedes-benz.com";
    public static final String PSAG_BASE_URI = "https://psag.query.api.dvb.corpinter.net";
    public static final String PSAG_BASE_URI_CN = "https://psag.query.api.dvb.corpinter.net.cn";
    public static final String RCP_BASE_URI = "https://rcp-rs.query.api.dvb.corpinter.net";
    public static final String RCP_BASE_URI_CN = "https://rcp-rs.query.api.dvb.corpinter.net.cn";
    public static final String REST_API_BASE = "https://bff.emea-prod.mobilesdk.mercedes-benz.com";
    public static final String REST_API_BASE_CN = "https://bff.cn-prod.mobilesdk.mercedes-benz.com";
    public static final String REST_API_BASE_NA = "https://bff.amap-prod.mobilesdk.mercedes-benz.com";
    public static final String REST_API_BASE_PA = "https://bff.amap-prod.mobilesdk.mercedes-benz.com";
    public static final String WEBSOCKET_API_BASE = "wss://websocket.emea-prod.mobilesdk.mercedes-benz.com/ws";
    public static final String WEBSOCKET_API_BASE_NA = "wss://websocket.amap-prod.mobilesdk.mercedes-benz.com/ws";
    public static final String WEBSOCKET_API_BASE_PA = "wss://websocket.amap-prod.mobilesdk.mercedes-benz.com/ws";
    public static final String WEBSOCKET_API_BASE_CN = "wss://websocket.cn-prod.mobilesdk.mercedes-benz.com/ws";
    public static final String WEBSOCKET_USER_AGENT = "MyCar/1.30.1 (com.daimler.ris.mercedesme.ece.ios; build:1819; iOS 16.5.0) Alamofire/5.4.0";
    public static final String WEBSOCKET_USER_AGENT_CN = "MyStarCN/1.27.0 (com.daimler.ris.mercedesme.cn.ios; build:1758; iOS 16.3.1) Alamofire/5.4.0";
    public static final String WEBSOCKET_USER_AGENT_PA = "mycar-store-ap v1.27.0, android 8.0.0, SDK 2.84.3";

    public static final String RIS_APPLICATION_VERSION_NA = "3.30.1";
    public static final String RIS_APPLICATION_VERSION_CN = "1.27.0 (1758)";
    public static final String RIS_APPLICATION_VERSION_PA = "1.30.1";
    public static final String RIS_APPLICATION_VERSION = "1.30.1";
    public static final String RIS_SDK_VERSION = "2.91.1";
    public static final String RIS_SDK_VERSION_CN = "2.84.0";
    public static final String RIS_OS_VERSION = "16.5";
    public static final String RIS_OS_NAME = "ios";
    public static final String X_APPLICATIONNAME = "mycar-store-ece";
    public static final String X_APPLICATIONNAME_ECE = "mycar-store-ece";
    public static final String X_APPLICATIONNAME_CN = "mycar-store-cn";
    public static final String X_APPLICATIONNAME_US = "mycar-store-us";
    public static final String X_APPLICATIONNAME_AP = "mycar-store-ap";

    public static final String REGION_EUROPE = "EU";
    public static final String REGION_NORAM = "NA";
    public static final String REGION_APAC = "AP";
    public static final String REGION_CHINA = "CN";

    public static final String SCOPE = "openid email phone profile offline_access ciam-uid";

    public static final String MAX_SOC_KEY = "maxsoc";
    public static final String AUTOUNLOCK_KEY = "autolock";

    public static final String CHANNEL_MILEAGE = "mileage";
    public static final String CHANNEL_TEMPERATURE = "temperature";
}
