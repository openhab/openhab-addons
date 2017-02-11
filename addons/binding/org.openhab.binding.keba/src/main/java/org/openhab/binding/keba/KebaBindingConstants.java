/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.keba;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KebaBinding} class defines common constants, which are used across
 * the whole binding.
 * 
 * @author Karel Goderis - Initial contribution
 */
public class KebaBindingConstants {

	public static final String BINDING_ID = "keba";

	// List of all Thing Type UIDs
	public final static ThingTypeUID THING_TYPE_KECONTACTP20 = new ThingTypeUID(
			BINDING_ID, "kecontactp20");

	// List of all Channel ids
	public final static String CHANNEL_MODEL = "model";
	public final static String CHANNEL_FIRMWARE = "firmware";
	public final static String CHANNEL_STATE = "state";
	public final static String CHANNEL_ERROR = "error";
	public final static String CHANNEL_WALLBOX = "wallbox";
	public final static String CHANNEL_VEHICLE = "vehicle";
	public final static String CHANNEL_PLUG_LOCKED = "locked";
	public final static String CHANNEL_ENABLED = "enabled";
	public final static String CHANNEL_MAX_SYSTEM_CURRENT = "maxsystemcurrent";
	public final static String CHANNEL_MAX_PRESET_CURRENT_RANGE = "maxpresetcurrentrange";
	public final static String CHANNEL_MAX_PRESET_CURRENT = "maxpresetcurrent";
	public final static String CHANNEL_FAILSAFE_CURRENT = "failsafecurrent";
	public final static String CHANNEL_INPUT = "input";
	public final static String CHANNEL_OUTPUT = "output";
	public final static String CHANNEL_SERIAL = "serial";
	public final static String CHANNEL_UPTIME = "uptime";
	public final static String CHANNEL_I1 = "I1";
	public final static String CHANNEL_I2 = "I2";
	public final static String CHANNEL_I3 = "I3";
	public final static String CHANNEL_U1 = "U1";
	public final static String CHANNEL_U2 = "U2";
	public final static String CHANNEL_U3 = "U3";
	public final static String CHANNEL_POWER = "power";
	public final static String CHANNEL_POWER_FACTOR = "powerfactor";
	public final static String CHANNEL_SESSION_CONSUMPTION = "sessionconsumption";
	public final static String CHANNEL_TOTAL_CONSUMPTION = "totalconsumption";

}
