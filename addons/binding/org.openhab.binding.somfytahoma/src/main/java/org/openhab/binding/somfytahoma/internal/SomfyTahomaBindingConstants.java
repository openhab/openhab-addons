/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.somfytahoma.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SomfyTahomaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaBindingConstants {

    public static final String BINDING_ID = "somfytahoma";

    // Things
    // Bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // Gateway
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    // Roller Shutter
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");

    // Silent Roller Shutter
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER_SILENT = new ThingTypeUID(BINDING_ID,
            "rollershutter_silent");

    // Screen
    public static final ThingTypeUID THING_TYPE_SCREEN = new ThingTypeUID(BINDING_ID, "screen");

    // Venetian Blind
    public static final ThingTypeUID THING_TYPE_VENETIANBLIND = new ThingTypeUID(BINDING_ID, "venetianblind");

    // Exterior Screen
    public static final ThingTypeUID THING_TYPE_EXTERIORSCREEN = new ThingTypeUID(BINDING_ID, "exteriorscreen");

    // Exterior Venetian Blind
    public static final ThingTypeUID THING_TYPE_EXTERIORVENETIANBLIND = new ThingTypeUID(BINDING_ID,
            "exteriorvenetianblind");

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
    public static final ThingTypeUID THING_TYPE_INTERNAL_ALARM = new ThingTypeUID(BINDING_ID, "internalalarm");
    public static final ThingTypeUID THING_TYPE_EXTERNAL_ALARM = new ThingTypeUID(BINDING_ID, "externalalarm");

    // Pod
    public static final ThingTypeUID THING_TYPE_POD = new ThingTypeUID(BINDING_ID, "pod");

    // Heating system
    public static final ThingTypeUID THING_TYPE_HEATING_SYSTEM = new ThingTypeUID(BINDING_ID, "heatingsystem");
    public static final ThingTypeUID THING_TYPE_ONOFF_HEATING_SYSTEM = new ThingTypeUID(BINDING_ID, "onoffheatingsystem");

    // Door lock
    public static final ThingTypeUID THING_TYPE_DOOR_LOCK = new ThingTypeUID(BINDING_ID, "doorlock");

    // Pergola
    public static final ThingTypeUID THING_TYPE_PERGOLA = new ThingTypeUID(BINDING_ID, "pergola");

    // Window handle
    public static final ThingTypeUID THING_TYPE_WINDOW_HANDLE = new ThingTypeUID(BINDING_ID, "windowhandle");

    // Temperature sensor
    public static final ThingTypeUID THING_TYPE_TEMPERATURESENSOR = new ThingTypeUID(BINDING_ID, "temperaturesensor");

    // Gate
    public static final ThingTypeUID THING_TYPE_GATE = new ThingTypeUID(BINDING_ID, "gate");

    // List of all Channel ids
    // Gateway
    public static final String STATUS = "status";

    // Roller shutter, Awning, Screen, Blind, Garage door, Window
    public static final String CONTROL = "control";

    // Silent roller shutter
    public static final String CONTROL_SILENT = "control_silent";

    // Blind
    public static final String ORIENTATION = "orientation";

    // Action group
    public static final String EXECUTE_ACTION = "execute_action";

    // OnOff, Light
    public static final String SWITCH = "switch";

    // Door lock
    public static final String LOCK = "lock";
    public static final String OPEN = "open";

    // Smoke sensor, Occupancy sensor, Contact sensor
    public static final String CONTACT = "contact";

    // Light sensor
    public static final String LUMINANCE = "luminance";

    // Temperature sensor
    public static final String TEMPERATURE = "temperature";

    // Alarm
    public static final String ALARM_COMMAND = "alarm_command";
    public static final String ALARM_STATE = "alarm_state";
    public static final String TARGET_ALARM_STATE = "target_alarm_state";
    public static final String INTRUSION_CONTROL = "intrusion_control";
    public static final String INTRUSION_STATE = "intrusion_state";

    // Heating system
    public static final String TARGET_TEMPERATURE = "target_temperature";
    public static final String CURRENT_TEMPERATURE = "current_temperature";
    public static final String CURRENT_STATE = "current_state";
    public static final String BATTERY_LEVEL = "battery_level";
    public static final String TARGET_HEATING_LEVEL = "target_heating_level";

    // Window handle
    public static final String HANDLE_STATE = "handle_state";

    // Gate
    public static final String GATE_STATE = "gate_state";
    public static final String GATE_COMMAND = "gate_command";

    //Constants
    private static final String API_URL = "https://www.tahomalink.com/enduser-mobile-web/";
    public static final String TAHOMA_URL = API_URL + "externalAPI/json/";
    public static final String TAHOMA_EVENT_URL = API_URL + "enduserAPI/events/";
    public static final String SETUP_URL = API_URL + "enduserAPI/setup/gateways/";
    public static final String REFRESH_URL = API_URL + "enduserAPI/setup/devices/states/refresh";
    public static final String EXEC_URL = API_URL + "enduserAPI/exec/";
    public static final String DELETE_URL = EXEC_URL + "current/setup/";
    public static final String TAHOMA_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
    public static final int TAHOMA_TIMEOUT = 5;
    public static final String UNAUTHORIZED = "Not logged in";
    public static final int TYPE_PERCENT = 1;
    public static final int TYPE_DECIMAL = 2;
    public static final int TYPE_STRING = 3;
    public static final String COMMAND_MY = "my";
    public static final String COMMAND_SET_CLOSURE = "setClosure";
    public static final String COMMAND_SET_DEPLOYMENT = "setDeployment";
    public static final String COMMAND_SET_ORIENTATION = "setOrientation";
    public static final String COMMAND_SET_CLOSURESPEED = "setClosureAndLinearSpeed";
    public static final String COMMAND_SET_HEATINGLEVEL = "setHeatingLevel";
    public static final String COMMAND_SET_PEDESTRIANPOSITION = "setPedestrianPosition";
    public static final String COMMAND_REFRESH_HEATINGLEVEL = "refreshHeatingLevel";
    public static final String COMMAND_UP = "up";
    public static final String COMMAND_DOWN = "down";
    public static final String COMMAND_OPEN = "open";
    public static final String COMMAND_CLOSE = "close";
    public static final String COMMAND_STOP = "stop";
    public static final String COMMAND_OFF = "off";
    public static final String STATUS_STATE = "core:StatusState";
    public static final String UNAVAILABLE = "unavailable";
    public static final String AUTHENTICATION_CHALLENGE = "HTTP protocol violation: Authentication challenge without WWW-Authenticate header";

    // supported uiClasses
    public static final String ROLLERSHUTTER = "RollerShutter";
    public static final String SCREEN = "Screen";
    public static final String VENETIANBLIND = "VenetianBlind";
    public static final String EXTERIORSCREEN = "ExteriorScreen";
    public static final String EXTERIORVENETIANBLIND = "ExteriorVenetianBlind";
    public static final String GARAGEDOOR = "GarageDoor";
    public static final String AWNING = "Awning";
    public static final String ONOFF = "OnOff";
    public static final String LIGHT = "Light";
    public static final String LIGHTSENSOR = "LightSensor";
    public static final String SMOKESENSOR = "SmokeSensor";
    public static final String CONTACTSENSOR = "ContactSensor";
    public static final String OCCUPANCYSENSOR = "OccupancySensor";
    public static final String WINDOW = "Window";
    public static final String ALARM = "Alarm";
    public static final String POD = "Pod";
    public static final String HEATINGSYSTEM = "HeatingSystem";
    public static final String DOORLOCK = "DoorLock";
    public static final String PERGOLA = "Pergola";
    public static final String WINDOWHANDLE = "WindowHandle";
    public static final String TEMPERATURESENSOR = "TemperatureSensor";
    public static final String GATE = "Gate";

    // unsupported uiClasses
    public static final String PROTOCOLGATEWAY = "ProtocolGateway";
    public static final String REMOTECONTROLLER = "RemoteController";
    public static final String NETWORKCOMPONENT = "NetworkComponent";

    // cache timeout
    public static final int CACHE_EXPIRY = 10000;
}
