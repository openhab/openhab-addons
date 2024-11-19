/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

    public static final String REACHABLE = "isReachable";
    public static final String ATTRIBUTES = "attributes";
    public static final String CAPABILITIES = "capabilities";
    public static final String PROPERTY_CAN_RECEIVE = "canReceive";
    public static final String PROPERTY_CAN_SEND = "canSend";
    public static final String SCENES = "scenes";
    public static final String CUSTOM_NAME = "customName";
    public static final String DEVICE_MODEL = "model";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_RELATION_ID = "relationId";

    public static final String COLOR_TEMPERATURE_CAPABILITY = "colorTemperature";

    public static final String TEMPLATE_LIGHT_PRESET_BRIGHT = "/json/light-presets/bright.json";
    public static final String TEMPLATE_LIGHT_PRESET_SLOWDOWN = "/json/light-presets/slowdown.json";
    public static final String TEMPLATE_LIGHT_PRESET_SMOOTH = "/json/light-presets/smooth.json";
    public static final String TEMPLATE_LIGHT_PRESET_WARM = "/json/light-presets/warm.json";
    public static final String TEMPLATE_SENSOR_ALWQAYS_ON = "/json/sensor-config/always-on.json";
    public static final String TEMPLATE_SENSOR_DURATION_UPDATE = "/json/sensor-config/duration-update.json";
    public static final String TEMPLATE_SENSOR_FOLLOW_SUN = "/json/sensor-config/follow-sun.json";
    public static final String TEMPLATE_SENSOR_SCHEDULE_ON = "/json/sensor-config/schedule-on.json";
    public static final String TEMPLATE_CLICK_SCENE = "/json/scenes/click-scene.json";
    public static final String TEMPLATE_COORDINATES = "/json/gateway/coordinates.json";
    public static final String TEMPLATE_NULL_COORDINATES = "/json/gateway/null-coordinates.json";

    /**
     * Get structure model as JSON String.
     *
     * @see json channel
     * @return JSON String
     */
    public String getModelString();

    /**
     * Model update will be performed with API request. Relative expensive operation depending on number of connected
     * devices. Call triggers
     * - startup
     * - add / remove device to DIRIGERA gateway, not openHAB
     * - custom name changes for Discovery updates
     */
    public int update();

    /**
     * Starts a new detection without model update. If handlers are removed they shall appear in discovery again.
     */
    public void detection();

    /**
     * Get all id's for a specific type. Used to identify link candidates for a specific device.
     * - LightController needs lights and plugs and vice versa
     * - BlindController needs blinds and vice versa
     * - SoundController needs speakers and vice versa
     *
     * @param types as list of types to query
     * @return list of matching device id's
     */
    public List<String> getDevicesForTypes(List<String> types);

    /**
     * Returns a list of all device id's.
     *
     * @return list of all connected devices
     */
    public List<String> getAllDeviceIds();

    /**
     * Returns a list with resolved relation id's. There are complex device registering more than one id with different
     * type. This binding combines them in one handler.
     * - MotionLightHandler
     * - DoubleShortcutControllerHandler
     *
     * @return list of device id's without related devices
     */
    public List<String> getResolvedDeviceList();

    /**
     * Get all stored information for one device or scene.
     *
     * @param id to query
     * @param type device or scene
     * @return data as JSON
     */
    public JSONObject getAllFor(String id, String type);

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
    public TreeMap<String, String> getRelations(String relationId);

    /**
     * Get relationId for a given device id
     *
     * @param id to check
     * @return same id if no relations are found or relationId
     */
    public String getRelationId(String id);

    /**
     * Identify device which is present in model with openHAB ThingTypeUID.
     *
     * @param id to identify
     * @return ThingTypeUID
     */
    public ThingTypeUID identifyDeviceFromModel(String id);

    /**
     * Check if given id is present in devices or scenes.
     *
     * @param id to check
     * @return true if id is found
     */
    public boolean has(String id);

    /**
     * Get the custom name configured in IKEA Smart home app.
     *
     * @param id to query
     * @return name as String
     */
    public String getCustonNameFor(String id);

    /**
     * Properties Map for Discovery
     *
     * @param id to query
     * @return Map with attributes for Thing properties
     */
    public Map<String, Object> getPropertiesFor(String id);

    /**
     * Read a resource file from this bundle. Some presets and commands sent to API shall not be implemented
     * in code if they are just needing minor String replacements.
     * Root path in project is src/main/resources. Line breaks and white spaces will
     *
     * @return
     */
    public String getTemplate(String name);
}
