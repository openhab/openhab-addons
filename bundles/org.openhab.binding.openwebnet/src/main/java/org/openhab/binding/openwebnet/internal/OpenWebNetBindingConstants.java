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
package org.openhab.binding.openwebnet.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OpenWebNetBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Massimo Valla - Initial contribution, updates
 * @author Gilberto Cocchi - Thermoregulation
 * @author Andrea Conte - Energy management, Thermoregulation
 * @author Giovanni Fabiani - Aux support
 */

@NonNullByDefault
public class OpenWebNetBindingConstants {

    public static final String BINDING_ID = "openwebnet";

    public static final int THING_STATE_REQ_TIMEOUT_SEC = 5;

    // #LIST OF Thing Type UIDs
    // generic device (used for not identified devices)
    public static final ThingTypeUID THING_TYPE_GENERIC_DEVICE = new ThingTypeUID(BINDING_ID, "generic_device");
    public static final String THING_LABEL_GENERIC_DEVICE = "GENERIC Device";
    // bridges
    public static final ThingTypeUID THING_TYPE_ZB_GATEWAY = new ThingTypeUID(BINDING_ID, "zb_gateway");
    public static final String THING_LABEL_ZB_GATEWAY = "Zigbee USB Gateway";
    public static final ThingTypeUID THING_TYPE_BUS_GATEWAY = new ThingTypeUID(BINDING_ID, "bus_gateway");
    public static final String THING_LABEL_BUS_GATEWAY = "BUS Gateway";
    // other thing types
    // BUS
    public static final ThingTypeUID THING_TYPE_BUS_ON_OFF_SWITCH = new ThingTypeUID(BINDING_ID, "bus_on_off_switch");
    public static final String THING_LABEL_BUS_ON_OFF_SWITCH = "Switch";
    public static final ThingTypeUID THING_TYPE_BUS_DIMMER = new ThingTypeUID(BINDING_ID, "bus_dimmer");
    public static final String THING_LABEL_BUS_DIMMER = "Dimmer";
    public static final ThingTypeUID THING_TYPE_BUS_AUTOMATION = new ThingTypeUID(BINDING_ID, "bus_automation");
    public static final String THING_LABEL_BUS_AUTOMATION = "Automation";
    public static final ThingTypeUID THING_TYPE_BUS_ENERGY_METER = new ThingTypeUID(BINDING_ID, "bus_energy_meter");
    public static final String THING_LABEL_BUS_ENERGY_METER = "Energy Meter";
    public static final ThingTypeUID THING_TYPE_BUS_THERMO_SENSOR = new ThingTypeUID(BINDING_ID, "bus_thermo_sensor");
    public static final String THING_LABEL_BUS_THERMO_SENSOR = "Thermo Sensor";
    public static final ThingTypeUID THING_TYPE_BUS_THERMO_ZONE = new ThingTypeUID(BINDING_ID, "bus_thermo_zone");
    public static final String THING_LABEL_BUS_THERMO_ZONE = "Thermo Zone";
    public static final ThingTypeUID THING_TYPE_BUS_THERMO_CU = new ThingTypeUID(BINDING_ID, "bus_thermo_cu");
    public static final String THING_LABEL_BUS_THERMO_CU = "Thermo Central Unit";
    public static final ThingTypeUID THING_TYPE_BUS_CEN_SCENARIO_CONTROL = new ThingTypeUID(BINDING_ID,
            "bus_cen_scenario_control");
    public static final String THING_LABEL_BUS_CEN_SCENARIO_CONTROL = "CEN Scenario Control";
    public static final ThingTypeUID THING_TYPE_BUS_DRY_CONTACT_IR = new ThingTypeUID(BINDING_ID, "bus_dry_contact_ir");
    public static final String THING_LABEL_BUS_DRY_CONTACT_IR = "Dry Contact/IR";
    public static final ThingTypeUID THING_TYPE_BUS_CENPLUS_SCENARIO_CONTROL = new ThingTypeUID(BINDING_ID,
            "bus_cenplus_scenario_control");
    public static final String THING_LABEL_BUS_CENPLUS_SCENARIO_CONTROL = "CEN+ Scenario Control";
    public static final ThingTypeUID THING_TYPE_BUS_SCENARIO = new ThingTypeUID(BINDING_ID, "bus_scenario_control");
    public static final String THING_LABEL_BUS_SCENARIO = "Scenario Control";
    public static final ThingTypeUID THING_TYPE_BUS_ALARM_SYSTEM = new ThingTypeUID(BINDING_ID, "bus_alarm_system");
    public static final String THING_LABEL_BUS_ALARM_SYSTEM = "Alarm System";
    public static final ThingTypeUID THING_TYPE_BUS_ALARM_ZONE = new ThingTypeUID(BINDING_ID, "bus_alarm_zone");
    public static final String THING_LABEL_BUS_ALARM_ZONE = "Alarm Zone";
    public static final ThingTypeUID THING_TYPE_BUS_AUX = new ThingTypeUID(BINDING_ID, "bus_aux");
    public static final String THING_LABEL_BUS_AUX = "Auxiliary";
    // ZIGBEE
    public static final ThingTypeUID THING_TYPE_ZB_ON_OFF_SWITCH = new ThingTypeUID(BINDING_ID, "zb_on_off_switch");
    public static final String THING_LABEL_ZB_ON_OFF_SWITCH = "Zigbee Switch";
    public static final ThingTypeUID THING_TYPE_ZB_ON_OFF_SWITCH_2UNITS = new ThingTypeUID(BINDING_ID,
            "zb_on_off_switch2u");
    public static final String THING_LABEL_ZB_ON_OFF_SWITCH_2UNITS = "Zigbee 2-units Switch";
    public static final ThingTypeUID THING_TYPE_ZB_DIMMER = new ThingTypeUID(BINDING_ID, "zb_dimmer");
    public static final String THING_LABEL_ZB_DIMMER = "Zigbee Dimmer";
    public static final ThingTypeUID THING_TYPE_ZB_AUTOMATION = new ThingTypeUID(BINDING_ID, "zb_automation");
    public static final String THING_LABEL_ZB_AUTOMATION = "Zigbee Automation";

