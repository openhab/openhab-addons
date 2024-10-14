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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.exception.ModelUpdateException;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.network.RestAPI;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Model} holds the complete Gateway model
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Model {
    private final Logger logger = LoggerFactory.getLogger(Model.class);

    public final static String REACHABLE = "isReachable";
    public final static String ATTRIBUTES = "attributes";
    public final static String CUSTOM_NAME = "customName";
    public final static String DEVICE_MODEL = "model";
    public final static String DEVICE_TYPE = "deviceType";

    private JSONObject model = new JSONObject();
    private Gateway gateway;

    public Model(Gateway gateway) {
        this.gateway = gateway;
    }

    /**
     * All functions synchronized in order to keep it thread-safe.
     * No query shall happen in parallel to an update
     */
    public synchronized void update() {
        RestAPI api = gateway.api();
        try {
            JSONObject home = api.readHome();
            if (home.has(PROPERTY_HTTP_ERROR_STATUS)) {
                logger.warn("DIRIGERA MODEL received model with error code {} - don't take it",
                        home.get(PROPERTY_HTTP_ERROR_STATUS));
            } else {
                model = home;
                List<Object> newDevices = getAllIds().toList();
                newDevices.forEach(deviceId -> {
                    gateway.newDevice(deviceId.toString());
                });
            }
        } catch (Throwable t) {
            throw new ModelUpdateException("Excpetion during model update " + t.getMessage());
        }
    }

    /**
     * only used for unit testing
     */
    public synchronized void update(String modelString) {
        this.model = new JSONObject(modelString);
        List<Object> newDevices = getAllIds().toList();
        newDevices.forEach(deviceId -> {
            gateway.newDevice(deviceId.toString());
        });
    }

    public synchronized JSONArray getAllIds() {
        JSONArray returnArray = new JSONArray();
        if (!model.isNull(PROPERTY_DEVICES)) {
            JSONArray devices = model.getJSONArray("devices");
            Iterator<Object> entries = devices.iterator();
            while (entries.hasNext()) {
                JSONObject entry = (JSONObject) entries.next();
                returnArray.put(entry.get(PROPERTY_DEVICE_ID));
            }
        }
        return returnArray;
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
        logger.info("DIRIGERA MODEL identify thingtype for {}", id);
        if (!model.isNull(PROPERTY_DEVICES)) {
            JSONArray devices = model.getJSONArray(PROPERTY_DEVICES);
            Iterator<Object> entries = devices.iterator();
            while (entries.hasNext()) {
                JSONObject entry = (JSONObject) entries.next();
                if (id.equals(entry.get(PROPERTY_DEVICE_ID))) {
                    logger.info("DIRIGERA MODEL found entry for {}", id);
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
                                            logger.info("DIRIGERA MODEL identified {} for {}",
                                                    THING_TYPE_COLOR_LIGHT.toString(), id);
                                            return THING_TYPE_COLOR_LIGHT;
                                        case "temperature":
                                            logger.info("DIRIGERA MODEL identified {} for {}",
                                                    THING_TYPE_TEMPERATURE_LIGHT.toString(), id);
                                            return THING_TYPE_TEMPERATURE_LIGHT;
                                    }
                                }
                                break;
                            case DEVICE_TYPE_MOTION_SENSOR:
                                logger.info("DIRIGERA MODEL identified {} for {}", THING_TYPE_MOTION_SENSOR.toString(),
                                        id);
                                return THING_TYPE_MOTION_SENSOR;
                            case DEVICE_TYPE_LIGHT_SENSOR:
                                logger.info("DIRIGERA MODEL identified {} for {}", THING_TYPE_LIGHT_SENSOR.toString(),
                                        id);
                                return THING_TYPE_LIGHT_SENSOR;
                            case DEVICE_TYPE_CONTACT_SENSOR:
                                logger.info("DIRIGERA MODEL identified {} for {}", THING_TYPE_CONTACT_SENSOR.toString(),
                                        id);
                                return THING_TYPE_CONTACT_SENSOR;
                            case DEVICE_TYPE_SMART_PLUG:
                                logger.info("DIRIGERA MODEL identified {} for {}", THING_TYPE_SMART_PLUG.toString(),
                                        id);
                                return THING_TYPE_SMART_PLUG;
                            case DEVICE_TYPE_SPEAKER:
                                logger.info("DIRIGERA MODEL identified {} for {}", THING_TYPE_SPEAKER.toString(), id);
                                return THING_TYPE_SPEAKER;
                            default:
                                logger.info("DIRIGERA MODEL Unsuppoerted Device {} with attributes {}", deviceType,
                                        attributes);
                        }
                    }
                }
            }

        }
        return THING_TYPE_UNKNNOWN;
    }

    public synchronized JSONObject getAllFor(String id) {
        JSONObject returnObject = new JSONObject();
        if (!model.isNull(PROPERTY_DEVICES)) {
            JSONArray devices = model.getJSONArray(PROPERTY_DEVICES);
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
        JSONObject deviceObject = getAllFor(id);
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
        return id;
    }
}
