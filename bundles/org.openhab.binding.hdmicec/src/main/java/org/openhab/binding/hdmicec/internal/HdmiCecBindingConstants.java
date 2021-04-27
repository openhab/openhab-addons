/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.hdmicec.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HdmiCecBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Masshardt - Initial contribution
 * @author Sam Spencer - Conversion to OH3 and submission
 */
@NonNullByDefault
public class HdmiCecBindingConstants {

    private static final String BINDING_ID = "hdmicec";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_EQUIPMENT = new ThingTypeUID(BINDING_ID, "equipment");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_ACTIVE_SOURCE = "activeSource";
    public static final String CHANNEL_EVENT = "event";
    public static final String CHANNEL_SEND = "send";
    public static final String CHANNEL_SEND_CEC = "sendCEC";
    public static final String CHANNEL_REMOTE_BUTTON = "remoteButton";

    // List of Configurations constants
    public static final String UNIQUE_ID = "uniqueId";
    public static final String VENDOR = "vendor";
    public static final String OSD = "osd";
    public static final String DEVICE_INDEX = "deviceIndex";
    public static final String ADDRESS = "address";
}
