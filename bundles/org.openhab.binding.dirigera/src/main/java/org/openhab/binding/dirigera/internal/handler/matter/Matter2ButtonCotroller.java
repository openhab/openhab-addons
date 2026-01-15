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
package org.openhab.binding.dirigera.internal.handler.matter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Matter2ButtonCotroller} is configured by devices.json
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Matter2ButtonCotroller extends BaseMatterHandler {
    private static final Map<String, String> TRIGGER_MAPPING = Map.of("singlePress", "SINGLE_PRESS", "doublePress",
            "DOUBLE_PRESS", "longPress", "LONG_PRESS");
    private final Logger logger = LoggerFactory.getLogger(Matter2ButtonCotroller.class);
    private Map<String, String> triggerChannelMapping = new HashMap<>();

    public Matter2ButtonCotroller(Thing thing) {
        super(thing);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        configMap.forEach((deviceId, config) -> {
            String triggerChannelName = "button" + deviceId.charAt(deviceId.length() - 1);
            triggerChannelMapping.put(deviceId, triggerChannelName);
            createChannelIfNecessary(triggerChannelName, "system.button", null);
        });
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);
        // handle remotePress events
        JSONObject data = update.optJSONObject("data");
        if (data != null) {
            String channelName = triggerChannelMapping.get(data.optString("id"));
            String clickPattern = TRIGGER_MAPPING.get(data.optString("clickPattern"));

            if (channelName != null && clickPattern != null) {
                triggerChannel(channelName, clickPattern);
            }
        }
    }
}
