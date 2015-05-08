/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max;

import java.util.Collection;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The {@link MaxBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxBinding {

	public static final String BINDING_ID = "max";

	// List of main device types 
	public static final String DEVICE_THERMOSTAT = "thermostat";
	public static final String DEVICE_THERMOSTATPLUS = "thermostatplus";
	public static final String DEVICE_WALLTHERMOSTAT = "wallthermostat";
	public static final String DEVICE_ECOSWITCH = "ecoswitch";
	public static final String DEVICE_SHUTTERCONTACT = "shuttercontact";
	public static final String BRIDGE_MAXCUBE = "bridge";

	// List of all Thing Type UIDs
	public final static ThingTypeUID HEATINGTHERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_THERMOSTAT);
	public final static ThingTypeUID HEATINGTHERMOSTATPLUS_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_THERMOSTATPLUS);
	public final static ThingTypeUID WALLTHERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_WALLTHERMOSTAT);
	public final static ThingTypeUID ECOSWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_ECOSWITCH);
	public final static ThingTypeUID SHUTTERCONTACT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_SHUTTERCONTACT);
	public final static ThingTypeUID CUBEBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_MAXCUBE);

	// List of all Channel ids
	public final static String CHANNEL_VALVE = "valve";
	public final static String CHANNEL_BATTERY = "battery_low";
	public final static String CHANNEL_MODE = "mode";
	public final static String CHANNEL_ACTUALTEMP = "actual_temp";
	public final static String CHANNEL_SETTEMP = "set_temp";
	public final static String CHANNEL_SWITCH_STATE = "eco_mode";
	public final static String CHANNEL_CONTACT_STATE = "contact_state";
	public final static String CHANNEL_FREE_MEMORY = "free_mem";
	public final static String CHANNEL_DUTY_CYCLE = "duty_cycle";

	//Custom Properties
	public final static String PROPERTY_SERIAL_NUMBER = "serialNumber";
	public final static String PROPERTY_IP_ADDRESS = "ipAddress";
	public final static String PROPERTY_VENDOR_NAME = "eQ-3 AG";
	public final static String PROPERTY_RFADDRESS = "rfAddress";
	public final static String PROPERTY_ROOMNAME = "room";
	public final static String PROPERTY_DEVICENAME = "name";
	

	public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(
			HEATINGTHERMOSTAT_THING_TYPE, HEATINGTHERMOSTATPLUS_THING_TYPE, WALLTHERMOSTAT_THING_TYPE, 
			ECOSWITCH_THING_TYPE, SHUTTERCONTACT_THING_TYPE, CUBEBRIDGE_THING_TYPE);

	public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS =ImmutableSet.of(
			HEATINGTHERMOSTAT_THING_TYPE, HEATINGTHERMOSTATPLUS_THING_TYPE, WALLTHERMOSTAT_THING_TYPE, 
			ECOSWITCH_THING_TYPE, SHUTTERCONTACT_THING_TYPE);


	public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS =ImmutableSet.of(
			CUBEBRIDGE_THING_TYPE);
}
