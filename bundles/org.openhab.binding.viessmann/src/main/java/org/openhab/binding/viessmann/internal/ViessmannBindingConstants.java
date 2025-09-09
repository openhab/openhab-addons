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
package org.openhab.binding.viessmann.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.viessmann.internal.util.ViessmannUtil;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ViessmannBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class ViessmannBindingConstants {
    public static final String BINDING_ID = "viessmann";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPE_UIDS = Set.of(THING_TYPE_DEVICE);

    public static final String COUNT_API_CALLS = "countApiCalls";

    // References for needed API identifiers
    public static final String VIESSMANN_HOST = "api.viessmann-climatesolutions.com";
    public static final String IAM_HOST = "iam.viessmann-climatesolutions.com";
    public static final String VIESSMANN_BASE_URL = "https://" + VIESSMANN_HOST + "/";
    public static final String IAM_BASE_URL = "https://" + IAM_HOST + "/";
    public static final String VIESSMANN_AUTHORIZE_URL = IAM_BASE_URL + "idp/v3/authorize";
    public static final String VIESSMANN_TOKEN_URL = IAM_BASE_URL + "idp/v3/token";
    public static final String VIESSMANN_SCOPE = "IoT%20User%20offline_access";

    public static final int REFRESH_TOKEN_EXPIRE = 15552000;

    public static final int API_TIMEOUT_MS = 20000;
    public static final String PROPERTY_ID = "deviceId";
    public static final String INSTALLATION_ID = "installationId";
    public static final String GATEWAY_SERIAL = "gatewaySerial";
    public static final String REPRESENTATION_ID = "representationId";

    public static final Map<String, String> FEATURES_MAP = ViessmannUtil.readProperties(ViessmannBindingConstants.class,
            "features.properties");

    public static final Map<String, String> FEATURE_DESCRIPTION_MAP = ViessmannUtil
            .readProperties(ViessmannBindingConstants.class, "featuresDescription.properties");

    public static final Map<String, String> UNIT_MAP = Map.of( //
            "celsius", SIUnits.CELSIUS.getSymbol(), //
            "kelvin", Units.KELVIN.toString(), //
            "kilowattHour", Units.KILOWATT_HOUR.toString(), //
            "percent", Units.PERCENT.toString(), //
            "minute", Units.MINUTE.toString(), //
            "hour", Units.HOUR.toString(), //
            "hours", Units.HOUR.toString(), //
            "liter", Units.LITRE.toString(), //
            "liter/minute", Units.LITRE_PER_MINUTE.toString(), //
            "cubicMeter", SIUnits.CUBIC_METRE.toString());

    public static final Map<String, String> SUB_CHANNEL_TYPE_MAP = Map.of( //
            "cubicMeter", "type-volume", //
            "kilowattHour", "type-energy");

    public static final Map<String, String> MODES_MAP = ViessmannUtil.readProperties(ViessmannBindingConstants.class,
            "modes.properties");

    public static final String CHANNEL_RUN_QUERY_ONCE = "runQueryOnce";
    public static final String CHANNEL_RUN_ERROR_QUERY_ONCE = "runErrorQueryOnce";

    public static final List<String> PROPERTIES_URIS = List.of( //
            "setNameUri", //
            "setCurveUri", //
            "setScheduleUri", //
            "setModeUri", //
            "setTemperatureUri", //
            "temperatureUri", //
            "activateUri", //
            "deactivateUri", //
            "changeEndDateUri", //
            "scheduleUri", //
            "unscheduleUri", //
            "setMinUri", //
            "setMaxUri", //
            "setHysteresisUri");
}
