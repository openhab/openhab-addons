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
package org.openhab.binding.onecta.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OnectaBridgeConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaBridgeConstants {

    private static final String BINDING_ID = "onecta";
    public static final String BRIDGE = "account";
    // List of all Device Types
    public static final String DEVICE_TYPE_GATEWAY = "gateway";
    public static final String DEVICE_TYPE_CLIMATECONTROL = "climate-control";
    public static final String DEVICE_TYPE_WATERTANK = "domestic-hot-water-tank";
    public static final String DEVICE_TYPE_INDOORUNIT = "indoor-unit";

    // List of config parameters
    public static final String CONFIG_PAR_REFRESHINTERVAL = "refreshInterval";
    public static final String CONFIG_PAR_UNITID = "unitID";

    // List of all Bridge Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, BRIDGE);

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CLIMATECONTROL = new ThingTypeUID(BINDING_ID,
            DEVICE_TYPE_CLIMATECONTROL);
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, DEVICE_TYPE_GATEWAY);
    public static final ThingTypeUID THING_TYPE_WATERTANK = new ThingTypeUID(BINDING_ID, DEVICE_TYPE_WATERTANK);
    public static final ThingTypeUID THING_TYPE_INDOORUNIT = new ThingTypeUID(BINDING_ID, DEVICE_TYPE_INDOORUNIT);

    // The supported thing types.
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE, THING_TYPE_CLIMATECONTROL,
            THING_TYPE_WATERTANK, THING_TYPE_GATEWAY, THING_TYPE_INDOORUNIT);

    public static final String THIRD_PARTY_ENDPOINTS_BASENAME = "https://idp.onecta.daikineurope.com/v1/oidc";
    public static final String OAUTH2_SERVICE_HANDLE = BINDING_ID + ":" + BRIDGE;
}
