/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.km200.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link km200Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200BindingConstants {

    public static final String BINDING_ID = "km200";
    public static final String CONFIG_DESCRIPTION_URI_CHANNEL = "channel-type:km200:config";
    public static final String CONFIG_DESCRIPTION_URI_THING = "thing-type:km200:config";

    // Bridge UID
    public static final ThingTypeUID THING_TYPE_KMDEVICE = new ThingTypeUID(BINDING_ID, "kmdevice");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_DHW_CIRCUIT = new ThingTypeUID(BINDING_ID, "dhwCircuit");
    public static final ThingTypeUID THING_TYPE_HEATING_CIRCUIT = new ThingTypeUID(BINDING_ID, "heatingCircuit");
    public static final ThingTypeUID THING_TYPE_SOLAR_CIRCUIT = new ThingTypeUID(BINDING_ID, "solarCircuit");
    public static final ThingTypeUID THING_TYPE_HEAT_SOURCE = new ThingTypeUID(BINDING_ID, "heatSource");
    public static final ThingTypeUID THING_TYPE_SYSTEM = new ThingTypeUID(BINDING_ID, "system");
    public static final ThingTypeUID THING_TYPE_SYSTEM_APPLIANCE = new ThingTypeUID(BINDING_ID, "appliance");
    public static final ThingTypeUID THING_TYPE_SYSTEM_HOLIDAYMODES = new ThingTypeUID(BINDING_ID, "holidayMode");
    public static final ThingTypeUID THING_TYPE_SYSTEM_SENSOR = new ThingTypeUID(BINDING_ID, "sensor");
    public static final ThingTypeUID THING_TYPE_NOTIFICATION = new ThingTypeUID(BINDING_ID, "notification");
    public static final ThingTypeUID THING_TYPE_SWITCH_PROGRAM = new ThingTypeUID(BINDING_ID, "switchProgram");
    public static final ThingTypeUID THING_TYPE_SYSTEMSTATES = new ThingTypeUID(BINDING_ID, "systemStates");
    // Other constants

    public static final String SWITCH_PROGRAM_REPLACEMENT = "__current__";
    public static final String SWITCH_PROGRAM_PATH_NAME = "switchPrograms";
    public static final String SWITCH_PROGRAM_CURRENT_PATH_NAME = "activeSwitchProgram";
    public static final String SWITCH_PROGRAM_POSITIVE = "activeSwitchProgramPos";
    public static final String SWITCH_PROGRAM_NEGATIVE = "activeSwitchProgramNeg";

}
