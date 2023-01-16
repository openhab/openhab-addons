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
package org.openhab.binding.hapero.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HaperoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
public class HaperoBindingConstants {
    public static final String BINDING_ID = "Hapero";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "haperoBridge");
    public static final ThingTypeUID THING_TYPE_FURNACE = new ThingTypeUID(BINDING_ID, "furnace");
    public static final ThingTypeUID THING_TYPE_BUFFER = new ThingTypeUID(BINDING_ID, "buffer");
    public static final ThingTypeUID THING_TYPE_BOILER = new ThingTypeUID(BINDING_ID, "boiler");
    public static final ThingTypeUID THING_TYPE_HEATING = new ThingTypeUID(BINDING_ID, "heatingCircuit");

    // List of all Group ids
    public static final String GROUP_TEMPERATURES = "temperatures";
    public static final String GROUP_STATUS = "status";
    public static final String GROUP_POWER = "power";
    public static final String GROUP_AIR = "air";

    // List of all Channel ids
    public static final String CHANNEL_COMBUSTIONTEMPERATURE = "temperatures#combustionTemp";
    public static final String CHANNEL_PELLETCHANNELTEMPERATURE = "temperatures#pelletChannelTemp";
    public static final String CHANNEL_BOILERTEMPERATURE = "temperatures#boilerTemp";
    public static final String CHANNEL_BOILERTEMPERATURESET = "temperatures#boilerSetTemp";
    public static final String CHANNEL_OUTSIDETEMPERATURE = "temperatures#outsideTemp";
    public static final String CHANNEL_FURNACESTATUS = "status#furnaceStatus";
    public static final String CHANNEL_BURNERSTATUS = "status#burnerStatus";
    public static final String CHANNEL_MATERIALSTATUS = "status#materialStatus";
    public static final String CHANNEL_AIRSTATUS = "status#airStatus";
    public static final String CHANNEL_GRATESTATUS = "status#grateStatus";
    public static final String CHANNEL_ERRORSTATUS = "status#errorStatus";
    public static final String CHANNEL_MULTIFUNCTIONMOTORMODE = "status#multifunctionMotorMode";
    public static final String CHANNEL_MULTIFUNCTIONMOTORSTATUS = "status#multifunctionMotorStatus";
    public static final String CHANNEL_FURNACEPOWER = "power#power";
    public static final String CHANNEL_FURNACEAIRFLOW = "air#airFlow";
    public static final String CHANNEL_FURNACEAIRFLOWSET = "air#airFlowSet";
    public static final String CHANNEL_FURNACEAIRPOWER = "air#airPower";
    public static final String CHANNEL_FURNACEAIRDRIVE = "air#airDrive";
    public static final String CHANNEL_FURNACEAIRO2 = "air#airO2";
    public static final String CHANNEL_TEMPERATURETOP = "temperatureTop";
    public static final String CHANNEL_TEMPERATUREBOTTOM = "temperatureBottom";
    public static final String CHANNEL_ONTEMPERATURE = "onTemperature";
    public static final String CHANNEL_OFFTEMPERATURE = "offTemperature";
    public static final String CHANNEL_PUMP = "pump";
    public static final String CHANNEL_SWITCHVALVE = "switchValve";
    public static final String CHANNEL_CHARGING = "charging";
    public static final String CHANNEL_FLOWTEMPERATURE = "flowTemperature";
    public static final String CHANNEL_FLOWTEMPERATURESET = "flowTemperatureSet";
    public static final String CHANNEL_CIRCUITMODE = "circuitMode";
    public static final String CHANNEL_CIRCUITSUBMODE = "circuitSubMode";
    public static final String CHANNEL_CIRCUITFAULT = "circuitFault";
    public static final String CHANNEL_ROOMTEMPERATURE = "roomTemperature";
    public static final String CHANNEL_ROOMTEMPERATURESET = "roomTemperatureSet";

    // Device identifiers received from the control system
    public static final String BUFFER_ID = "PU";
    public static final String BOILER_ID = "WW";
    public static final String HEATING_ID = "HK";
    public static final String FURNACE_ID = "SI";

    // Config parameter values
    public static final String CONFIG_ACCESS_FILESYSTEM = "file";
    public static final String CONFIG_ACCESS_FTP = "ftp";
    public static final String DATA_FILENAME = "Upload.hld";
}
