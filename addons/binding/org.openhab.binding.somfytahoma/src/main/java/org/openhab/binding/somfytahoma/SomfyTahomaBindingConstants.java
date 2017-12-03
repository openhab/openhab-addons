/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SomfyTahomaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaBindingConstants {

    public static final @NonNull String BINDING_ID = "somfytahoma";

    // Things
    // Bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // Gateway
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    // Roller Shutter
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");

    // Silent Roller Shutter
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER_SILENT = new ThingTypeUID(BINDING_ID, "rollershutter_silent");

    // Screen
    public static final ThingTypeUID THING_TYPE_SCREEN = new ThingTypeUID(BINDING_ID, "screen");

    // Venetian Blind
    public static final ThingTypeUID THING_TYPE_VENETIANBLIND = new ThingTypeUID(BINDING_ID, "venetianblind");

    // Exterior Screen
    public static final ThingTypeUID THING_TYPE_EXTERIORSCREEN = new ThingTypeUID(BINDING_ID, "exteriorscreen");

    // Exterior Venetian Blind
    public static final ThingTypeUID THING_TYPE_EXTERIORVENETIANBLIND = new ThingTypeUID(BINDING_ID, "exteriorvenetianblind");

    // Garage Door
    public static final ThingTypeUID THING_TYPE_GARAGEDOOR = new ThingTypeUID(BINDING_ID, "garagedoor");

    // Awning
    public static final ThingTypeUID THING_TYPE_AWNING = new ThingTypeUID(BINDING_ID, "awning");
    
    // Actiongroup
    public static final ThingTypeUID THING_TYPE_ACTIONGROUP = new ThingTypeUID(BINDING_ID, "actiongroup");

    // On Off
    public static final ThingTypeUID THING_TYPE_ONOFF = new ThingTypeUID(BINDING_ID, "onoff");

    // Light
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");

    // Light sensor
    public static final ThingTypeUID THING_TYPE_LIGHTSENSOR = new ThingTypeUID(BINDING_ID, "lightsensor");

    // Smoke sensor
    public static final ThingTypeUID THING_TYPE_SMOKESENSOR = new ThingTypeUID(BINDING_ID, "smokesensor");

    // Contact sensor
    public static final ThingTypeUID THING_TYPE_CONTACTSENSOR = new ThingTypeUID(BINDING_ID, "contactsensor");

    // Occupancy sensor
    public static final ThingTypeUID THING_TYPE_OCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID, "occupancysensor");

    // Window
    public static final ThingTypeUID THING_TYPE_WINDOW = new ThingTypeUID(BINDING_ID, "window");

    // Alarm
    public static final ThingTypeUID THING_TYPE_ALARM = new ThingTypeUID(BINDING_ID, "alarm");

    // List of all Channel ids
    // Gateway
    public static final String VERSION = "version";

    // Roller shutter, Awning, Screen, Blind, Garage door, Window
    public static final String POSITION = "position";
    public static final String CONTROL = "control";

    // Silent roller shutter
    public static final String POSITION_SILENT = "position_silent";

    // Blind
    public static final String ORIENTATION = "orientation";

    // Action group
    public static final String TRIGGER = "trigger";

    // OnOff, Light
    public static final String SWITCH = "switch";

    // Smoke sensor, Occupancy sensor, Contact sensor
    public static final String CONTACT = "contact";

    // Light sensor
    public static final String LUMINANCE = "luminance";

    // Alarm
    public static final String ALARM_COMMAND = "alarm_command";
    public static final String ALARM_STATE = "alarm_state";
    public static final String TARGET_ALARM_STATE = "target_alarm_state";
    public static final String INTRUSION_CONTROL = "intrusion_control";
    public static final String INTRUSION_STATE = "intrusion_state";

    //Constants
    final private static String API_URL = "https://www.tahomalink.com/enduser-mobile-web/";
    final public static String TAHOMA_URL = API_URL + "externalAPI/json/";
    final public static String SETUP_URL = API_URL + "enduserAPI/setup/gateways/";
    final public static String EXEC_URL = API_URL + "enduserAPI/exec/";
    final public static String DELETE_URL = EXEC_URL + "current/setup/";
    final public static String TAHOMA_AGENT = "TaHoma/3640 CFNetwork/711.1.16 Darwin/14.0.0";
    final public static String UNAUTHORIZED = "Server returned HTTP response code: 401";
    final public static int TYPE_PERCENT = 1;
    final public static int TYPE_DECIMAL = 2;
    final public static int TYPE_STRING = 3;
    final public static String COMMAND_MY = "my";
    final public static String COMMAND_SET_CLOSURE = "setClosure";
    final public static String COMMAND_SET_DEPLOYMENT = "setDeployment";
    final public static String COMMAND_SET_ORIENTATION = "setOrientation";
    final public static String COMMAND_SET_CLOSURESPEED = "setClosureAndLinearSpeed";
    final public static String COMMAND_UP = "up";
    final public static String COMMAND_DOWN = "down";
    final public static String COMMAND_OPEN = "open";
    final public static String COMMAND_CLOSE = "close";
    final public static String COMMAND_STOP = "stop";
    final public static String STATUS_STATE = "core:StatusState";
    final public static String UNAVAILABLE = "unavailable";

    // supproted uiClasses
    final public static String ROLLERSHUTTER = "RollerShutter";
    final public static String SCREEN = "Screen";
    final public static String VENETIANBLIND = "VenetianBlind";
    final public static String EXTERIORSCREEN = "ExteriorScreen";
    final public static String EXTERIORVENETIANBLIND = "ExteriorVenetianBlind";
    final public static String GARAGEDOOR = "GarageDoor";
    final public static String AWNING = "Awning";
    final public static String ONOFF = "OnOff";
    final public static String LIGHT = "Light";
    final public static String LIGHTSENSOR = "LightSensor";
    final public static String SMOKESENSOR = "SmokeSensor";
    final public static String CONTACTSENSOR = "ContactSensor";
    final public static String OCCUPANCYSENSOR = "OccupancySensor";
    final public static String WINDOW = "Window";
    // unsupported uiClasses
    final public static String ALARM = "Alarm";
    final public static String POD = "Pod";
    final public static String PROTOCOLGATEWAY = "ProtocolGateway";
}
