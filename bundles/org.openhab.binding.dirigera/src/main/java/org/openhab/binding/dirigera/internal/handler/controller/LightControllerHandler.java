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

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link LightControllerHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LightControllerHandler extends BaseHandler {

    public LightControllerHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        // links of types which can be established towards this device
        linkCandidateTypes = List.of(DEVICE_TYPE_LIGHT, DEVICE_TYPE_OUTLET);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readDevice(config.id);
            handleUpdate(values);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        String targetChannel = channelUID.getIdWithoutGroup();
        switch (targetChannel) {
            case CHANNEL_LIGHT_PRESET:
                if (command instanceof StringType string) {
                    JSONArray presetValues = new JSONArray();
                    // handle the standard presets from IKEA app, custom otherwise without consistency check
                    switch (string.toFullString()) {
                        case "Off":
                            // fine - array stays empty
                            break;
                        case "Warm":
                            presetValues = new JSONArray(
                                    gateway().model().getTemplate(Model.TEMPLATE_LIGHT_PRESET_WARM));
                            break;
                        case "Slowdown":
                            presetValues = new JSONArray(
                                    gateway().model().getTemplate(Model.TEMPLATE_LIGHT_PRESET_SLOWDOWN));
                            break;
                        case "Smooth":
                            presetValues = new JSONArray(
                                    gateway().model().getTemplate(Model.TEMPLATE_LIGHT_PRESET_SMOOTH));
                            break;
                        case "Bright":
                            presetValues = new JSONArray(
                                    gateway().model().getTemplate(Model.TEMPLATE_LIGHT_PRESET_BRIGHT));
                            break;
                        default:
                            presetValues = new JSONArray(string.toFullString());
                    }
                    JSONObject preset = new JSONObject();
                    preset.put("circadianPresets", presetValues);
                    super.sendAttributes(preset);
                }
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                switch (key) {
                    case "circadianPresets":
                        if (attributes.has("circadianPresets")) {
                            JSONArray lightPresets = attributes.getJSONArray("circadianPresets");
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_PRESET),
                                    StringType.valueOf(lightPresets.toString()));
                        }
                        break;
                }
            }
        }
    }
}
