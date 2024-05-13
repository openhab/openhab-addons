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
package org.openhab.binding.onecta.internal;

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
    public static final String DEVICE = "device";
    public static final String GATEWAY = "gateway";
    public static final String CLIMATECONTROL = "climateControl";
    public static final String WATERTANK = "domesticHotWaterTank";
    public static final String INDOORUNIT = "indoorUnit";
    public static final String CHANNEL_REFRESH_TOKEN = "refreshToken";
    public static final String CHANNEL_PASSWORD = "password";
    public static final String CHANNEL_USERID = "userId";
    public static final String CHANNEL_REFRESHINTERVAL = "refreshInterval";
    public static final String CHANNEL_LOGRAWDATA = "rawdataLogging";
    public static final String CHANNEL_STUBDATAFILE = "stubdataFile";
    public static final String CHANNEL_OPENHAB_HOST = "openhabHost";

    // List of all Bridge Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE);

    // List of all Thing Type UIDs
    public static final ThingTypeUID DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE);
    public static final ThingTypeUID GATEWAY_THING_TYPE = new ThingTypeUID(BINDING_ID, GATEWAY);
    public static final ThingTypeUID WATERTANK_THING_TYPE = new ThingTypeUID(BINDING_ID, WATERTANK);
    public static final ThingTypeUID INDOORUNIT_THING_TYPE = new ThingTypeUID(BINDING_ID, INDOORUNIT);
}
