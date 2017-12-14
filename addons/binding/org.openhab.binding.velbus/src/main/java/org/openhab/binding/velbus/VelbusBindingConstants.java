/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link VelbusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusBindingConstants {

    @NonNull
    public static final String BINDING_ID = "velbus";

    // bridge
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_VMB4RYLD = new ThingTypeUID(BINDING_ID, "vmb4ryld");
    public static final ThingTypeUID THING_TYPE_VMB4DC = new ThingTypeUID(BINDING_ID, "vmb4dc");
    public static final ThingTypeUID THING_TYPE_VMB2BLE = new ThingTypeUID(BINDING_ID, "vmb2ble");
    public static final ThingTypeUID THING_TYPE_VMBGPO = new ThingTypeUID(BINDING_ID, "vmbgpo");
    public static final ThingTypeUID THING_TYPE_VMBGPOD = new ThingTypeUID(BINDING_ID, "vmbgpod");
    public static final ThingTypeUID THING_TYPE_VMBGP1 = new ThingTypeUID(BINDING_ID, "vmbgp1");
    public static final ThingTypeUID THING_TYPE_VMBGP2 = new ThingTypeUID(BINDING_ID, "vmbgp2");
    public static final ThingTypeUID THING_TYPE_VMBGP4 = new ThingTypeUID(BINDING_ID, "vmbgp4");
    public static final ThingTypeUID THING_TYPE_VMBGP4PIR = new ThingTypeUID(BINDING_ID, "vmbgp4pir");
    public static final ThingTypeUID THING_TYPE_VMB2PBN = new ThingTypeUID(BINDING_ID, "vmb2pbn");
    public static final ThingTypeUID THING_TYPE_VMB6PBN = new ThingTypeUID(BINDING_ID, "vmb6pbn");
    public static final ThingTypeUID THING_TYPE_VMB8PBU = new ThingTypeUID(BINDING_ID, "vmb8pbu");
    public static final ThingTypeUID THING_TYPE_VMB7IN = new ThingTypeUID(BINDING_ID, "vmb7in");
    public static final ThingTypeUID THING_TYPE_VMBPIRO = new ThingTypeUID(BINDING_ID, "vmbpiro");

    // thing type sets
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_VMB4RYLD,
            THING_TYPE_VMB4DC, THING_TYPE_VMB2BLE, THING_TYPE_VMBGP1, THING_TYPE_VMBGP2, THING_TYPE_VMBGP4,
            THING_TYPE_VMBGP4PIR, THING_TYPE_VMBGPO, THING_TYPE_VMBGPOD, THING_TYPE_VMB2PBN, THING_TYPE_VMB6PBN,
            THING_TYPE_VMB8PBU, THING_TYPE_VMB7IN, THING_TYPE_VMBPIRO);

    // Velbus commands
    public static final byte COMMAND_PUSH_BUTTON_STATUS = 0x00;
    public static final byte COMMAND_SWITCH_RELAY_OFF = 0x01;
    public static final byte COMMAND_SWITCH_RELAY_ON = 0x02;
    public static final byte COMMAND_SWITCH_BLIND_OFF = 0x04;
    public static final byte COMMAND_BLIND_UP = 0x05;
    public static final byte COMMAND_BLIND_DOWN = 0x06;
    public static final byte COMMAND_SET_DIMVALUE = 0x07;
    public static final byte COMMAND_RESTORE_LAST_DIMVALUE = 0x11;
    public static final byte COMMAND_BLIND_POS = 0x1C;
    public static final byte COMMAND_SUBTYPE = (byte) 0xB0;
    public static final byte COMMAND_DIMMERCONTROLLER_STATUS = (byte) 0xB8;
    public static final byte COMMAND_BLIND_STATUS = (byte) 0xEC;
    public static final byte COMMAND_SENSOR_TEMP_REQUEST = (byte) 0xE5;
    public static final byte COMMAND_SENSOR_TEMPERATURE = (byte) 0xE6;
    public static final byte COMMAND_MODULE_NAME_REQUEST = (byte) 0xEF;
    public static final byte COMMAND_MODULE_NAME_PART1 = (byte) 0xF0;
    public static final byte COMMAND_MODULE_NAME_PART2 = (byte) 0xF1;
    public static final byte COMMAND_MODULE_NAME_PART3 = (byte) 0xF2;
    public static final byte COMMAND_STATUS_REQUEST = (byte) 0xFA;
    public static final byte COMMAND_RELAY_STATUS = (byte) 0xFB;
    public static final byte COMMAND_MODULE_TYPE = (byte) 0xFF;

    // Module properties
    public static final String PORT = "port";
    public static final String MODULE_ADDRESS = "address";
    public static final String MODULE_SERIAL_NUMBER = "serial number";
    public static final String MODULE_MEMORY_MAP_VERSION = "memory map version";
    public static final String MODULE_BUILD = "build";
    public static final String CHANNEL = "channel";
    public static final String SUB_ADDRESS = "subaddress";
}