    // #SUPPORTED THINGS SETS
    // ## Generic
    public static final Set<ThingTypeUID> GENERIC_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_GENERIC_DEVICE);
    // ## Lighting
    public static final Set<ThingTypeUID> LIGHTING_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ZB_ON_OFF_SWITCH,
            THING_TYPE_ZB_ON_OFF_SWITCH_2UNITS, THING_TYPE_ZB_DIMMER, THING_TYPE_BUS_ON_OFF_SWITCH,
            THING_TYPE_BUS_DIMMER);
    // ## Automation
    public static final Set<ThingTypeUID> AUTOMATION_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ZB_AUTOMATION,
            THING_TYPE_BUS_AUTOMATION);
    // ## Thermoregulation
    public static final Set<ThingTypeUID> THERMOREGULATION_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BUS_THERMO_ZONE,
            THING_TYPE_BUS_THERMO_SENSOR, THING_TYPE_BUS_THERMO_CU);
    // ## Energy Management
    public static final Set<ThingTypeUID> ENERGY_MANAGEMENT_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BUS_ENERGY_METER);
    // ## CEN/CEN+ Scenario
    public static final Set<ThingTypeUID> SCENARIO_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BUS_CEN_SCENARIO_CONTROL,
            THING_TYPE_BUS_CENPLUS_SCENARIO_CONTROL, THING_TYPE_BUS_DRY_CONTACT_IR);
    // ## Basic Scenario
    public static final Set<ThingTypeUID> SCENARIO_BASIC_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BUS_SCENARIO);
    // ## Aux
    public static final Set<ThingTypeUID> AUX_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BUS_AUX);
    // ## Alarm
    public static final Set<ThingTypeUID> ALARM_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BUS_ALARM_SYSTEM,
            THING_TYPE_BUS_ALARM_ZONE);

    // ## Groups
    public static final Set<ThingTypeUID> DEVICE_SUPPORTED_THING_TYPES = Stream
            .of(LIGHTING_SUPPORTED_THING_TYPES, AUTOMATION_SUPPORTED_THING_TYPES,
                    THERMOREGULATION_SUPPORTED_THING_TYPES, ENERGY_MANAGEMENT_SUPPORTED_THING_TYPES,
                    SCENARIO_SUPPORTED_THING_TYPES, SCENARIO_BASIC_SUPPORTED_THING_TYPES, AUX_SUPPORTED_THING_TYPES,
                    ALARM_SUPPORTED_THING_TYPES, GENERIC_SUPPORTED_THING_TYPES)
            .flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));
    public static final Set<ThingTypeUID> BRIDGE_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ZB_GATEWAY,
            THING_TYPE_BUS_GATEWAY);
    public static final Set<ThingTypeUID> ALL_SUPPORTED_THING_TYPES = Stream
            .of(DEVICE_SUPPORTED_THING_TYPES, BRIDGE_SUPPORTED_THING_TYPES).flatMap(Collection::stream)
            .collect(Collectors.toCollection(HashSet::new));

    // LIST OF ALL CHANNEL IDs
    // lighting
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_SWITCH_01 = "switch_01";
    public static final String CHANNEL_SWITCH_02 = "switch_02";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    // automation
    public static final String CHANNEL_SHUTTER = "shutter";
    // thermo
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_FUNCTION = "function";
    public static final String CHANNEL_TEMP_SETPOINT = "setpointTemperature";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_FAN_SPEED = "speedFanCoil";
    public static final String CHANNEL_CONDITIONING_VALVES = "conditioningValves";
    public static final String CHANNEL_HEATING_VALVES = "heatingValves";
    public static final String CHANNEL_ACTUATORS = "actuators";
    public static final String CHANNEL_LOCAL_OFFSET = "localOffset";
    public static final String CHANNEL_CU_REMOTE_CONTROL = "remoteControl";
    public static final String CHANNEL_CU_BATTERY_STATUS = "batteryStatus";
    public static final String CHANNEL_CU_WEEKLY_PROGRAM_NUMBER = "weeklyProgram";
    public static final String CHANNEL_CU_SCENARIO_PROGRAM_NUMBER = "scenarioProgram";
    public static final String CHANNEL_CU_FAILURE_DISCOVERED = "failureDiscovered";
    public static final String CHANNEL_CU_AT_LEAST_ONE_PROBE_OFF = "atLeastOneProbeOff";
    public static final String CHANNEL_CU_AT_LEAST_ONE_PROBE_PROTECTION = "atLeastOneProbeProtection";
    public static final String CHANNEL_CU_AT_LEAST_ONE_PROBE_MANUAL = "atLeastOneProbeManual";
    // energy management
    public static final String CHANNEL_POWER = "power";
    // scenario button channels
    public static final String CHANNEL_SCENARIO_BUTTON = "button#";
    public static final String CHANNEL_TYPE_CEN_BUTTON_EVENT = "cenButtonEvent";
    public static final String CHANNEL_TYPE_CEN_PLUS_BUTTON_EVENT = "cenPlusButtonEvent";
    public static final String CHANNEL_DRY_CONTACT_IR = "sensor";
    // basic scenario
    public static final String CHANNEL_SCENARIO = "scenario";
    // aux
    public static final String CHANNEL_AUX = "aux";
    // alarm
    public static final String CHANNEL_ALARM_SYSTEM_STATE = "state";
    public static final String CHANNEL_ALARM_SYSTEM_ARMED = "armed";
    public static final String CHANNEL_ALARM_SYSTEM_NETWORK = "network";
    public static final String CHANNEL_ALARM_SYSTEM_BATTERY = "battery";
    public static final String CHANNEL_ALARM_ZONE_STATE = "state";
    public static final String CHANNEL_ALARM_ZONE_ALARM = "alarm";
    public static final String CHANNEL_ALARM_ZONE_ALARM_TIMESTAMP = "timestamp";

    // devices config properties
    public static final String CONFIG_PROPERTY_WHERE = "where";
    public static final String CONFIG_PROPERTY_SHUTTER_RUN = "shutterRun";
    public static final String CONFIG_PROPERTY_SCENARIO_BUTTONS = "buttons";
    public static final String CONFIG_PROPERTY_STANDALONE = "standAlone";

    // gw config properties
    public static final String CONFIG_PROPERTY_HOST = "host";
    public static final String CONFIG_PROPERTY_SERIAL_PORT = "serialPort";
    // properties
    public static final String PROPERTY_OWNID = "ownId";
    public static final String PROPERTY_ZIGBEEID = "zigbeeid";
    public static final String PROPERTY_FIRMWARE_VERSION = "firmwareVersion";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_SERIAL_NO = "serialNumber";
}
