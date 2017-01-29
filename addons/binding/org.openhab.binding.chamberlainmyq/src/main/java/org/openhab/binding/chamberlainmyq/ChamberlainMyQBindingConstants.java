/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chamberlainmyq;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ChamberlainMyQBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Scott Hanson - Initial contribution
 */
public class ChamberlainMyQBindingConstants {

    public static final String BINDING_ID = "chamberlainmyq";

    // bridge
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "MyqGateway");
    
    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_DOOR_OPENER = new ThingTypeUID(BINDING_ID, "DoorOpener");
	public final static ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "Light");

    // List of all Channel ids
    public static final String CHANNEL_LIGHT_STATE = "lightstate";
	public static final String CHANNEL_DOOR_STATE = "doorstate";

    // Bridge config properties
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String POLL_PERIOD = "pollPeriod";
	public static final String QUICK_POLL_PERIOD = "quickPollPeriod";
	public static final String TIME_OUT = "timeout";

    // Door Opener/Light config properties
    public static final String MYQ_ID = "MyQDeviceId";

}
