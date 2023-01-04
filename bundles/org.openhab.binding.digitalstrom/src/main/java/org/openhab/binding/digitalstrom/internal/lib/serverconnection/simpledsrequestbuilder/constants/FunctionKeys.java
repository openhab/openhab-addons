/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.serverconnection.simpledsrequestbuilder.constants;

/**
 * The {@link FunctionKeys} contains digitalSTROM-JSON function keys.
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public class FunctionKeys {

    public static final String CALL_SCENE = "callScene";
    public static final String SAVE_SCENE = "saveScene";
    public static final String LOGIN = "login";
    public static final String LOGOUT = "logout";
    public static final String UNDO_SCENE = "undoScene";
    public static final String TURN_ON = "turnOn";
    public static final String TURN_OFF = "turnOff";
    public static final String INCREASE_VALUE = "increaseValue";
    public static final String DECREASE_VALUE = "decreaseValue";
    public static final String GET_STRUCTURE = "getStructure";
    public static final String GET_DEVICES = "getDevices";
    public static final String GET_CIRCUITS = "getCircuits";
    public static final String LOGIN_APPLICATION = "loginApplication";
    public static final String GET_NAME = "getName";
    public static final String SET_NAME = "setName";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String GET = "get";
    public static final String SET_VALUE = "setValue";
    public static final String GET_CONSUMPTION = "getConsumption";
    public static final String RESCAN = "rescan";
    public static final String SCENE_SET_NAME = "sceneSetName";
    public static final String SCENE_GET_NAME = "sceneGetName";
    public static final String PUSH_SENSOR_VALUES = "pushSensorValues";
    public static final String GET_REACHABLE_SCENES = "getReachableScenes";
    public static final String GET_STATE = "getState";
    public static final String GET_GROUPS = "getGroups";
    public static final String GET_ENERGY_METER_VALUE = "getEnergyMeterValue";
    public static final String GET_STRING = "getString";
    public static final String GET_INTEGER = "getInteger";
    public static final String GET_BOOLEAN = "getBoolean";
    public static final String SET_STRING = "setString";
    public static final String SET_INTEGER = "setInteger";
    public static final String SET_BOOLEAN = "setBoolean";
    public static final String GET_CHILDREN = "getChildren";
    public static final String SET_FLAG = "setFlag";
    public static final String GET_FLAGS = "getFlags";
    public static final String QUERY = "query";
    public static final String REMOVE = "remove";
    public static final String GET_TYPE = "getType";
    public static final String GET_SPEC = "getSpec";
    public static final String VERSION = "version";
    public static final String TIME = "time";
    public static final String FROM_APARTMENT = "fromApartment";
    public static final String BY_ZONE = "byZone";
    public static final String BY_GROUP = "byGroup";
    public static final String BY_DSID = "byDSID";
    public static final String ADD = "add";
    public static final String SUBTRACT = "subtract";
    public static final String LOGGED_IN_USER = "loggedInUser";
    public static final String ZONE_ADD_DEVICE = "zoneAddDevice";
    public static final String ADD_ZONE = "addZone";
    public static final String REMOVE_ZONE = "removeZone";
    public static final String REMOVE_DEVICE = "removeDevice";
    public static final String PERSIST_SET = "persistSet";
    public static final String UNPERSIST_SET = "unpersistSet";
    public static final String ADD_GROUP = "addGroup";
    public static final String GROUP_ADD_DEVICE = "groupAddDevice";
    public static final String GROUP_REMOVE_DEVICE = "groupRemoveDevice";
    public static final String GET_RESOLUTIONS = "getResolutions";
    public static final String GET_SERIES = "getSeries";
    public static final String GET_VALUES = "getValues";
    public static final String GET_LATEST = "getLatest";
    public static final String ADD_TAG = "addTag";
    public static final String REMOVE_TAG = "removeTag";
    public static final String HAS_TAG = "hasTag";
    public static final String GET_TAGS = "getTags";
    public static final String LOCK = "lock";
    public static final String UNLOCK = "unlock";
    public static final String GET_SENSOR_EVENT_TABLE_ENTRY = "getSensorEventTableEntry";
    public static final String SET_SENSOR_EVENT_TABLE_ENTRY = "setSensorEventTableEntry";
    public static final String ADD_TO_AREA = "addToArea";
    public static final String REMOVE_FROM_AREA = "removeFromArea";
    public static final String SET_CONFIG = "setConfig";
    public static final String GET_CONFIG = "getConfig";
    public static final String GET_CONFIG_WORD = "getConfigWord";
    public static final String SET_JOKER_GROUP = "setJokerGroup";
    public static final String SET_BUTTON_ID = "setButtonID";
    public static final String SET_BUTTON_INPUT_MODE = "setButtonInputMode";
    public static final String SET_OUTPUT_MODE = "setOutputMode";
    public static final String SET_PROG_MODE = "setProgMode";
    public static final String GET_OUTPUT_VALUE = "getOutputValue";
    public static final String SET_OUTPUT_VALUE = "setOutputValue";
    public static final String GET_SCENE_MODE = "getSceneMode";
    public static final String SET_SCENE_MODE = "setSceneMode";
    public static final String GET_TRANSITION_TIME = "getTransitionTime";
    public static final String SET_TRANSITION_TIME = "setTransitionTime";
    public static final String GET_LED_MODE = "getLedMode";
    public static final String SET_LED_MODE = "setLedMode";
    public static final String GET_SENSOR_VALUE = "getSensorValue";
    public static final String GET_SENSOR_TYPE = "getSensorType";
    public static final String GET_DSID = "getDSID";
    public static final String ENABLE_APPLICATION_TOKEN = "enableToken";
    public static final String REQUEST_APPLICATION_TOKEN = "requestApplicationToken";
    public static final String REVOKE_TOKEN = "revokeToken";
    public static final String GET_SCENE_VALUE = "getSceneValue";
    public static final String GET_TEMPERATURE_CONTROL_STATUS = "getTemperatureControlStatus";
    public static final String GET_TEMPERATURE_CONTROL_CONFIG = "getTemperatureControlConfig";
    public static final String GET_TEMPERATURE_CONTROL_VALUES = "getTemperatureControlValues";
    public static final String GET_ASSIGNED_SENSORS = "getAssignedSensors";
    public static final String SET_TEMEPERATURE_CONTROL_STATE = "setTemperatureControlState";
    public static final String SET_TEMEPERATURE_CONTROL_VALUE = "setTemperatureControlValue";
    public static final String GET_SENSOR_VALUES = "getSensorValues";
    public static final String SET_SENSOR_SOURCE = "setSensorSource";
    public static final String GET_TEMPERATURE_CONTROL_INTERNALS = "getTemperatureControlInternals";
    public static final String SET_TEMPERATION_CONTROL_CONFIG = "setTemperatureControlConfig";
    public static final String QUERY2 = "query2";
    public static final String BLINK = "blink";
    public static final String PUSH_SENSOR_VALUE = "pushSensorValue";
}
