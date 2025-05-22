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
package org.openhab.binding.dirigera.internal.handler.controller;

import static org.openhab.binding.dirigera.internal.Constants.PROPERTY_DEVICE_ID;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseShortcutController} for triggering scenes
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BaseShortcutController extends BaseHandler {
    private final Logger logger = LoggerFactory.getLogger(BaseShortcutController.class);

    private Storage<String> storage;
    public Map<String, String> sceneMapping = new HashMap<>();
    private Map<String, Instant> triggerTimes = new HashMap<>();

    private static final String SINGLE_PRESS = "singlePress";
    private static final String DOUBLE_PRESS = "doublePress";
    private static final String LONG_PRESS = "longPress";
    private static final List<String> CLICK_PATTERNS = List.of(SINGLE_PRESS, DOUBLE_PRESS, LONG_PRESS);

    public BaseShortcutController(Thing thing, Map<String, String> mapping, Storage<String> bindingStorage) {
        super(thing, mapping);
        super.setChildHandler(this);
        this.storage = bindingStorage;
    }

    public void initializeScenes(String deviceId, String channel) {
        // check scenes
        CLICK_PATTERNS.forEach(pattern -> {
            String patternKey = deviceId + ":" + channel + ":" + pattern;
            if (!sceneMapping.containsKey(patternKey)) {
                String patternSceneId = storage.get(patternKey);
                if (patternSceneId != null) {
                    sceneMapping.put(patternKey, patternSceneId);
                } else {
                    String uuid = getUID();
                    String createdUUID = gateway().api().createScene(uuid, pattern, deviceId);
                    if (uuid.equals(createdUUID)) {
                        storage.put(patternKey, createdUUID);
                        sceneMapping.put(patternKey, createdUUID);
                    } else {
                        logger.warn("DIRIGERA BASE_SHORTCUT_CONTROLLER scene create failed for {}", patternKey);
                    }
                }
            }

            // after all check if scene is created and register for updates
            String sceneId = sceneMapping.get(patternKey);
            if (sceneId != null) {
                gateway().registerDevice(this, sceneId);
            }
        });
    }

    @Override
    public void dispose() {
        sceneMapping.forEach((key, value) -> {
            BaseHandler proxy = child;
            if (proxy != null) {
                gateway().unregisterDevice(proxy, value);
            }
        });
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        sceneMapping.forEach((key, value) -> {
            // cleanup storage and hub
            BaseHandler proxy = child;
            if (proxy != null) {
                gateway().deleteDevice(proxy, value);
            }
            gateway().api().deleteScene(value);
            storage.remove(key);
        });
        super.handleRemoval();
    }

    private String getUID() {
        String uuid = UUID.randomUUID().toString();
        while (gateway().model().has(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);
        if (update.has(PROPERTY_DEVICE_ID) && update.has("triggers")) {
            // first check if trigger happened
            String sceneId = update.getString(PROPERTY_DEVICE_ID);
            JSONArray triggers = update.getJSONArray("triggers");
            boolean triggered = false;
            for (int i = 0; i < triggers.length(); i++) {
                JSONObject triggerObject = triggers.getJSONObject(i);
                if (triggerObject.has("triggeredAt")) {
                    String triggerTimeString = triggerObject.getString("triggeredAt");
                    Instant triggerTime = Instant.parse(triggerTimeString);
                    Instant lastTriggered = triggerTimes.get(sceneId);
                    if (lastTriggered != null) {
                        if (triggerTime.isAfter(lastTriggered)) {
                            triggerTimes.put(sceneId, triggerTime);
                            triggered = true;
                        }
                    } else {
                        triggered = true;
                        triggerTimes.put(sceneId, triggerTime);
                        break;
                    }
                }
            }
            // if triggered deliver
            if (triggered) {
                sceneMapping.forEach((key, value) -> {
                    if (sceneId.equals(value)) {
                        String[] channelPattern = key.split(":");
                        String pattern = "";
                        switch (channelPattern[2]) {
                            case SINGLE_PRESS:
                                pattern = "SHORT_PRESSED";
                                break;
                            case DOUBLE_PRESS:
                                pattern = "DOUBLE_PRESSED";
                                break;
                            case LONG_PRESS:
                                pattern = "LONG_PRESSED";
                                break;
                        }
                        if (!pattern.isBlank()) {
                            triggerChannel(new ChannelUID(thing.getUID(), channelPattern[1]), pattern);
                        }
                    }
                });
            }
        }
    }
}
