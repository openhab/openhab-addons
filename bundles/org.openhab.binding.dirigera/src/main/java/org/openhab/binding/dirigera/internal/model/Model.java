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
                            case DEVICE_TYPE_LIGHT:
                                if (attributes.has(ATTRIBUTE_COLOR_MODE)) {
                                    String colorMode = attributes.getString(ATTRIBUTE_COLOR_MODE);
                                    switch (colorMode) {
                                        case "color":
                                            ThingTypeUID ttUID = THING_TYPE_COLOR_LIGHT;
                                            logger.info("DIRIGERA MODEL identified {} for {}", ttUID.toString(), id);
                                            if (SUPPORTED_THING_TYPES_UIDS.contains(ttUID)) {
                                                logger.info("DIRIGERA MODEL {} is suppoerted", ttUID);
                                                return ttUID;
                                            } else {
                                                logger.warn("DIRIGERA MODEL {} is not suppoerted - adapt code please",
                                                        ttUID);
                                            }
                                            break;
                                        case "temperature":
                                            // TODO
                                            break;
                                    }
                                }
                                break;
                            case DEVICE_TYPE_MOTION_SENSOR:
                                ThingTypeUID ttUID = THING_TYPE_MOTION_SENSOR;
                                logger.info("DIRIGERA MODEL identified {} for {}", ttUID.toString(), id);
                                if (SUPPORTED_THING_TYPES_UIDS.contains(ttUID)) {
                                    logger.info("DIRIGERA MODEL {} is suppoerted", ttUID);
                                    return ttUID;
                                } else {
                                    logger.warn("DIRIGERA MODEL {} is not suppoerted - adapt code please", ttUID);
                                }
                                break;
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

    public String getCustonNameFor(String id) {
        JSONObject deviceObject = getAllFor(id);
        if (deviceObject.has(ATTRIBUTES)) {
            JSONObject attributes = deviceObject.getJSONObject(ATTRIBUTES);
            if (attributes.has(CUSTOM_NAME)) {
                return attributes.getString(CUSTOM_NAME);
            } else if (attributes.has(DEVICE_MODEL)) {
                return attributes.getString(DEVICE_MODEL);
            } else if (deviceObject.has(DEVICE_TYPE)) {
                return deviceObject.getString(DEVICE_TYPE);
            }
            // 3 fallback options}
        }
        return PROPERTY_EMPTY;
    }
}
