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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.library.CoreItemFactory;
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
        configMap.forEach((deviceId, config) -> {
            char buttonNumber = deviceId.charAt(deviceId.length() - 1);
            String triggerChannelName = "button" + buttonNumber;
            triggerChannelMapping.put(deviceId, triggerChannelName);
            createChannelIfNecessary(triggerChannelName, "system.button", null, "Button " + buttonNumber,
                    "Press triggers for button " + buttonNumber);
        });
        // for controller 100% needed
        createChannelIfNecessary(CHANNEL_LINKS, CHANNEL_LINKS, CoreItemFactory.STRING);
        createChannelIfNecessary(CHANNEL_LINK_CANDIDATES, CHANNEL_LINK_CANDIDATES, CoreItemFactory.STRING);
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);

        // handle remotePress events
        String channelName = triggerChannelMapping.get(update.optString("id"));
        String clickPattern = TRIGGER_MAPPING.get(update.optString("clickPattern"));
        if (channelName != null && clickPattern != null) {
            logger.warn("Button {} pressed: {}", channelName, clickPattern);
            triggerChannel(channelName, clickPattern);
        }

        // change link candidates id control-mode switched
        JSONObject attributes = update.optJSONObject("attributes");
        if (attributes != null) {
            String controlMode = attributes.optString("controlMode");
            System.out.println("Control Mode: " + controlMode);
            if (!controlMode.isBlank()) {
                List<String> candidateTypes = modeLinkCandidateMapping.get(controlMode);
                System.out.println("Candidate Types: " + candidateTypes);
                if (candidateTypes != null) {
                    if (!candidateTypes.equals(linkCandidateTypes)) {
                        System.out.println("Updating link candidate types for control mode " + controlMode);
                        linkCandidateTypes.clear();
                        linkCandidateTypes.addAll(candidateTypes);
                        System.out.println("New Link Candidate Types: " + linkCandidateTypes);
                        logger.info("Link candidate types for control-mode {}: {}", controlMode, linkCandidateTypes);
                        gateway().updateLinks();
                    }
                } else {
                    linkCandidateTypes.clear();
                    gateway().updateLinks();
                    logger.warn("No link candidate types found for control-mode {}", controlMode);
                }
            }
        }
    }
}
