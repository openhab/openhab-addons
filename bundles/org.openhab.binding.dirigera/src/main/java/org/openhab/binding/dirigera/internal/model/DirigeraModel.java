/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.interfaces.DirigeraAPI;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DirigeraModel} is representing the structural data of the devices connected to gateway. Concrete values of
 * devices shall not be accessed.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DirigeraModel implements Model {
    private final Logger logger = LoggerFactory.getLogger(DirigeraModel.class);

    private Map<String, DiscoveryResult> resultMap = new HashMap<>();
    private Map<String, String> templates = new HashMap<>();
    private List<String> devices = new ArrayList<>();
    private JSONObject model = new JSONObject();
    private Gateway gateway;

    public DirigeraModel(Gateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public synchronized String getModelString() {
        return model.toString();
    }

    @Override
    public synchronized int update() {
        Instant startTime = Instant.now();
        JSONObject home = gateway.api().readHome();
        // call finished with error code ...
        if (home.has(DirigeraAPI.HTTP_ERROR_FLAG)) {
            int status = home.getInt(DirigeraAPI.HTTP_ERROR_STATUS);
            logger.warn("DIRIGERA MODEL received model with error code {} - don't take it", status);
            return status;
        } else if (home.isEmpty()) {
            // ... call finished with unchecked exception ...
            return 500;
        } else {
            // ... call finished with success
            model = home;
            detection();
        }
        logger.trace("DIRIGERA MODEL full update {} ms", Duration.between(startTime, Instant.now()).toMillis());
        return 200;
    }

    @Override
    public synchronized void detection() {
        if (gateway.discoveryEnabled()) {
            List<String> previousDevices = new ArrayList<>();
            previousDevices.addAll(devices);

            // first get devices
            List<String> foundDevices = new ArrayList<>();
            foundDevices.addAll(getResolvedDeviceList());
            foundDevices.addAll(getAllSceneIds());
            devices.clear();
            devices.addAll(foundDevices);
            previousDevices.forEach(deviceId -> {
                boolean known = gateway.isKnownDevice(deviceId);
                boolean removed = !foundDevices.contains(deviceId);
                if (removed) {
                    removedDeviceScene(deviceId);
                } else {
                    if (!known) {
                        addedDeviceScene(deviceId);
                    } // don't update known devices
                }
            });
            foundDevices.removeAll(previousDevices);
            foundDevices.forEach(deviceId -> {
                boolean known = gateway.isKnownDevice(deviceId);
                if (!known) {
                    addedDeviceScene(deviceId);
                }
            });
        }
    }

    /**
     * Returns list with resolved relations
     *
     * @return
     */
    @Override
    public synchronized List<String> getResolvedDeviceList() {
        List<String> deviceList = new ArrayList<>();
        if (!model.isNull(PROPERTY_DEVICES)) {
            JSONArray devices = model.getJSONArray(PROPERTY_DEVICES);
            Iterator<Object> entries = devices.iterator();
            while (entries.hasNext()) {
                JSONObject entry = (JSONObject) entries.next();
                String deviceId = entry.getString(PROPERTY_DEVICE_ID);
                String relationId = getRelationId(deviceId);
                if (!deviceId.equals(relationId)) {
                    TreeMap<String, String> relationMap = getRelations(relationId);
                    // store for complex devices store result with first found id
                    relationId = relationMap.firstKey();
                }
                if (!deviceList.contains(relationId)) {
                    deviceList.add(relationId);
                }
            }
        }
        return deviceList;
    }

    /**
     * Returns list with all device id's
     *
     * @return
     */
    @Override
    public synchronized List<String> getAllDeviceIds() {
        List<String> deviceList = new ArrayList<>();
        if (!model.isNull(PROPERTY_DEVICES)) {
            JSONArray devices = model.getJSONArray(PROPERTY_DEVICES);
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
                if (entry.has(PROPERTY_TYPE)) {
                    if ("userScene".equals(entry.getString(PROPERTY_TYPE))) {
                        if (entry.has(PROPERTY_DEVICE_ID)) {
                            String id = entry.getString(PROPERTY_DEVICE_ID);
                            sceneList.add(id);
                        }
                    }
                }
            }
        }
        return sceneList;
    }

    private void addedDeviceScene(String id) {
        DiscoveryResult result = identifiy(id);
        if (result != null) {
            gateway.discovery().deviceDiscovered(result);
            resultMap.put(id, result);
        }
    }

    private void removedDeviceScene(String id) {
        DiscoveryResult deliveredResult = resultMap.remove(id);
        if (deliveredResult != null) {
            gateway.discovery().deviceRemoved(deliveredResult);
        }
        // inform gateway to remove device and update handler accordingly
        gateway.deleteDevice(id);
    }

    @Override
    public synchronized List<String> getDevicesForTypes(List<String> types) {
        List<String> candidates = new ArrayList<>();
        types.forEach(type -> {
            JSONArray addons = getIdsForType(type);
            addons.forEach(entry -> {
                candidates.add(entry.toString());
            });
        });
        return candidates;
    }

    private JSONArray getIdsForType(String type) {
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

    private boolean hasAttribute(String id, String attribute) {
        JSONObject deviceObject = getAllFor(id, PROPERTY_DEVICES);
        if (deviceObject.has(ATTRIBUTES)) {
            JSONObject attributes = deviceObject.getJSONObject(ATTRIBUTES);
            return attributes.has(attribute);
        }
        return false;
    }

    @Override
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

    @Override
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

    @Override
    public synchronized Map<String, Object> getPropertiesFor(String id) {
        final Map<String, Object> properties = new HashMap<>();
        JSONObject deviceObject = getAllFor(id, PROPERTY_DEVICES);
        // get manufacturer, model and version data
        if (deviceObject.has(ATTRIBUTES)) {
            JSONObject attributes = deviceObject.getJSONObject(ATTRIBUTES);
            THING_PROPERTIES.forEach(property -> {
                if (attributes.has(property)) {
                    properties.put(property, attributes.get(property));
                }
            });
        }
        // put id in as representation property
        properties.put(PROPERTY_DEVICE_ID, id);
        // add capabilities
        if (deviceObject.has(CAPABILITIES)) {
            JSONObject capabilities = deviceObject.getJSONObject(CAPABILITIES);
            if (capabilities.has(PROPERTY_CAN_RECEIVE)) {
                properties.put(PROPERTY_CAN_RECEIVE, capabilities.getJSONArray(PROPERTY_CAN_RECEIVE));
            }
            if (capabilities.has(PROPERTY_CAN_SEND)) {
                properties.put(PROPERTY_CAN_SEND, capabilities.getJSONArray(PROPERTY_CAN_SEND));
            }
        }

        return properties;
    }

    @Override
    public synchronized TreeMap<String, String> getRelations(String relationId) {
        final TreeMap<String, String> relationsMap = new TreeMap<>();
        List<String> allDevices = getAllDeviceIds();
        allDevices.forEach(deviceId -> {
            JSONObject data = getAllFor(deviceId, PROPERTY_DEVICES);
            if (data.has(Model.PROPERTY_RELATION_ID)) {
                String relation = data.getString(Model.PROPERTY_RELATION_ID);
                if (relationId.equals(relation)) {
                    String relationDeviceId = data.getString(PROPERTY_DEVICE_ID);
                    String deviceType = data.getString(PROPERTY_DEVICE_TYPE);
                    if (relationDeviceId != null && deviceType != null) {
                        relationsMap.put(relationDeviceId, deviceType);
                    }
                }
            }
        });
        return relationsMap;
    }

    private @Nullable DiscoveryResult identifiy(String id) {
        ThingTypeUID ttuid = identifyDeviceFromModel(id);
        // don't report gateway, unknown devices and light sensors connected to motion sensors
        if (!THING_TYPE_GATEWAY.equals(ttuid) && !THING_TYPE_UNKNNOWN.equals(ttuid)
                && !THING_TYPE_LIGHT_SENSOR.equals(ttuid) && !THING_TYPE_IGNORE.equals(ttuid)) {
            // check if it's a simple or complex device
            String relationId = getRelationId(id);
            String firstDeviceId = id;
            if (!id.equals(relationId)) {
                // complex device
                TreeMap<String, String> relationMap = getRelations(relationId);
                // take name from first ordered entry
                firstDeviceId = relationMap.firstKey();
            }
            // take name and properties from first found id
            String customName = getCustonNameFor(firstDeviceId);
            Map<String, Object> propertiesMap = getPropertiesFor(firstDeviceId);
            return DiscoveryResultBuilder.create(new ThingUID(ttuid, gateway.getThing().getUID(), firstDeviceId))
                    .withBridge(gateway.getThing().getUID()).withProperties(propertiesMap)
                    .withRepresentationProperty(PROPERTY_DEVICE_ID).withLabel(customName).build();
        }
        return null;
    }

    /**
     * Identify device which is present in model
     *
     * @param id
     * @return
     */
    @Override
    public synchronized ThingTypeUID identifyDeviceFromModel(String id) {
        JSONObject entry = getAllFor(id, PROPERTY_DEVICES);
        if (entry.isEmpty()) {
            entry = getAllFor(id, PROPERTY_SCENES);
        }
        if (entry.isEmpty()) {
            return THING_TYPE_NOT_FOUND;
        } else {
            return identifyDeviceFromJSON(id, entry);
        }
    }

    private ThingTypeUID identifyDeviceFromJSON(String id, JSONObject data) {
        String typeDeviceType = "";
        if (data.has(Model.PROPERTY_RELATION_ID)) {
            return identifiyComplexDevice(data.getString(Model.PROPERTY_RELATION_ID));
        } else if (data.has(PROPERTY_DEVICE_TYPE)) {
            String deviceType = data.getString(PROPERTY_DEVICE_TYPE);
            typeDeviceType = deviceType;
            switch (deviceType) {
                case DEVICE_TYPE_GATEWAY:
                    return THING_TYPE_GATEWAY;
                case DEVICE_TYPE_LIGHT:
                    if (data.has(CAPABILITIES)) {
                        JSONObject capabilities = data.getJSONObject(CAPABILITIES);
                        List<String> capabilityList = new ArrayList<>();
                        if (capabilities.has(PROPERTY_CAN_RECEIVE)) {
                            JSONArray receiveProperties = capabilities.getJSONArray(PROPERTY_CAN_RECEIVE);
                            receiveProperties.forEach(capability -> {
                                capabilityList.add(capability.toString());
                            });
                        }
                        if (capabilityList.contains("colorHue")) {
                            return THING_TYPE_COLOR_LIGHT;
                        } else if (capabilityList.contains("colorTemperature")) {
                            return THING_TYPE_TEMPERATURE_LIGHT;
                        } else if (capabilityList.contains("lightLevel")) {
                            return THING_TYPE_DIMMABLE_LIGHT;
                        } else if (capabilityList.contains("isOn")) {
                            return THING_TYPE_SWITCH_LIGHT;
                        } else {
                            logger.warn("DIRIGERA MODEL cannot identify light {}", data);
                        }
                    } else {
                        logger.warn("DIRIGERA MODEL cannot identify light {}", data);
                    }
                    break;
                case DEVICE_TYPE_MOTION_SENSOR:
                    return THING_TYPE_MOTION_SENSOR;
                case DEVICE_TYPE_LIGHT_SENSOR:
                    return THING_TYPE_LIGHT_SENSOR;
                case DEVICE_TYPE_CONTACT_SENSOR:
                    return THING_TYPE_CONTACT_SENSOR;
                case DEVICE_TYPE_OUTLET:
                    if (hasAttribute(id, "currentActivePower")) {
                        return THING_TYPE_SMART_PLUG;
                    } else if (hasAttribute(id, "childLock")) {
                        return THING_TYPE_POWER_PLUG;
                    } else {
                        return THING_TYPE_SIMPLE_PLUG;
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
                case DEVICE_TYPE_AIR_PURIFIER:
                    return THING_TYPE_AIR_PURIFIER;
                case DEVICE_TYPE_BLINDS:
                    return THING_TYPE_BLIND;
                case DEVICE_TYPE_BLIND_CONTROLLER:
                    return THING_TYPE_BLIND_CONTROLLER;
                case DEVICE_TYPE_SOUND_CONTROLLER:
                    return THING_TYPE_SOUND_CONTROLLER;
                case DEVICE_TYPE_SHORTCUT_CONTROLLER:
                    return THING_TYPE_SINGLE_SHORTCUT_CONTROLLER;
            }
        } else {
            // device type is empty, check for scene
            if (!data.isNull(PROPERTY_TYPE)) {
                String type = data.getString(PROPERTY_TYPE);
                typeDeviceType = type + "/" + typeDeviceType; // just for logging
                switch (type) {
                    case TYPE_USER_SCENE:
                        return THING_TYPE_SCENE;
                    case TYPE_CUSTOM_SCENE:
                        return THING_TYPE_IGNORE;
                }
            }
        }
        logger.warn("DIRIGERA MODEL Unsupported device {} with data {} {}", typeDeviceType, data, id);
        return THING_TYPE_UNKNNOWN;
    }

    private ThingTypeUID identifiyComplexDevice(String relationId) {
        Map<String, String> relationsMap = getRelations(relationId);
        if (relationsMap.size() == 2 && relationsMap.containsValue("lightSensor")
                && relationsMap.containsValue("motionSensor")) {
            return THING_TYPE_MOTION_LIGHT_SENSOR;
        } else if (relationsMap.size() == 2 && relationsMap.containsValue("shortcutController")) {
            for (Iterator<String> iterator = relationsMap.keySet().iterator(); iterator.hasNext();) {
                if (!"shortcutController".equals(relationsMap.get(iterator.next()))) {
                    return THING_TYPE_UNKNNOWN;
                }
            }
            return THING_TYPE_DOUBLE_SHORTCUT_CONTROLLER;
        } else if (relationsMap.size() == 1 && relationsMap.containsValue("gatewy")) {
            return THING_TYPE_GATEWAY;
        } else {
            return THING_TYPE_UNKNNOWN;
        }
    }

    /**
     * Get relationId for a given device id
     *
     * @param id to check
     * @return same id if no relations are found or relationId
     */
    @Override
    public synchronized String getRelationId(String id) {
        JSONObject dataObject = getAllFor(id, PROPERTY_DEVICES);
        if (dataObject.has(PROPERTY_RELATION_ID)) {
            return dataObject.getString(PROPERTY_RELATION_ID);
        }
        return id;
    }

    /**
     * Check if given id is present in devices or scenes
     *
     * @param id to check
     * @return true if id is found
     */
    @Override
    public synchronized boolean has(String id) {
        return getAllDeviceIds().contains(id) || getAllSceneIds().contains(id);
    }

    @Override
    public String getTemplate(String name) {
        String template = templates.get(name);
        if (template == null) {
            template = getResourceFile(name);
            if (!template.isBlank()) {
                templates.put(name, template);
            } else {
                logger.warn("DIRIGERA MODEL empty template for {}", name);
                template = "{}";
            }
        }
        return template;
    }

    private String getResourceFile(String fileName) {
        try {
            Bundle myself = gateway.getBundleContext().getBundle();
            // do this check for unit tests to avoid NullPointerException
            if (myself != null) {
                URL url = myself.getResource(fileName);
                InputStream input = url.openStream();
                // https://www.baeldung.com/java-scanner-usedelimiter
                try (Scanner scanner = new Scanner(input).useDelimiter("\\A")) {
                    String result = scanner.hasNext() ? scanner.next() : "";
                    String resultReplaceAll = result.replaceAll("[\\n\\r\\s]", "");
                    scanner.close();
                    return resultReplaceAll;
                }
            } else {
                // only unit testing
                return Files.readString(Paths.get("src/main/resources" + fileName));
            }
        } catch (IOException e) {
            logger.warn("DIRIGERA MODEL no template found for {}", fileName);
        }
        return "";
    }
}
