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
package org.openhab.binding.alarmdecoder.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AlarmDecoderBindingConstants} class defines common constants, which are
 * used throughout the binding.
 *
 * @author Bob Adair - Initial contribution
 * @author Bill Forsyth - Initial contribution
 */
@NonNullByDefault
public class AlarmDecoderBindingConstants {

    private static final String BINDING_ID = "alarmdecoder";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_IPBRIDGE = new ThingTypeUID(BINDING_ID, "ipbridge");
    public static final ThingTypeUID THING_TYPE_SERIALBRIDGE = new ThingTypeUID(BINDING_ID, "serialbridge");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_RFZONE = new ThingTypeUID(BINDING_ID, "rfzone");
    public static final ThingTypeUID THING_TYPE_VZONE = new ThingTypeUID(BINDING_ID, "vzone");
    public static final ThingTypeUID THING_TYPE_KEYPAD = new ThingTypeUID(BINDING_ID, "keypad");
    public static final ThingTypeUID THING_TYPE_LRR = new ThingTypeUID(BINDING_ID, "lrr");

    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPE_UIDS = Collections.unmodifiableSet(Stream
            .of(THING_TYPE_ZONE, THING_TYPE_RFZONE, THING_TYPE_KEYPAD, THING_TYPE_LRR).collect(Collectors.toSet()));

    // Bridge properties
    public static final String PROPERTY_SERIALNUM = "serialNumber";
    public static final String PROPERTY_VERSION = "firmwareVersion";
    public static final String PROPERTY_CAPABILITIES = "capabilities";

    // Channel IDs for ZoneHandler
    public static final String PROPERTY_ADDRESS = "address";
    public static final String PROPERTY_CHANNEL = "channel";
    public static final String PROPERTY_ID = "id";

    public static final String CHANNEL_CONTACT = "contact";

    // Channel IDs for VZoneHandler
    public static final String CHANNEL_COMMAND = "command";

    // Channel IDs for RFZoneHandler
    public static final String PROPERTY_SERIAL = "serial";

    public static final String CHANNEL_RF_LOWBAT = "lowbat";
    public static final String CHANNEL_RF_SUPERVISION = "supervision";
    public static final String CHANNEL_RF_LOOP1 = "loop1";
    public static final String CHANNEL_RF_LOOP2 = "loop2";
    public static final String CHANNEL_RF_LOOP3 = "loop3";
    public static final String CHANNEL_RF_LOOP4 = "loop4";

    // Channel IDs for KeypadHandler
    public static final String CHANNEL_KP_ZONE = "zone";
    public static final String CHANNEL_KP_TEXT = "text";
    public static final String CHANNEL_KP_READY = "ready";
    public static final String CHANNEL_KP_ARMEDAWAY = "armedaway";
    public static final String CHANNEL_KP_ARMEDHOME = "armedhome";
    public static final String CHANNEL_KP_BACKLIGHT = "backlight";
    public static final String CHANNEL_KP_PRORGAM = "program";
    public static final String CHANNEL_KP_BEEPS = "beeps";
    public static final String CHANNEL_KP_BYPASSED = "bypassed";
    public static final String CHANNEL_KP_ACPOWER = "acpower";
    public static final String CHANNEL_KP_CHIME = "chime";
    public static final String CHANNEL_KP_ALARMOCCURRED = "alarmoccurred";
    public static final String CHANNEL_KP_ALARM = "alarm";
    public static final String CHANNEL_KP_LOWBAT = "lowbat";
    public static final String CHANNEL_KP_DELAYOFF = "delayoff";
    public static final String CHANNEL_KP_FIRE = "fire";
    public static final String CHANNEL_KP_SYSFAULT = "sysfault";
    public static final String CHANNEL_KP_PERIMETER = "perimeter";
    public static final String CHANNEL_KP_COMMAND = "command";
    public static final String CHANNEL_KP_INTCOMMAND = "intcommand";
    public static final String DEFAULT_MAPPING = "0=0,1=1,2=2,3=3,4=4,5=5,6=6,7=7,8=8,9=9,10=*,11=#";

    // Channel IDs for LRRHandler
    public static final String CHANNEL_LRR_PARTITION = "partition";
    public static final String CHANNEL_LRR_EVENTDATA = "eventdata";
    public static final String CHANNEL_LRR_CIDMESSAGE = "cidmessage";
    public static final String CHANNEL_LRR_REPORTCODE = "reportcode";
}
