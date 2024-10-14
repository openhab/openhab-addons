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
package org.openhab.binding.broadlink.internal;

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
    public static final String BINDING_ID = "broadlink";
    public static final ThingTypeUID THING_TYPE_RM_PRO = new ThingTypeUID(BINDING_ID, "rm-pro");
    public static final ThingTypeUID THING_TYPE_RM3 = new ThingTypeUID(BINDING_ID, "rm3");
    public static final ThingTypeUID THING_TYPE_RM3Q = new ThingTypeUID(BINDING_ID, "rm3-q");
    public static final ThingTypeUID THING_TYPE_RM4_MINI = new ThingTypeUID(BINDING_ID, "rm4-mini");
    public static final ThingTypeUID THING_TYPE_RM4_PRO = new ThingTypeUID(BINDING_ID, "rm4-pro");
    public static final ThingTypeUID THING_TYPE_A1 = new ThingTypeUID(BINDING_ID, "a1");
    public static final ThingTypeUID THING_TYPE_MP1 = new ThingTypeUID(BINDING_ID, "mp1");
    public static final ThingTypeUID THING_TYPE_MP1_1K3S2U = new ThingTypeUID(BINDING_ID, "mp1-1k3s2u");
    public static final ThingTypeUID THING_TYPE_MP2 = new ThingTypeUID(BINDING_ID, "mp2");
    public static final ThingTypeUID THING_TYPE_SP1 = new ThingTypeUID(BINDING_ID, "sp1");
    public static final ThingTypeUID THING_TYPE_SP2 = new ThingTypeUID(BINDING_ID, "sp2");
    public static final ThingTypeUID THING_TYPE_SP2S = new ThingTypeUID(BINDING_ID, "sp2-s");
    public static final ThingTypeUID THING_TYPE_SP3 = new ThingTypeUID(BINDING_ID, "sp3");
    public static final ThingTypeUID THING_TYPE_SP3S = new ThingTypeUID(BINDING_ID, "sp3-s");

    public static final String RM_PRO = "Broadlink RM pro / pro+ / plus";
    public static final String RM3 = "Broadlink RM3";
    public static final String RM3Q = "Broadlink RM3 v11057";
    public static final String RM4_MINI = "Broadlink RM4 Mini";
    public static final String RM4_PRO = "Broadlink RM4 Pro";
    public static final String A1 = "Broadlink A1";
    public static final String MP1 = "Broadlink MP1";
    public static final String MP1_1K3S2U = "Broadlink MP1 1K3S2U";
    public static final String MP2 = "Broadlink MP2";

    public static final String SP1 = "Broadlink SP1";
    public static final String SP2 = "Broadlink SP2";
    public static final String SP2S = "Broadlink SP2-s";
    public static final String SP3 = "Broadlink SP3";
    public static final String SP3S = "Broadlink SP3-s";

    public static final String BROADLINK_AUTH_KEY = "097628343fe99e23765c1513accf8b02";
    public static final String BROADLINK_IV = "562e17996d093d28ddb3ba695a2e6f58";

    public static final String COMMAND_CHANNEL = "command";
    public static final String LEARNING_CONTROL_CHANNEL = "learning-control";
    public static final String RF_COMMAND_CHANNEL = "rf-command";
    public static final String RF_LEARNING_CONTROL_CHANNEL = "learning-rf-control";
    public static final String LEARNING_CONTROL_COMMAND_LEARN = "LEARN";
    public static final String LEARNING_CONTROL_COMMAND_CHECK = "CHECK";
    public static final String LEARNING_CONTROL_COMMAND_MODIFY = "MODIFY";
    public static final String LEARNING_CONTROL_COMMAND_DELETE = "DELETE";
    public static final String TEMPERATURE_CHANNEL = "temperature";
    public static final String HUMIDITY_CHANNEL = "humidity";
    public static final String LIGHT_CHANNEL = "light";
    public static final String AIR_CHANNEL = "air";
    public static final String NOISE_CHANNEL = "noise";
    public static final String POWER_CONSUMPTION_CHANNEL = "power-consumption";

    public static final String COMMAND_POWER_ON = "power-on";
    public static final String COMMAND_NIGHTLIGHT = "night-light";
    public static final String COMMAND_POWER_ON_S1 = "s1power-on";
    public static final String COMMAND_POWER_ON_S2 = "s2power-on";
    public static final String COMMAND_POWER_ON_S3 = "s3power-on";
    public static final String COMMAND_POWER_ON_S4 = "s4power-on";
    public static final String COMMAND_POWER_ON_USB = "power-on-usb";

    public static final String IR_MAP_NAME = "broadlink_ir";
    public static final String RF_MAP_NAME = "broadlink_rf";

    /**
     * Enum type to make a distinction between IR and RF codes being managed by a device
     */
    public static enum CodeType {
        IR,
        RF
    };

    public static final Unit<Temperature> BROADLINK_TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Dimensionless> BROADLINK_HUMIDITY_UNIT = Units.PERCENT;
    public static final Unit<Power> BROADLINK_POWER_CONSUMPTION_UNIT = Units.WATT;

    public static final Map<ThingTypeUID, String> SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP = new HashMap<>();

    static {
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM_PRO, RM_PRO);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM3, RM3);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM3Q, RM3Q);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM4_MINI, RM4_MINI);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM4_PRO, RM4_PRO);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_A1, A1);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MP1, MP1);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MP1_1K3S2U, MP1_1K3S2U);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MP2, MP2);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP1, SP1);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP2, SP2);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP2S, SP2S);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP3, SP3);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP3S, SP3S);
    }
}
