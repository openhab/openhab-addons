/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VelbusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusBindingConstants {

    public static final String BINDING_ID = "velbus";

    // bridge
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_VMB1BLS = new ThingTypeUID(BINDING_ID, "vmb1bls");
    public static final ThingTypeUID THING_TYPE_VMB1DM = new ThingTypeUID(BINDING_ID, "vmb1dm");
    public static final ThingTypeUID THING_TYPE_VMB1LED = new ThingTypeUID(BINDING_ID, "vmb1led");
    public static final ThingTypeUID THING_TYPE_VMB1RY = new ThingTypeUID(BINDING_ID, "vmb1ry");
    public static final ThingTypeUID THING_TYPE_VMB1RYNO = new ThingTypeUID(BINDING_ID, "vmb1ryno");
    public static final ThingTypeUID THING_TYPE_VMB1RYNOS = new ThingTypeUID(BINDING_ID, "vmb1rynos");
    public static final ThingTypeUID THING_TYPE_VMB2BLE = new ThingTypeUID(BINDING_ID, "vmb2ble");
    public static final ThingTypeUID THING_TYPE_VMB2PBN = new ThingTypeUID(BINDING_ID, "vmb2pbn");
    public static final ThingTypeUID THING_TYPE_VMB4DC = new ThingTypeUID(BINDING_ID, "vmb4dc");
    public static final ThingTypeUID THING_TYPE_VMB4RY = new ThingTypeUID(BINDING_ID, "vmb4ry");
    public static final ThingTypeUID THING_TYPE_VMB4RYLD = new ThingTypeUID(BINDING_ID, "vmb4ryld");
    public static final ThingTypeUID THING_TYPE_VMB4RYNO = new ThingTypeUID(BINDING_ID, "vmb4ryno");
    public static final ThingTypeUID THING_TYPE_VMB6IN = new ThingTypeUID(BINDING_ID, "vmb6in");
    public static final ThingTypeUID THING_TYPE_VMB6PBN = new ThingTypeUID(BINDING_ID, "vmb6pbn");
    public static final ThingTypeUID THING_TYPE_VMB7IN = new ThingTypeUID(BINDING_ID, "vmb7in");
    public static final ThingTypeUID THING_TYPE_VMB8IR = new ThingTypeUID(BINDING_ID, "vmb8ir");
    public static final ThingTypeUID THING_TYPE_VMB8PB = new ThingTypeUID(BINDING_ID, "vmb8pb");
    public static final ThingTypeUID THING_TYPE_VMB8PBU = new ThingTypeUID(BINDING_ID, "vmb8pbu");
    public static final ThingTypeUID THING_TYPE_VMBDME = new ThingTypeUID(BINDING_ID, "vmbdme");
    public static final ThingTypeUID THING_TYPE_VMBDMI = new ThingTypeUID(BINDING_ID, "vmbdmi");
    public static final ThingTypeUID THING_TYPE_VMBDMIR = new ThingTypeUID(BINDING_ID, "vmbdmir");
    public static final ThingTypeUID THING_TYPE_VMBGP1 = new ThingTypeUID(BINDING_ID, "vmbgp1");
    public static final ThingTypeUID THING_TYPE_VMBGP2 = new ThingTypeUID(BINDING_ID, "vmbgp2");
    public static final ThingTypeUID THING_TYPE_VMBGP4 = new ThingTypeUID(BINDING_ID, "vmbgp4");
    public static final ThingTypeUID THING_TYPE_VMBGP4PIR = new ThingTypeUID(BINDING_ID, "vmbgp4pir");
    public static final ThingTypeUID THING_TYPE_VMBGPO = new ThingTypeUID(BINDING_ID, "vmbgpo");
    public static final ThingTypeUID THING_TYPE_VMBGPOD = new ThingTypeUID(BINDING_ID, "vmbgpod");
    public static final ThingTypeUID THING_TYPE_VMBPIRC = new ThingTypeUID(BINDING_ID, "vmbpirc");
    public static final ThingTypeUID THING_TYPE_VMBPIRM = new ThingTypeUID(BINDING_ID, "vmbpirm");
    public static final ThingTypeUID THING_TYPE_VMBPIRO = new ThingTypeUID(BINDING_ID, "vmbpiro");

    // thing type sets
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(THING_TYPE_VMB1BLS, THING_TYPE_VMB1DM, THING_TYPE_VMB1LED,
                    THING_TYPE_VMB1RY, THING_TYPE_VMB1RYNO, THING_TYPE_VMB1RYNOS, THING_TYPE_VMB2BLE,
                    THING_TYPE_VMB2PBN, THING_TYPE_VMB4DC, THING_TYPE_VMB4RY, THING_TYPE_VMB4RYLD, THING_TYPE_VMB4RYNO,
                    THING_TYPE_VMB6IN, THING_TYPE_VMB6PBN, THING_TYPE_VMB7IN, THING_TYPE_VMB8IR, THING_TYPE_VMB8PB,
                    THING_TYPE_VMB8PBU, THING_TYPE_VMBDME, THING_TYPE_VMBDMI, THING_TYPE_VMBDMIR, THING_TYPE_VMBGP1,
                    THING_TYPE_VMBGP2, THING_TYPE_VMBGP4, THING_TYPE_VMBGP4PIR, THING_TYPE_VMBGPO, THING_TYPE_VMBGPOD,
                    THING_TYPE_VMBPIRC, THING_TYPE_VMBPIRM, THING_TYPE_VMBPIRO)));

    // Velbus module types
    public static final byte MODULE_TYPE_VMB8PB = 0x01;
    public static final byte MODULE_TYPE_VMB1RY = 0x02;
    public static final byte MODULE_TYPE_VMB6IN = 0x05;
    public static final byte MODULE_TYPE_VMB1DM = 0x07;
    public static final byte MODULE_TYPE_VMB4RY = 0x08;
    public static final byte MODULE_TYPE_VMB8IR = 0x0A;
    public static final byte MODULE_TYPE_VMB1LED = 0x0F;
    public static final byte MODULE_TYPE_VMB4RYLD = 0x10;
    public static final byte MODULE_TYPE_VMB4RYNO = 0x11;
    public static final byte MODULE_TYPE_VMB4DC = 0x12;
    public static final byte MODULE_TYPE_VMBDME = 0x14;
    public static final byte MODULE_TYPE_VMBDMI = 0x15;
    public static final byte MODULE_TYPE_VMB8PBU = 0x16;
    public static final byte MODULE_TYPE_VMB6PBN = 0x17;
    public static final byte MODULE_TYPE_VMB2PBN = 0x18;
    public static final byte MODULE_TYPE_VMB1RYNO = 0x1B;
    public static final byte MODULE_TYPE_VMB2BLE = 0x1D;
    public static final byte MODULE_TYPE_VMBGP1 = 0x1E;
    public static final byte MODULE_TYPE_VMBGP2 = 0x1F;
    public static final byte MODULE_TYPE_VMBGP4 = 0x20;
    public static final byte MODULE_TYPE_VMBGPO = 0x21;
    public static final byte MODULE_TYPE_VMB7IN = 0x22;
    public static final byte MODULE_TYPE_VMBGPOD = 0x28;
    public static final byte MODULE_TYPE_VMB1RYNOS = 0x29;
    public static final byte MODULE_TYPE_VMBPIRM = 0x2A;
    public static final byte MODULE_TYPE_VMBPIRC = 0x2B;
    public static final byte MODULE_TYPE_VMBPIRO = 0x2C;
    public static final byte MODULE_TYPE_VMBGP4PIR = 0x2D;
    public static final byte MODULE_TYPE_VMB1BLS = 0x2E;
    public static final byte MODULE_TYPE_VMBDMIR = 0x2F;

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
    public static final byte COMMAND_DIMMER_STATUS = (byte) 0xEE;
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
    public static final String REFRESH_INTERVAL = "refresh";
    public static final String MODULE_SERIAL_NUMBER = "serial number";
    public static final String MODULE_MEMORY_MAP_VERSION = "memory map version";
    public static final String MODULE_BUILD = "build";
    public static final String CHANNEL = "CH";
    public static final String SUB_ADDRESS = "subaddress";
}
