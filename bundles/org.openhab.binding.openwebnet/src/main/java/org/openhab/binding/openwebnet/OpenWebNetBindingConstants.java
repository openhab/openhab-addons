/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OpenWebNetBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Massimo Valla - Initial contribution
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
    public static final Set<ThingTypeUID> GENERIC_SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_GENERIC_DEVICE));
    // ## Lighting
    public static final Set<ThingTypeUID> LIGHTING_SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_ZB_ON_OFF_SWITCH, THING_TYPE_ZB_ON_OFF_SWITCH_2UNITS, THING_TYPE_ZB_DIMMER,
                    THING_TYPE_BUS_ON_OFF_SWITCH, THING_TYPE_BUS_DIMMER));
    // ## Automation
    public static final Set<ThingTypeUID> AUTOMATION_SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_ZB_AUTOMATION, THING_TYPE_BUS_AUTOMATION));

    // ## Groups
    public static final Set<ThingTypeUID> DEVICE_SUPPORTED_THING_TYPES = Stream
            .of(LIGHTING_SUPPORTED_THING_TYPES, AUTOMATION_SUPPORTED_THING_TYPES, GENERIC_SUPPORTED_THING_TYPES)
            .flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));

    public static final Set<ThingTypeUID> BRIDGE_SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_ZB_GATEWAY, THING_TYPE_BUS_GATEWAY));

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

    // devices config properties
    public static final String CONFIG_PROPERTY_WHERE = "where";
    public static final String CONFIG_PROPERTY_SHUTTER_RUN = "shutterRun";

    // BUS gw config properties
    public static final String CONFIG_PROPERTY_HOST = "host";
    // properties
    public static final String PROPERTY_OWNID = "ownId";
    public static final String PROPERTY_ZIGBEEID = "zigbeeid";
    public static final String PROPERTY_FIRMWARE_VERSION = "firmwareVersion";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_SERIAL_NO = "serialNumber";
}
