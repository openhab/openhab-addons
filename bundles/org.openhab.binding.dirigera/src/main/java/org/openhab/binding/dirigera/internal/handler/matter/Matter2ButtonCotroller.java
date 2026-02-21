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

import static org.openhab.binding.dirigera.internal.Constants.*;
import static org.openhab.binding.dirigera.internal.interfaces.Model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Matter2ButtonCotroller} handles a Matter 2-Button Controller device e.g. BILRESA from IKEA.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Matter2ButtonCotroller extends BaseMatterHandler {
    private static final Map<String, String> TRIGGER_MAPPING = Map.of("singlePress", "SINGLE_PRESS", "doublePress",
            "DOUBLE_PRESS", "longPress", "LONG_PRESS");
    private final Logger logger = LoggerFactory.getLogger(Matter2ButtonCotroller.class);
    private final Map<String, List<String>> modeLinkCandidateMapping = Map.of("light",
            List.of(DEVICE_TYPE_LIGHT, DEVICE_TYPE_OUTLET), "speaker", List.of(DEVICE_TYPE_SPEAKER), "blind",
            List.of(DEVICE_TYPE_BLINDS));
    private Map<String, String> triggerChannelMapping = new HashMap<>();

    public Matter2ButtonCotroller(Thing thing) {
        super(thing);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        deviceModelMap.forEach((deviceId, config) -> {
            char buttonNumber = deviceId.charAt(deviceId.length() - 1);
            var buttonName = switch (buttonNumber) {
                case '1' -> "Top Button";
                case '2' -> "Lower Button";
                default -> "Button " + buttonNumber;
            };
            String triggerChannelName = buttonName.toLowerCase(Locale.ENGLISH).replace(" ", "-");
            triggerChannelMapping.put(deviceId, triggerChannelName);
            createChannelIfNecessary(triggerChannelName, "system.button", "", buttonName,
                    "Press triggers for " + buttonName.toLowerCase(Locale.ENGLISH));
        });
        // for controller 100% needed
        createChannelIfNecessary(CHANNEL_LINKS, CHANNEL_LINKS, CoreItemFactory.STRING);
        createChannelIfNecessary(CHANNEL_LINK_CANDIDATES, CHANNEL_LINK_CANDIDATES, CoreItemFactory.STRING);
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);

        // handle remotePress events
        String channelName = triggerChannelMapping.get(update.optString(JSON_KEY_DEVICE_ID));
        String clickPattern = TRIGGER_MAPPING.get(update.optString(EVENT_KEY_CLICK_PATTER));
        if (channelName != null && clickPattern != null) {
            logger.warn("Button {} pressed: {}", channelName, clickPattern);
            triggerChannel(channelName, clickPattern);
        }

        // change link candidates id control-mode switched
        JSONObject attributes = update.optJSONObject(JSON_KEY_ATTRIBUTES);
        if (attributes != null) {
            String controlMode = attributes.optString(ATTRIBUTES_KEY_CONTROL_MODE);
            String deviceId = update.optString(JSON_KEY_DEVICE_ID);
            if (!controlMode.isBlank() && !deviceId.isBlank()) {
                List<String> candidateTypes = modeLinkCandidateMapping.get(controlMode);
                if (candidateTypes != null) {
                    linkHandlerMap.put(deviceId, new LinkHandler(this, deviceId, candidateTypes));
                    gateway().updateLinks();
                    logger.trace("Link candidate types for control-mode {}: {}", controlMode, candidateTypes);
                }
            }
        }
    }
}
