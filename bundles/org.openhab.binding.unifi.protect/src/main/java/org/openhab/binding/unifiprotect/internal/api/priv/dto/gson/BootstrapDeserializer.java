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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.AiPort;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Bridge;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Camera;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Chime;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Doorlock;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Light;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Sensor;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Viewer;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.Bootstrap;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.Event;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.Group;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.Liveview;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.Nvr;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Custom Gson deserializer for Bootstrap that converts device arrays to maps
 * The API returns devices as arrays, but we convert them to maps keyed by ID
 *
 * @author Dan Cunningham - Initial contribution
 */
public class BootstrapDeserializer implements JsonDeserializer<Bootstrap> {

    private final Logger logger = LoggerFactory.getLogger(BootstrapDeserializer.class);

    @Override
    public Bootstrap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        logger.debug("BootstrapDeserializer.deserialize called");

        JsonObject obj = json.getAsJsonObject();
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.authUserId = getAsString(obj, "authUserId");
        bootstrap.accessKey = getAsString(obj, "accessKey");
        bootstrap.lastUpdateId = getAsString(obj, "lastUpdateId");

        logger.debug("Bootstrap authUserId: {}, lastUpdateId: {}", bootstrap.authUserId, bootstrap.lastUpdateId);

        if (obj.has("nvr")) {
            bootstrap.nvr = context.deserialize(obj.get("nvr"), Nvr.class);
            logger.debug("Deserialized NVR: {}", bootstrap.nvr != null ? "success" : "null");
        }

        // Convert device arrays to maps
        bootstrap.cameras = arrayToMap(obj, "cameras", Camera.class, context);
        logger.debug("Deserialized {} cameras", bootstrap.cameras.size());

        bootstrap.lights = arrayToMap(obj, "lights", Light.class, context);
        bootstrap.sensors = arrayToMap(obj, "sensors", Sensor.class, context);
        bootstrap.doorlocks = arrayToMap(obj, "doorlocks", Doorlock.class, context);
        bootstrap.chimes = arrayToMap(obj, "chimes", Chime.class, context);
        bootstrap.bridges = arrayToMap(obj, "bridges", Bridge.class, context);
        bootstrap.viewers = arrayToMap(obj, "viewers", Viewer.class, context);
        bootstrap.aiports = arrayToMap(obj, "aiports", AiPort.class, context);
        bootstrap.users = arrayToMap(obj, "users", User.class, context);
        logger.debug("Deserialized {} users", bootstrap.users.size());

        bootstrap.groups = arrayToMap(obj, "groups", Group.class, context);
        bootstrap.liveviews = arrayToMap(obj, "liveviews", Liveview.class, context);
        bootstrap.events = arrayToMap(obj, "events", Event.class, context);

        logger.debug("Bootstrap deserialization complete");
        return bootstrap;
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

    private <T> Map<String, T> arrayToMap(JsonObject obj, String field, Class<T> clazz,
            JsonDeserializationContext context) {
        Map<String, T> map = new HashMap<>();

        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            logger.trace("Field '{}' not present or is null", field);
            return map;
        }

        try {
            JsonArray array = obj.getAsJsonArray(field);
            logger.trace("Processing '{}' array with {} items", field, array.size());

            for (JsonElement element : array) {
                try {
                    T item = context.deserialize(element, clazz);
                    if (item != null) {
                        JsonObject itemObj = element.getAsJsonObject();
                        if (itemObj.has("id")) {
                            String id = itemObj.get("id").getAsString();
                            map.put(id, item);
                        } else {
                            logger.debug("Item in '{}' array has no 'id' field", field);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Failed to deserialize item in '{}' array: {}", field, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to process '{}' array: {}", field, e.getMessage(), e);
        }

        return map;
    }
}
