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
package org.openhab.binding.dirigera.internal.model;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryResult;
import org.openhab.binding.dirigera.internal.exception.ModelUpdateException;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Model} is the central instance identifying devices and
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Model {
    private final Logger logger = LoggerFactory.getLogger(Model.class);
    private Map<String, DirigeraDiscoveryResult> resultMap = new HashMap<>();
    public static final String REACHABLE = "isReachable";
    public static final String ATTRIBUTES = "attributes";
    public static final String SCENES = "scenes";
    public static final String CUSTOM_NAME = "customName";
    public static final String DEVICE_MODEL = "model";
    public static final String DEVICE_TYPE = "deviceType";

    private JSONObject model = new JSONObject();
    private Gateway gateway;

    public Model(Gateway gateway) {
        this.gateway = gateway;
    }

    /**
     * only used for unit testing
     */
    // public synchronized void update(String modelString) {
    // this.model = new JSONObject(modelString);
    // }

    public synchronized void init() {
        try {
            JSONObject home = gateway.api().readHome();
            if (home.has(PROPERTY_HTTP_ERROR_STATUS)) {
                logger.warn("DIRIGERA MODEL received model with error code {} - don't take it",
                        home.get(PROPERTY_HTTP_ERROR_STATUS));
            } else {
                model = home;
                // first get devices
                List<String> foundDevices = getAllDeviceIds();
                List<String> foundScenes = getAllSceneIds();
                foundDevices.addAll(foundScenes);
                /**
                 * at init phase check for each device
                 * - is it new in the tree
                 * - is it knwon by handler from persistence
                 */
                foundDevices.forEach(deviceId -> {
                    addedDeviceScene(deviceId);
                });
            }
        } catch (Throwable t) {
            logger.error("Excpetion during model update {}", t.getMessage());
            throw new ModelUpdateException("Excpetion during model update " + t.getMessage());
        }
    }

    public synchronized List<String> getAllDeviceIds() {
        List<String> deviceList = new ArrayList<>();
        if (!model.isNull(PROPERTY_DEVICES)) {
            JSONArray devices = model.getJSONArray("devices");
            Iterator<Object> entries = devices.iterator();
            while (entries.hasNext()) {
                JSONObject entry = (JSONObject) entries.next();
                deviceList.add(entry.getString(PROPERTY_DEVICE_ID));
            }
        }
        return deviceList;
    }

    private List<String> getAllSceneIds() {
        List<String> sceneList = new ArrayList<>();
        if (!model.isNull(SCENES)) {
            JSONArray scenes = model.getJSONArray(SCENES);
            Iterator<Object> sceneIterator = scenes.iterator();
            while (sceneIterator.hasNext()) {
                JSONObject entry = (JSONObject) sceneIterator.next();

                if (entry.has(PROPERTY_DEVICE_ID)) {
                    String id = entry.getString(PROPERTY_DEVICE_ID);
                    sceneList.add(id);
                    // if (entry.has("info")) {
                    // JSONObject info = entry.getJSONObject("info");
                    // if (info.has("name")) {
                    // gateway.newScene(id, info.getString("name"));
                    // }
                    // }
                }
            }
        }
        return sceneList;
    }

    public void addedDeviceScene(String id) {
        if (gateway.discoverEnabled()) {
            DirigeraDiscoveryResult result = identifiy(id);
            if (result.result.isPresent()) {
                if (!result.isKnown) {
                    if (!result.isDelivered) {
                        gateway.discovery().thingDiscovered(result.result.get());
                        result.isDelivered = true;
                        resultMap.put(id, result);
                    } // don't deliver because result is present in map
                } // don't deliver because handler is present
            } else {
                logger.info("DIRIGERA MODEL No DiscoveryResult created for {}", id);
            }
        } else {
            logger.trace("DIRIGERA MODEL Discovery disabled");
        }
    }

    public void updateDeviceScene(String id) {
        if (gateway.discoverEnabled()) {
            DirigeraDiscoveryResult deliveredResult = resultMap.get(id);
            if (deliveredResult != null) {
                // check for name update
                String previousName = deliveredResult.result.get().getLabel();
                String currentName = getCustonNameFor(id);
                if (!currentName.equals(previousName)) {
                    logger.trace("DIRIGERA MODEL Name update detected from {} to {}", previousName, currentName);
                    removedDeviceScene(id);
                    addedDeviceScene(id);
                }
            }
        }
    }

    public void removedDeviceScene(String id) {
        if (gateway.discoverEnabled()) {
            DirigeraDiscoveryResult deliveredResult = resultMap.get(id);
            if (deliveredResult != null) {
                gateway.discovery().thingRemoved(deliveredResult.result.get());
            }
        } else {
            logger.trace("DIRIGERA MODEL Discovery disabled");
        }
    }

    public synchronized JSONArray getIdsForType(String type) {
        JSONArray returnArray = new JSONArray();
        if (!model.isNull(PROPERTY_DEVICES)) {
            JSONArray devices = model.getJSONArray(PROPERTY_DEVICES);
            Iterator<Object> entries = devices.iterator();
            while (entries.hasNext()) {
                JSONObject entry = (JSONObject) entries.next();
                if (!entry.isNull(PROPERTY_DEVICE_TYPE) && !entry.isNull(PROPERTY_DEVICE_ID)) {
                    if (type.equals(entry.get(PROPERTY_DEVICE_TYPE))) {
                        returnArray.put(entry.get(PROPERTY_DEVICE_ID));
                    }
                }
            }
        }
        return returnArray;
    }

    /**
     * Crucial function to identify DeviceHandler - all options needs to be listed here to deliver the right
     * ThingTypeUID
     *
     * @param DIRIGERA Id
     * @return ThingTypeUID
     */
    public synchronized ThingTypeUID identifyDevice(String id) {
        JSONObject entry = getAllFor(id, PROPERTY_DEVICES);
        if (!entry.isNull(PROPERTY_DEVICE_TYPE)) {
            String deviceType = entry.getString(PROPERTY_DEVICE_TYPE);
            JSONObject attributes = entry.getJSONObject(PROPERTY_ATTRIBUTES);
            switch (deviceType) {
                case DEVICE_TYPE_GATEWAY:
                    return THING_TYPE_GATEWAY;
                case DEVICE_TYPE_LIGHT:
                    if (attributes.has(ATTRIBUTE_COLOR_MODE)) {
                        String colorMode = attributes.getString(ATTRIBUTE_COLOR_MODE);
                        switch (colorMode) {
                            case "color":
                                return THING_TYPE_COLOR_LIGHT;
                            case "temperature":
                                return THING_TYPE_TEMPERATURE_LIGHT;
                        }
                    }
                    break;
                case DEVICE_TYPE_MOTION_SENSOR:
                    // if product code is E2134 (VALLHORN) sensor contains an additional light sensor!
                    String motionSensorProductCode = getStringAttribute(id, "productCode");
                    if ("E2134".equals(motionSensorProductCode)) {
                        return THING_TYPE_MOTION_LIGHT_SENSOR;
                    } else {
                        return THING_TYPE_MOTION_SENSOR;
                    }
                case DEVICE_TYPE_LIGHT_SENSOR:
                    return THING_TYPE_LIGHT_SENSOR;
                case DEVICE_TYPE_CONTACT_SENSOR:
                    return THING_TYPE_CONTACT_SENSOR;
                case DEVICE_TYPE_OUTLET:
                    // if product code is E2206 (INSPELNING) plug contains an additional light sensor!
                    String pluGroductCode = getStringAttribute(id, "productCode");
                    if ("E2206".equals(pluGroductCode)) {
                        return THING_TYPE_SMART_PLUG;
                    } else {
                        return THING_TYPE_PLUG;
                    }
                case DEVICE_TYPE_SPEAKER:
                    return THING_TYPE_SPEAKER;
                case DEVICE_TYPE_REPEATER:
                    return THING_TYPE_REPEATER;
                case DEVICE_TYPE_LIGHT_CONTROLLER:
                    return THING_TYPE_LIGHT_CONTROLLER;
                case DEVICE_TYPE_ENVIRONMENT_SENSOR:
                    return THING_TYPE_AIR_QUALITY;
                case DEVICE_TYPE_WATER_SENSOR:
                    return THING_TYPE_WATER_SENSOR;
                default:
                    logger.info("DIRIGERA MODEL Unsuppoerted Device {} with attributes {}", deviceType, attributes);
            }
        }
        // no device discovred yet - check scenes
        entry = getAllFor(id, PROPERTY_SCENES);
        if (!entry.isEmpty()) {
            return THING_TYPE_SCENE;
        }
        return THING_TYPE_UNKNNOWN;
    }

    private String getStringAttribute(String id, String attribute) {
        String attributeValue = "";
        JSONObject deviceObject = getAllFor(id, PROPERTY_DEVICES);
        if (deviceObject.has(ATTRIBUTES)) {
            JSONObject attributes = deviceObject.getJSONObject(ATTRIBUTES);
            if (attributes.has(attribute)) {
                attributeValue = attributes.getString(attribute);
            }
        }
        return attributeValue;
    }

    public synchronized JSONObject getAllFor(String id, String type) {
        JSONObject returnObject = new JSONObject();
        if (model.has(type)) {
            JSONArray devices = model.getJSONArray(type);
            Iterator<Object> entries = devices.iterator();
            while (entries.hasNext()) {
                JSONObject entry = (JSONObject) entries.next();
                if (id.equals(entry.get(PROPERTY_DEVICE_ID))) {
                    return entry;
                }
            }
        }
        return returnObject;
    }

    public synchronized String getCustonNameFor(String id) {
        JSONObject deviceObject = getAllFor(id, PROPERTY_DEVICES);
        if (deviceObject.has(ATTRIBUTES)) {
            JSONObject attributes = deviceObject.getJSONObject(ATTRIBUTES);
            if (attributes.has(CUSTOM_NAME)) {
                String customName = attributes.getString(CUSTOM_NAME);
                if (!customName.isBlank()) {
                    return customName;
                }
            }
            if (attributes.has(DEVICE_MODEL)) {
                String deviceModel = attributes.getString(DEVICE_MODEL);
                if (!deviceModel.isBlank()) {
                    return deviceModel;
                }
            }
            if (deviceObject.has(DEVICE_TYPE)) {
                return deviceObject.getString(DEVICE_TYPE);
            }
            // 3 fallback options
        }
        // not found yet - check scenes
        JSONObject sceneObject = getAllFor(id, PROPERTY_SCENES);
        if (sceneObject.has("info")) {
            JSONObject info = sceneObject.getJSONObject("info");
            if (info.has("name")) {
                String name = info.getString("name");
                if (!name.isBlank()) {
                    return name;
                }
            }
        }

        return id;
    }

    public synchronized Map<String, Object> getPropertiesFor(String id) {
        final Map<String, Object> properties = new HashMap<>();
        JSONObject deviceObject = getAllFor(id, PROPERTY_DEVICES);
        if (deviceObject.has(ATTRIBUTES)) {
            JSONObject attributes = deviceObject.getJSONObject(ATTRIBUTES);
            THING_PROPERTIES.forEach(property -> {
                if (attributes.has(property)) {
                    properties.put(property, attributes.get(property));
                }
            });
        }
        properties.put(PROPERTY_DEVICE_ID, id);
        return properties;
    }

    public synchronized List<String> getTwins(String id) {
        final List<String> twins = new ArrayList<>();
        String serialNumber = getStringAttribute(id, "serialNumber");
        List<String> allDevices = getAllDeviceIds();
        allDevices.forEach(deviceId -> {
            if (!id.equals(deviceId)) {
                String investigateSerialNumber = getStringAttribute(deviceId, "serialNumber");
                if (!investigateSerialNumber.isBlank() && serialNumber.equals(investigateSerialNumber)) {
                    twins.add(deviceId);
                    logger.info("DIRIGERA MODEL twin {} found for {}", deviceId, id);
                }
            }
        });
        return twins;
    }

    private DirigeraDiscoveryResult identifiy(String id) {
        ThingTypeUID ttuid = identifyDevice(id);
        String customName = getCustonNameFor(id);
        Map<String, Object> propertiesMap = getPropertiesFor(id);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                .create(new ThingUID(ttuid, gateway.getThing().getUID(), id)).withBridge(gateway.getThing().getUID())
                .withProperties(propertiesMap).withRepresentationProperty(PROPERTY_DEVICE_ID).withLabel(customName)
                .build();
        boolean isDelivered = resultMap.containsKey(id);
        boolean isKnown = gateway.isKnownDevice(id);

        DirigeraDiscoveryResult dirigeraResult = new DirigeraDiscoveryResult();
        dirigeraResult.result = Optional.of(discoveryResult);
        dirigeraResult.isDelivered = isDelivered;
        dirigeraResult.isKnown = isKnown;
        return dirigeraResult;
    }
}
