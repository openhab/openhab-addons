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
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.BaseDeviceConfiguration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Matter3ButtonCotroller} is the custom handling for BILRESA 3-Button with 3 groups. For each group a
 * separate handler is created to handle the complexity.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Matter3ButtonCotroller extends BaseMatterHandler {
    private static final Map<String, String> TRIGGER_MAPPING = Map.of("singlePress", "SINGLE_PRESS", "doublePress",
            "DOUBLE_PRESS", "longPress", "LONG_PRESS");
    private final Logger logger = LoggerFactory.getLogger(Matter3ButtonCotroller.class);
    private final Map<String, List<String>> modeLinkCandidateMapping = Map.of("light",
            List.of(DEVICE_TYPE_LIGHT, DEVICE_TYPE_OUTLET), "speaker", List.of(DEVICE_TYPE_SPEAKER));
    private Map<String, String> triggerChannelMapping = new HashMap<>();

    public Matter3ButtonCotroller(Thing thing) {
        super(thing);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        config = getConfigAs(BaseDeviceConfiguration.class);
        if (!getGateway()) {
            return;
        }
        configure();
        super.initialize();
        // for controller 100% needed
        createChannelIfNecessary(CHANNEL_LINKS, CHANNEL_LINKS, CoreItemFactory.STRING);
        createChannelIfNecessary(CHANNEL_LINK_CANDIDATES, CHANNEL_LINK_CANDIDATES, CoreItemFactory.STRING);
    }

    @Override
    protected void configure() {
        int subDeviceId = Character.getNumericValue(config.id.charAt(config.id.length() - 1));
        String relationId = gateway().model().getRelationId(config.id);
        int j = 1;
        for (int i = subDeviceId; i > subDeviceId - 3; i--) {
            String deviceId = relationId + "_" + i;
            deviceConfigMap.put(deviceId, new DeviceConfig(deviceId, thing.getThingTypeUID().getId()));
            triggerChannelMapping.put(deviceId, createTriggerChannel(j));
            j++;
        }
    }

    private String createTriggerChannel(int i) {
        var buttonName = switch (i) {
            case 1 -> "Scroll Down";
            case 2 -> "Scroll Up";
            case 3 -> "Press";
            default -> "Button " + i;
        };
        String triggerChannelName = buttonName.toLowerCase(Locale.US).replace(" ", "-");
        createChannelIfNecessary(triggerChannelName, "system.button", "", buttonName,
                "Triggers for button " + buttonName);
        return triggerChannelName;
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);

        // handle remotePress events
        String channelName = triggerChannelMapping.get(update.optString(PROPERTY_DEVICE_ID));
        String clickPattern = TRIGGER_MAPPING.get(update.optString("clickPattern"));
        if (channelName != null && clickPattern != null) {
            logger.warn("Button {} pressed: {}", channelName, clickPattern);
            triggerChannel(channelName, clickPattern);
        }

        // change link candidates id control-mode switched
        JSONObject attributes = update.optJSONObject("attributes");
        if (attributes != null) {
            String controlMode = attributes.optString("controlMode");
            if (!controlMode.isBlank()) {
                List<String> candidateTypes = modeLinkCandidateMapping.get(controlMode);
                if (candidateTypes != null) {
                    if (!candidateTypes.equals(linkCandidateTypes)) {
                        linkCandidateTypes.clear();
                        linkCandidateTypes.addAll(candidateTypes);
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
