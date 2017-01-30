/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link km200Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200BindingConstants {

    public static final String BINDING_ID = "km200";

    // Bridge UID
    public final static ThingTypeUID THING_TYPE_KMDEVICE = new ThingTypeUID(BINDING_ID, "kmdevice");

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public final static ThingTypeUID THING_TYPE_DHW_CIRCUIT = new ThingTypeUID(BINDING_ID, "dhwCircuit");
    public final static ThingTypeUID THING_TYPE_HEATING_CIRCUIT = new ThingTypeUID(BINDING_ID, "heatingCircuit");
    public final static ThingTypeUID THING_TYPE_SOLAR_CIRCUIT = new ThingTypeUID(BINDING_ID, "solarCircuit");
    public final static ThingTypeUID THING_TYPE_HEAT_SOURCE = new ThingTypeUID(BINDING_ID, "heatSource");
    public final static ThingTypeUID THING_TYPE_SYSTEM_APPLIANCE = new ThingTypeUID(BINDING_ID, "appliance");
    public final static ThingTypeUID THING_TYPE_SYSTEM_SENSOR = new ThingTypeUID(BINDING_ID, "sensor");
    public final static ThingTypeUID THING_TYPE_NOTIFICATION = new ThingTypeUID(BINDING_ID, "notification");
    public final static ThingTypeUID THING_TYPE_SWITCH_PROGRAM = new ThingTypeUID(BINDING_ID, "switchProgram");

    // List of all Channel ids

    public final static String CHANNEL_STRING_VALUE = "stringValue";
    public final static String CHANNEL_FLOAT_VALUE = "floatValue";

    // Other constants

    public final static String SWITCH_PROGRAM_REPLACEMENT = "__current__";
    public final static String SWITCH_PROGRAM_PATH_NAME = "switchPrograms";
    public final static String SWITCH_PROGRAM_CURRENT_PATH_NAME = "activeSwitchProgram";
    public final static String SWITCH_PROGRAM_POSITIVE = "activeSwitchProgramPos";
    public final static String SWITCH_PROGRAM_NEGATIVE = "activeSwitchProgramNeg";

}
