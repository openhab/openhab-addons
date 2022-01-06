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
package org.openhab.binding.openwebnet.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OpenWebNetBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Massimo Valla - Initial contribution
 * @author Gilberto Cocchi - Thermoregulation
 * @author Andrea Conte - Energy management, Thermoregulation
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
    public static final String THING_LABEL_ZB_GATEWAY = "ZigBee USB Gateway";
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
    public static final ThingTypeUID THING_TYPE_BUS_CEN_SCENARIO_CONTROL = new ThingTypeUID(BINDING_ID,
            "bus_cen_scenario_control");
    public static final String THING_LABEL_BUS_CEN_SCENARIO_CONTROL = "CEN Control";
    public static final ThingTypeUID THING_TYPE_BUS_DRY_CONTACT_IR = new ThingTypeUID(BINDING_ID, "bus_dry_contact_ir");
    public static final String THING_LABEL_BUS_DRY_CONTACT_IR = "Dry Contact/IR";
    public static final ThingTypeUID THING_TYPE_BUS_CENPLUS_SCENARIO_CONTROL = new ThingTypeUID(BINDING_ID,
            "bus_cenplus_scenario_control");
    public static final String THING_LABEL_BUS_CENPLUS_SCENARIO_CONTROL = "CEN+ Control";

    // ZIGBEE
    public static final ThingTypeUID THING_TYPE_ZB_ON_OFF_SWITCH = new ThingTypeUID(BINDING_ID, "zb_on_off_switch");
    public static final String THING_LABEL_ZB_ON_OFF_SWITCH = "ZigBee Switch";
    public static final ThingTypeUID THING_TYPE_ZB_ON_OFF_SWITCH_2UNITS = new ThingTypeUID(BINDING_ID,
            "zb_on_off_switch2u");
    public static final String THING_LABEL_ZB_ON_OFF_SWITCH_2UNITS = "ZigBee 2-units Switch";
    public static final ThingTypeUID THING_TYPE_ZB_DIMMER = new ThingTypeUID(BINDING_ID, "zb_dimmer");
    public static final String THING_LABEL_ZB_DIMMER = "ZigBee Dimmer";
    public static final ThingTypeUID THING_TYPE_ZB_AUTOMATION = new ThingTypeUID(BINDING_ID, "zb_automation");
    public static final String THING_LABEL_ZB_AUTOMATION = "ZigBee Automation";

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
            THING_TYPE_BUS_THERMO_SENSOR);
    // ## Energy Management
    public static final Set<ThingTypeUID> ENERGY_MANAGEMENT_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BUS_ENERGY_METER);
    // ## CEN/CEN+ Scenario
    public static final Set<ThingTypeUID> SCENARIO_SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BUS_CEN_SCENARIO_CONTROL,
            THING_TYPE_BUS_CENPLUS_SCENARIO_CONTROL, THING_TYPE_BUS_DRY_CONTACT_IR);
    // ## Groups
    public static final Set<ThingTypeUID> DEVICE_SUPPORTED_THING_TYPES = Stream
            .of(LIGHTING_SUPPORTED_THING_TYPES, AUTOMATION_SUPPORTED_THING_TYPES,
                    THERMOREGULATION_SUPPORTED_THING_TYPES, ENERGY_MANAGEMENT_SUPPORTED_THING_TYPES,
                    SCENARIO_SUPPORTED_THING_TYPES, GENERIC_SUPPORTED_THING_TYPES)
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
    // energy management
    public static final String CHANNEL_POWER = "power";
    // scenario button channels
    public static final String CHANNEL_SCENARIO_BUTTON = "button#";
    public static final String CHANNEL_TYPE_CEN_BUTTON_EVENT = "cenButtonEvent";
    public static final String CHANNEL_TYPE_CEN_PLUS_BUTTON_EVENT = "cenPlusButtonEvent";
    public static final String CHANNEL_DRY_CONTACT_IR = "sensor";

    // devices config properties
    public static final String CONFIG_PROPERTY_WHERE = "where";
    public static final String CONFIG_PROPERTY_SHUTTER_RUN = "shutterRun";
    public static final String CONFIG_PROPERTY_SCENARIO_BUTTONS = "buttons";
    // BUS gw config properties
    public static final String CONFIG_PROPERTY_HOST = "host";
    public static final String CONFIG_PROPERTY_SERIAL_PORT = "serialPort";
    // properties
    public static final String PROPERTY_OWNID = "ownId";
    public static final String PROPERTY_ZIGBEEID = "zigbeeid";
    public static final String PROPERTY_FIRMWARE_VERSION = "firmwareVersion";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_SERIAL_NO = "serialNumber";
}
