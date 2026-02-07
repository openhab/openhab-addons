/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.interfaces;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Model} is representing the structural data of the gateway. Concrete values e.g. temperature of devices
 * shall not be accessed.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface Model {

    // Constants for JSON keys and values from DIRIGERA API
    static final String MODEL_KEY_DEVICES = "devices";
    static final String MODEL_KEY_SCENES = "scenes";

    static final String JSON_KEY_TYPE = "type";
    static final String JSON_KEY_DEVICE_TYPE = "deviceType";
    static final String JSON_KEY_DEVICE_ID = "id";
    static final String JSON_KEY_REACHABLE = "isReachable";
    static final String JSON_KEY_ATTRIBUTES = "attributes";
    static final String JSON_KEY_RELATION_ID = "relationId";
    static final String JSON_KEY_CAPABILITIES = "capabilities";

    static final String TYPE_USER_SCENE = "userScene";
    static final String TYPE_CUSTOM_SCENE = "customScene";
    static final String TYPE_CONTROLLER = "controller";
    static final String TYPE_SENSOR = "sensor";

    static final String DEVICE_TYPE_GATEWAY = "gateway";
    static final String DEVICE_TYPE_SPEAKER = "speaker";
    static final String DEVICE_TYPE_REPEATER = "repeater";
    static final String DEVICE_TYPE_AIR_PURIFIER = "airPurifier";
    static final String DEVICE_TYPE_BLINDS = "blinds";
    static final String DEVICE_TYPE_LIGHT = "light";
    static final String DEVICE_TYPE_MOTION_SENSOR = "motionSensor";
    static final String DEVICE_TYPE_LIGHT_SENSOR = "lightSensor";
    static final String DEVICE_TYPE_CONTACT_SENSOR = "openCloseSensor";
    static final String DEVICE_TYPE_ENVIRONMENT_SENSOR = "environmentSensor";
    static final String DEVICE_TYPE_WATER_SENSOR = "waterSensor";
    static final String DEVICE_TYPE_OUTLET = "outlet";
    static final String DEVICE_TYPE_OCCUPANCY_SENSOR = "occupancySensor";
    static final String DEVICE_TYPE_GENERIC_SWITCH = "genericSwitch";
    static final String DEVICE_TYPE_LIGHT_CONTROLLER = "lightController";
    static final String DEVICE_TYPE_BLIND_CONTROLLER = "blindsController";
    static final String DEVICE_TYPE_SOUND_CONTROLLER = "soundController";
    static final String DEVICE_TYPE_SHORTCUT_CONTROLLER = "shortcutController";

    static final String CAPABILITIES_KEY_CAN_RECEIVE = "canReceive";
    static final String CAPABILITIES_KEY_CAN_SEND = "canSend";
    static final String CAPABILITIES_VALUE_COLOR_TEMPERATURE = "colorTemperature";

    static final String ATTRIBUTES_KEY_CUSTOM_NAME = "customName";
    static final String ATTRIBUTES_KEY_DEVICE_MODEL = "model";
    static final String ATTRIBUTES_KEY_QRCODE = "qrCode";
    static final String ATTRIBUTES_KEY_COLOR_TEMPERATURE = "colorTemperature";
    static final String ATTRIBUTES_KEY_OTA_STATUS = "otaStatus";
    static final String ATTRIBUTES_KEY_OTA_STATE = "otaState";
    static final String ATTRIBUTES_KEY_OTA_PROGRESS = "otaProgress";
    static final String ATTRIBUTES_KEY_BATTERY_PERCENTAGE = "batteryPercentage";
    static final String ATTRIBUTES_KEY_PERMIT_JOIN = "permittingJoin";
    static final String ATTRIBUTES_KEY_STARTUP_BEHAVIOR = "startupOnOff";
    static final String ATTRIBUTES_KEY_POWER_STATE = "isOn";
    static final String ATTRIBUTES_KEY_REMOTE_LINKS = "remoteLinks";
    static final String ATTRIBUTES_KEY_COLOR_MODE = "colorMode";
    static final String ATTRIBUTES_KEY_CONTROL_MODE = "controlMode";

    // Websocket update types
    static final String EVENT_TYPE_DEVICE_DISCOVERED = "deviceDiscovered";
    static final String EVENT_TYPE_DEVICE_ADDED = "deviceAdded";
    static final String EVENT_TYPE_DEVICE_CHANGE = "deviceStateChanged";
    static final String EVENT_TYPE_DEVICE_REMOVED = "deviceRemoved";
    static final String EVENT_TYPE_REMOTE_PRESS = "remotePressEvent";
    static final String EVENT_TYPE_SCENE_CREATED = "sceneCreated";
    static final String EVENT_TYPE_SCENE_UPDATE = "sceneUpdated";
    static final String EVENT_TYPE_SCENE_DELETED = "sceneDeleted";

    static final String EVENT_KEY_CLICK_PATTER = "clickPattern";

    static final String TEMPLATE_LIGHT_PRESET_BRIGHT = "/json/light-presets/bright.json";
    static final String TEMPLATE_LIGHT_PRESET_SLOWDOWN = "/json/light-presets/slowdown.json";
    static final String TEMPLATE_LIGHT_PRESET_SMOOTH = "/json/light-presets/smooth.json";
    static final String TEMPLATE_LIGHT_PRESET_WARM = "/json/light-presets/warm.json";
    static final String TEMPLATE_SENSOR_ALWQAYS_ON = "/json/sensor-config/always-on.json";
    static final String TEMPLATE_SENSOR_DURATION_UPDATE = "/json/sensor-config/duration-update.json";
    static final String TEMPLATE_SENSOR_FOLLOW_SUN = "/json/sensor-config/follow-sun.json";
    static final String TEMPLATE_SENSOR_SCHEDULE_ON = "/json/sensor-config/schedule-on.json";
    static final String TEMPLATE_CLICK_SCENE = "/json/scenes/click-scene.json";
    static final String TEMPLATE_COORDINATES = "/json/gateway/coordinates.json";
    static final String TEMPLATE_NULL_COORDINATES = "/json/gateway/null-coordinates.json";

    /**
     * Get structure model as JSON String.
     *
     * @see json channel
     * @return JSON String
     */
    String getModelString();

    /**
     * Model update will be performed with API request. Relative expensive operation depending on number of connected
     * devices. Call triggers
     * - startup
     * - add / remove device to DIRIGERA gateway, not openHAB
     * - custom name changes for Discovery updates
     */
    int update();

    /**
     * Starts a new detection without model update. If handlers are removed they shall appear in discovery again.
     */
    void detection();

    /**
     * Get all id's for a specific type. Used to identify link candidates for a specific device.
     * - LightController needs lights and plugs and vice versa
     * - BlindController needs blinds and vice versa
     * - SoundController needs speakers and vice versa
     *
     * @param types as list of types to query
     * @return list of matching device id's
     */
    List<String> getDevicesForTypes(List<String> types);

    /**
     * Returns a list of all device id's.
     *
     * @return list of all connected devices
     */
    List<String> getAllDeviceIds();

    /**
     * Returns a list with resolved relation id's. There are complex device registering more than one id with different
     * type. This binding combines them in one handler.
     * - MotionLightHandler
     * - DoubleShortcutControllerHandler
     *
     * @return list of device id's without related devices
     */
    List<String> getResolvedDeviceList();

    /**
     * Get all stored information for one device or scene.
     *
     * @param id to query
     * @param type device or scene
     * @return data as JSON
     */
    JSONObject getAllFor(String id, String type);

    /**
     * Gets all relations marked into relationId property
     * Rationale:
     * VALLHORN Motion Sensor registers 2 devices
     * - Motion Sensor
     * - Light Sensor
     *
     * Shortcut Controller with 2 buttons registers 2 controllers
     * They shall not be splitted in 2 different things so one Thing shall receive updates for both id's
     *
     * Use TreeMap to sort device id's so suffix _1 comes before _2
     *
     * @param relationId
     * @return List of id's with same serial number
     */
    TreeMap<String, String> getRelations(String relationId);

    /**
     * Get relationId for a given device id
     *
     * @param id to check
     * @return same id if no relations are found or relationId
     */
    String getRelationId(String id);

    /**
     * Get device type for a given device id
     *
     * @param id to query
     * @return device type as String
     */
    String getDeviceType(String id);

    /**
     * Identify device which is present in model with openHAB ThingTypeUID.
     *
     * @param id to identify
     * @return ThingTypeUID
     */
    ThingTypeUID identifyDeviceFromModel(String id);

    /**
     * Check if given id is present in devices or scenes.
     *
     * @param id to check
     * @return true if id is found
     */
    boolean has(String id);

    /**
     * Get the custom name configured in IKEA Smart home app.
     *
     * @param id to query
     * @return name as String
     */
    String getCustonNameFor(String id);

    /**
     * Properties Map for Discovery
     *
     * @param id to query
     * @return Map with attributes for Thing properties
     */
    Map<String, Object> getPropertiesFor(String id);
}
