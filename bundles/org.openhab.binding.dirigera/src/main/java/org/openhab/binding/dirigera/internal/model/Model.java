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

    private JSONObject model = new JSONObject();
    private Gateway gateway;

    public Model(Gateway gateway) {
        this.gateway = gateway;
    }

    public void update() {
        RestAPI api = gateway.api();
        try {
            JSONObject home = api.readHome();
            if (home.isEmpty()) {
                logger.warn("DIRIGERA MODEL received empty model - don't take it");
            } else {
                List<Object> currentDevices = getAllIds().toList();
                model = home;
                List<Object> newDevices = getAllIds().toList();
                newDevices.forEach(deviceId -> {
                    if (!currentDevices.contains(deviceId)) {
                        gateway.newDevice(deviceId.toString());
                    }
                });
            }
        } catch (Throwable t) {
            throw new ModelUpdateException("Excpetion during model update " + t.getMessage());
        }
    }

    public JSONArray getAllIds() {
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

    public JSONArray getIdsForType(String type) {
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
    public ThingTypeUID identifyDevice(String id) {
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
                            case PROPERTY_LIGHT:
                                if (attributes.has(ATTRIBUTE_COLOR_MODE)) {
                                    ThingTypeUID ttUID = new ThingTypeUID(BINDING_ID,
                                            deviceType + "-" + attributes.getString(ATTRIBUTE_COLOR_MODE));
                                    logger.info("DIRIGERA MODEL identified {} for {}", ttUID.toString(), id);
                                    if (SUPPORTED_THING_TYPES_UIDS.contains(ttUID)) {
                                        logger.info("DIRIGERA MODEL {} is suppoerted", ttUID);
                                        return ttUID;
                                    }
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

    public JSONObject getAllFor(String id) {
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
}
