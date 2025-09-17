/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SerialBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class SerialBindingConstants {

    private static final String BINDING_ID = "serial";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERIAL_BRIDGE = new ThingTypeUID(BINDING_ID, "serialBridge");
    public static final ThingTypeUID THING_TYPE_TCP_BRIDGE = new ThingTypeUID(BINDING_ID, "tcpBridge");
    public static final ThingTypeUID THING_TYPE_TCP_SERVER_BRIDGE = new ThingTypeUID(BINDING_ID, "tcpServerBridge");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "serialDevice");

    // List of all Channel ids
    public static final String TRIGGER_CHANNEL = "data";
    public static final String STRING_CHANNEL = "string";
    public static final String BINARY_CHANNEL = "binary";
    public static final String DEVICE_STRING_CHANNEL = "string";
    public static final String DEVICE_NUMBER_CHANNEL = "number";
    public static final String DEVICE_DIMMER_CHANNEL = "dimmer";
    public static final String DEVICE_SWITCH_CHANNEL = "switch";
    public static final String DEVICE_ROLLERSHUTTER_CHANNEL = "rollershutter";
}
