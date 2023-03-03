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
package org.openhab.binding.broadlink;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BroadlinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkBindingConstants {

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
    public static final ThingTypeUID THING_TYPE_SP2S = new ThingTypeUID("broadlink", "sp2s");
    public static final ThingTypeUID THING_TYPE_SP3 = new ThingTypeUID("broadlink", "sp3");
    public static final ThingTypeUID THING_TYPE_SP3S = new ThingTypeUID("broadlink", "sp3s");
    public static final ThingTypeUID THING_TYPE_PIR = new ThingTypeUID("broadlink", "s1p");
    public static final ThingTypeUID THING_TYPE_MAGNET = new ThingTypeUID("broadlink", "s1m");

    public static final String RM2 = "Broadlink RM2";
    public static final String RM3 = "Broadlink RM3";
    public static final String RM3Q = "Broadlink RM3 v11057";
    public static final String RM4 = "Broadlink RM4 / RM4 Mini / RM4 Pro";
    public static final String A1 = "Broadlink A1";
    public static final String MP1 = "Broadlink MP1";
    public static final String MP1_1K3S2U = "Broadlink MP1 1K3S2U";
    public static final String MP2 = "Broadlink MP2";

    public static final String SP1 = "Broadlink SP1";
    public static final String SP2 = "Broadlink SP2";
    public static final String SP2S = "Broadlink SP2s";
    public static final String SP3 = "Broadlink SP3";
    public static final String SP3S = "Broadlink SP3s";
    public static final String S1P = "Smart One PIR Sensor";
    public static final String S1M = "Smart One Magnet Sensor";

    public static final String BROADLINK_AUTH_KEY = "097628343fe99e23765c1513accf8b02";
    public static final String BROADLINK_IV = "562e17996d093d28ddb3ba695a2e6f58";

    public static final String COMMAND_CHANNEL = "command";
    public static final String LEARNING_CONTROL_CHANNEL = "learningControl";
    public static final String LEARNING_CONTROL_COMMAND_LEARN = "LEARN";
    public static final String LEARNING_CONTROL_COMMAND_CHECK = "CHECK";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_POWER_CONSUMPTION = "powerConsumption";

    public static final String COMMAND_POWER_ON = "powerOn";
    public static final String COMMAND_NIGHTLIGHT = "nightLight";

    public static final Unit<Temperature> BROADLINK_TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Dimensionless> BROADLINK_HUMIDITY_UNIT = Units.PERCENT;
    public static final Unit<Power> BROADLINK_POWER_CONSUMPTION_UNIT = Units.WATT;

    public static final Map<ThingTypeUID, String> SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP = new HashMap<>();

    static {
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
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP2S, SP2S);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP3, SP3);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP3S, SP3S);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_PIR, S1P);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MAGNET, S1M);
    }
}
