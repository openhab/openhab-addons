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
package org.openhab.binding.pentair.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PentairBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairBindingConstants {

    public static final String BINDING_ID = "pentair";

    // List of Bridge Types
    public static final String IP_BRIDGE = "ip_bridge";
    public static final String SERIAL_BRIDGE = "serial_bridge";

    // List of all Device Types
    public static final String CONTROLLER = "controller";
    public static final String INTELLIFLO = "intelliflo";
    public static final String INTELLICHLOR = "intellichlor";
    public static final String INTELLICHEM = "intellichem";

    // List of all Bridge Thing Type UIDs
    public static final ThingTypeUID IP_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, IP_BRIDGE);
    public static final ThingTypeUID SERIAL_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, SERIAL_BRIDGE);

    // List of all Thing Type UIDs
    public static final ThingTypeUID INTELLIFLO_THING_TYPE = new ThingTypeUID(BINDING_ID, INTELLIFLO);
    public static final ThingTypeUID CONTROLLER_THING_TYPE = new ThingTypeUID(BINDING_ID, CONTROLLER);
    public static final ThingTypeUID INTELLICHLOR_THING_TYPE = new ThingTypeUID(BINDING_ID, INTELLICHLOR);
    public static final ThingTypeUID INTELLICHEM_THING_TYPE = new ThingTypeUID(BINDING_ID, INTELLICHEM);

    public static final String PARAMETER_ID = "id";

    // Controller Items
    public static final String PROPERTY_CONTROLLER_FIRMWAREVERSION = "firmwareVersion";
    public static final String PROPERTY_CONTROLLER_ID = "id";

    public static final String CONTROLLER_CONFIGSYNCTIME = "synctime";

    public static final String GROUP_CONTROLLER_STATUS = "status";

    public static final String CHANNEL_CONTROLLER_AIRTEMPERATURE = "airtemperature";
    public static final String CHANNEL_CONTROLLER_SOLARTEMPERATURE = "solartemperature";
    public static final String CHANNEL_CONTROLLER_LIGHTMODE = "lightmode";
    public static final String CHANNEL_CONTROLLER_SERVICEMODE = "servicemode";
    public static final String CHANNEL_CONTROLLER_SOLARON = "solaron";
    public static final String CHANNEL_CONTROLLER_HEATERON = "heateron";
    public static final String CHANNEL_CONTROLLER_HEATERDELAY = "heaterdelay";

    public static final String GROUP_CONTROLLER_POOLCIRCUIT = "pool";
    public static final String GROUP_CONTROLLER_SPACIRCUIT = "spa";
    public static final String GROUP_CONTROLLER_AUX1CIRCUIT = "aux1";
    public static final String GROUP_CONTROLLER_AUX2CIRCUIT = "aux2";
    public static final String GROUP_CONTROLLER_AUX3CIRCUIT = "aux3";
    public static final String GROUP_CONTROLLER_AUX4CIRCUIT = "aux4";
    public static final String GROUP_CONTROLLER_AUX5CIRCUIT = "aux5";
    public static final String GROUP_CONTROLLER_AUX6CIRCUIT = "aux6";
    public static final String GROUP_CONTROLLER_AUX7CIRCUIT = "aux7";
    public static final String GROUP_CONTROLLER_AUX8CIRCUIT = "aux8";

    public static final String CHANNEL_CONTROLLER_CIRCUITSWITCH = "switch";
    public static final String CHANNEL_CONTROLLER_CIRCUITNAME = "name";
    public static final String CHANNEL_CONTROLLER_CIRCUITFUNCTION = "function";

    public static final String GROUP_CONTROLLER_FEATURE1 = "feature1";
    public static final String GROUP_CONTROLLER_FEATURE2 = "feature2";
    public static final String GROUP_CONTROLLER_FEATURE3 = "feature3";
    public static final String GROUP_CONTROLLER_FEATURE4 = "feature4";
    public static final String GROUP_CONTROLLER_FEATURE5 = "feature5";
    public static final String GROUP_CONTROLLER_FEATURE6 = "feature6";
    public static final String GROUP_CONTROLLER_FEATURE7 = "feature7";
    public static final String GROUP_CONTROLLER_FEATURE8 = "feature8";

    // List of heat group and items
    public static final String GROUP_CONTROLLER_POOLHEAT = "poolheat";
    public static final String GROUP_CONTROLLER_SPAHEAT = "spaheat";

    public static final String CHANNEL_CONTROLLER_TEMPERATURE = "temperature";
    public static final String CHANNEL_CONTROLLER_SETPOINT = "setpoint";
    public static final String CHANNEL_CONTROLLER_HEATMODE = "heatmode";

    // List of schedule group and items
    public static final String GROUP_CONTROLLER_SCHEDULE = "schedule";

    public static final String CHANNEL_CONTROLLER_SCHEDULESAVE = "save";
    public static final String CHANNEL_CONTROLLER_SCHEDULESTRING = "schedule";
    public static final String CHANNEL_CONTROLLER_SCHEDULETYPE = "type";
    public static final String CHANNEL_CONTROLLER_SCHEDULECIRCUIT = "circuit";
    public static final String CHANNEL_CONTROLLER_SCHEDULEDAYS = "days";
    public static final String CHANNEL_CONTROLLER_SCHEDULESTART = "start";
    public static final String CHANNEL_CONTROLLER_SCHEDULEEND = "end";

    // List of Intellichlor channel ids
    public static final String CHANNEL_INTELLICHLOR_PROPERTYVERSION = "version";
    public static final String CHANNEL_INTELLICHLOR_PROPERTYMODEL = "model";

    public static final String CHANNEL_INTELLICHLOR_SALTOUTPUT = "saltOutput";
    public static final String CHANNEL_INTELLICHLOR_SALINITY = "salinity";
    public static final String CHANNEL_INTELLICHLOR_OK = "ok";
    public static final String CHANNEL_INTELLICHLOR_LOWFLOW = "lowFlow";
    public static final String CHANNEL_INTELLICHLOR_LOWSALT = "lowSalt";
    public static final String CHANNEL_INTELLICHLOR_VERYLOWSALT = "veryLowSalt";
    public static final String CHANNEL_INTELLICHLOR_HIGHCURRENT = "highCurrent";
    public static final String CHANNEL_INTELLICHLOR_CLEANCELL = "cleanCell";
    public static final String CHANNEL_INTELLICHLOR_LOWVOLTAGE = "lowVoltage";
    public static final String CHANNEL_INTELLICHLOR_LOWWATERTEMP = "lowWaterTemp";
    public static final String CHANNEL_INTELLICHLOR_COMMERROR = "commError";

    // IntelliChem Items

    public static final String PROPERTY_INTELLICHEM_FIRMWAREVERSION = "firmwareVersion";

    public static final String CHANNEL_INTELLICHEM_PHREADING = "phReading";
    public static final String CHANNEL_INTELLICHEM_ORPREADING = "orpReading";
    public static final String CHANNEL_INTELLICHEM_PHSETPOINT = "phSetPoint";
    public static final String CHANNEL_INTELLICHEM_ORPSETPOINT = "orpSetPoint";
    public static final String CHANNEL_INTELLICHEM_TANK1LEVEL = "tank1Level";
    public static final String CHANNEL_INTELLICHEM_TANK2LEVEL = "tank2Level";
    public static final String CHANNEL_INTELLICHEM_CALCIUMHARDNESS = "calciumHardness";
    public static final String CHANNEL_INTELLICHEM_CYAREADING = "cyaReading";
    public static final String CHANNEL_INTELLICHEM_ALKALINITY = "alkalinity";
    public static final String CHANNEL_INTELLICHEM_PHDOSERTYPE = "phDoserType";
    public static final String CHANNEL_INTELLICHEM_ORPDOSERTYPE = "orpDoserType";
    public static final String CHANNEL_INTELLICHEM_PHDOSERSTATUS = "phDoserStatus";
    public static final String CHANNEL_INTELLICHEM_ORPDOSERSTATUS = "orpDoserStatus";
    public static final String CHANNEL_INTELLICHEM_PHDOSETIME = "phDoseTime";
    public static final String CHANNEL_INTELLICHEM_ORPDOSETIME = "orpDoesTIme";
    public static final String CHANNEL_INTELLICHEM_LSI = "lsi";
    public static final String CHANNEL_INTELLICHEM_SALTLEVEL = "saltLevel";

    public static final String CHANNEL_INTELLICHEM_ALARMWATERFLOW = "alarmWaterFlow";
    public static final String CHANNEL_INTELLICHEM_ALARMPH = "alarmPh";
    public static final String CHANNEL_INTELLICHEM_ALARMORP = "alarmOrp";
    public static final String CHANNEL_INTELLICHEM_ALARMPHTANK = "alarmPhTank";
    public static final String CHANNEL_INTELLICHEM_ALARMORPTANK = "alarmOrpTank";
    public static final String CHANNEL_INTELLICHEM_ALARMPROBEFAULT = "alarmProbeFault";

    public static final String CHANNEL_INTELLICHEM_WARNINGPHLOCKOUT = "warningPhLockout";
    public static final String CHANNEL_INTELLICHEM_WARNINGPHDAILYLIMITREACHED = "warningPhDailyLimitReached";
    public static final String CHANNEL_INTELLICHEM_WARNINGORPDAILYLIMITREACHED = "warningOrpDailyLimitReached";
    public static final String CHANNEL_INTELLICHEM_WARNINGINVALIDSETUP = "warningInvalidSetup";
    public static final String CHANNEL_INTELLICHEM_WARNINGCHLORINATORCOMMERROR = "warningChlorinatorCommError";

    // List of all Intelliflo channel ids
    public static final String CHANNEL_INTELLIFLO_RUN = "run";
    public static final String CHANNEL_INTELLIFLO_POWER = "power";
    public static final String CHANNEL_INTELLIFLO_RPM = "rpm";
    public static final String INTELLIFLO_GPM = "gpm";
    public static final String INTELLIFLO_ERROR = "error";
    public static final String INTELLIFLO_STATUS1 = "status1";
    public static final String INTELLIFLO_STATUS2 = "status2";
    public static final String INTELLIFLO_TIMER = "timer";
    public static final String INTELLIFLO_RUNPROGRAM = "runProgram";

    public static final String DIAG = "diag";

    // Custom Properties
    public static final String PROPERTY_ADDRESS = "localhost";
    public static final Integer PROPERTY_PORT = 10000;
    public static final int DEFAULT_PENTAIR_ID = 34;
}
