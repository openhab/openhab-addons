/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.broadlink;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BroadlinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkBindingConstants {

    public static final ThingTypeUID THING_TYPE_RM = new ThingTypeUID("broadlink", "rm");
    public static final ThingTypeUID THING_TYPE_RM2 = new ThingTypeUID("broadlink", "rm2");
    public static final ThingTypeUID THING_TYPE_RM3 = new ThingTypeUID("broadlink", "rm3");
    public static final ThingTypeUID THING_TYPE_RM3Q = new ThingTypeUID("broadlink", "rm3q");
    public static final ThingTypeUID THING_TYPE_RM4 = new ThingTypeUID("broadlink", "rm4");
    public static final ThingTypeUID THING_TYPE_A1 = new ThingTypeUID("broadlink", "a1");
    public static final ThingTypeUID THING_TYPE_MP1 = new ThingTypeUID("broadlink", "mp1");
    public static final ThingTypeUID THING_TYPE_MP1_1K3S2U = new ThingTypeUID("broadlink", "mp1_1k3s2u");
    public static final ThingTypeUID THING_TYPE_MP2 = new ThingTypeUID("broadlink", "mp2");
    public static final ThingTypeUID THING_TYPE_SP1 = new ThingTypeUID("broadlink", "sp1");
    public static final ThingTypeUID THING_TYPE_SP2 = new ThingTypeUID("broadlink", "sp2");
    public static final ThingTypeUID THING_TYPE_SP3 = new ThingTypeUID("broadlink", "sp3");
    public static final ThingTypeUID THING_TYPE_PIR = new ThingTypeUID("broadlink", "s1p");
    public static final ThingTypeUID THING_TYPE_MAGNET = new ThingTypeUID("broadlink", "s1m");

    public static final String RM = "Broadlink RM";
    public static final String RM2 = "Broadlink RM2";
    public static final String RM3 = "Broadlink RM3";
    public static final String RM3Q = "Broadlink RM3 v11057";
    public static final String RM4 = "Broadlink RM4 / RM4 Mini / RM4 Pro";
    public static final String A1 = "Broadlink A1";
    public static final String MP1 = "Broadlink MP1";
    public static final String MP1_1K3S2U = "Broadlink MP1 1K3S2U";
    public static final String MP2 = "Broadlink MP2";

    public static final String SP1 = "SP1";
    public static final String SP2 = "SP2";
    public static final String SP3 = "SP3";
    public static final String S1P = "Smart One PIR Sensor";
    public static final String S1M = "Smart One Magnet Sensor";

    public static final String BROADLINK_AUTH_KEY = "097628343fe99e23765c1513accf8b02";
    public static final String BROADLINK_IV = "562e17996d093d28ddb3ba695a2e6f58";

    public static final Map<ThingTypeUID, String> SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP = new HashMap<ThingTypeUID, String>();

    static {
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM, RM);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM2, RM2);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM3, RM3);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM3Q, RM3Q);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM4, RM4);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_A1, A1);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MP1, MP1);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MP1_1K3S2U, MP1_1K3S2U);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MP2, MP2);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP1, SP1);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP2, SP2);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP3, SP3);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_PIR, S1P);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MAGNET, S1M);
    }
}
