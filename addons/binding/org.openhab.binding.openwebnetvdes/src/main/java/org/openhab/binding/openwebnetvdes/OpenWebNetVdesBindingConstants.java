/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnetvdes;

import java.util.Collection;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The {@link OpenWebNetVdesBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author DmytroKulyanda - Initial contribution
 */
public class OpenWebNetVdesBindingConstants {

    public static final String BINDING_ID = "openwebnetvdes";
    public static final String SERIAL_NUMBER = "serialNumber";
	public static final String IP_ADDRESS = "ipAddress";
	public static final String OWN_WHERE_ADDRESS = "whereAddress";
    
    // List of main device types 
    public static final String DEVICE_VIDEO_CAMERA_ENTRANCE_PANEL = "VideoCameraEntrancePanel";
    public static final String DEVICE_APARTMENT_CAMERA = "IndoorCamera";
    public static final String DEVICE_DOOR_LOCK_ACTUATOR = "DoorlockActuator";
    public static final String BRIDGE_IP_2WIRE_INTERFACE = "Ip2WIREInterface";
    
 // List of all Thing Type UIDs
    public final static ThingTypeUID VIDEO_CAMERA_ENTRANCE_PANEL_THING_TYPE = 
    		new ThingTypeUID(BINDING_ID, DEVICE_VIDEO_CAMERA_ENTRANCE_PANEL);
    public final static ThingTypeUID APARTMENT_CAMERA_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_APARTMENT_CAMERA);
    public final static ThingTypeUID DOOR_LOCK_ACTUATOR_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DOOR_LOCK_ACTUATOR);
    public final static ThingTypeUID IP_2WIRE_INTERFACE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_IP_2WIRE_INTERFACE);

    // List of all Channel ids
    public final static String CHANNEL_SWITCH_ON_OFF_CAMERA	= "switchOnOffCamera";
    public final static String CHANNEL_OPEN_LOCK 			= "openLock";

    public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(
    		VIDEO_CAMERA_ENTRANCE_PANEL_THING_TYPE, APARTMENT_CAMERA_THING_TYPE, DOOR_LOCK_ACTUATOR_THING_TYPE, 
    		IP_2WIRE_INTERFACE_THING_TYPE);

    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS =ImmutableSet.of(
    		VIDEO_CAMERA_ENTRANCE_PANEL_THING_TYPE, APARTMENT_CAMERA_THING_TYPE, DOOR_LOCK_ACTUATOR_THING_TYPE, 
    		IP_2WIRE_INTERFACE_THING_TYPE);


    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS =ImmutableSet.of(
    		IP_2WIRE_INTERFACE_THING_TYPE);
    
    // Open Web Net constants    
    public static final String OWN_WHERE = "WHERE";
    public static final String OWN_RESPONSE_MESSAGE_ACK = "*#*1##";
    public static final String OWN_RESPONSE_MESSAGE_NACK = "*#*0##";
    public static final int OWN_RESPONSE_ACK = 1;
    public static final int OWN_RESPONSE_NACK = 0;
    
    public static final String OPEN_MSG_99_0 = "*99*0##";
    public static final String OPEN_MSG_MAC_ADDRESS_RESPONSE = "*#13**12##";
}
