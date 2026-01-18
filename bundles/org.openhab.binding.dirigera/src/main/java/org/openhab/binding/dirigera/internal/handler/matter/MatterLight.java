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
import static org.openhab.binding.dirigera.internal.interfaces.Model.DEVICE_TYPE_LIGHT;
import static org.openhab.binding.dirigera.internal.model.MatterModel.*;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.DirigeraStateDescriptionProvider;
import org.openhab.binding.dirigera.internal.config.ColorLightConfiguration;
import org.openhab.binding.dirigera.internal.handler.light.ColorLightHandler;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.binding.dirigera.internal.model.MatterModel;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MatterLight} is configured by devices.json
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MatterLight extends ColorLightHandler {
    private final Logger logger = LoggerFactory.getLogger(MatterLight.class);
    protected Map<String, MatterModel> configMap = new TreeMap<>();

    public MatterLight(Thing thing, Map<String, String> stateChannelMapping,
            DirigeraStateDescriptionProvider stateProvider) {
        super(thing, COLOR_LIGHT_MAP, stateProvider);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        lightConfig = getConfigAs(ColorLightConfiguration.class);
        configMap.put(lightConfig.id, new MatterModel(lightConfig.id, DEVICE_TYPE_LIGHT));
        super.initialize();
        JSONObject update = gateway().api().readDevice(lightConfig.id);
        createChannels(update);
        super.handleUpdate(update);
    }

    private void createChannels(JSONObject values) {
        if (values.has(Model.JSON_KEY_ATTRIBUTES)) {
            JSONObject attributes = values.getJSONObject(Model.JSON_KEY_ATTRIBUTES);
            configMap.forEach((deviceId, matterConfig) -> {
                matterConfig.getStatusProperties().forEach((statusPropertyKey, statusPropertyJson) -> {
                    String deviceAttribute = statusPropertyJson.optString(MatterModel.CHANNEL_KEY_ATTRIBUTE);
                    if (attributes.has(deviceAttribute)) {
                        createChannelIfNecessary(statusPropertyJson.optString(CHANNEL_KEY_CHANNEL_NAME),
                                statusPropertyJson.optString(CHANNEL_KEY_CHANNEL_TYPE),
                                statusPropertyJson.optString(CHANNEL_KEY_ITEM_TYPE),
                                statusPropertyJson.optString(CHANNEL_KEY_CHANNEL_LABEL),
                                statusPropertyJson.optString(CHANNEL_KEY_CHANNEL_DESCRIPTION));
                        if ("colorTemperature".equals(deviceAttribute)) {
                            // add additional channel for color temperature in percent
                            createChannelIfNecessary(CHANNEL_LIGHT_TEMPERATURE, "system.color-temperature",
                                    CoreItemFactory.DIMMER);
                        }
                    }
                });
            });
        }
        thing.getChannels().forEach(channel -> {
            logger.warn(" Matter Light thing channels:  {} type: {}", channel.getUID(), channel.getLabel());
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("MatterLight handleCommand called for channel {} with command {}", channelUID, command);
        super.handleCommand(channelUID, command);
    }
}
