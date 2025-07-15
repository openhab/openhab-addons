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
package org.openhab.binding.dirigera.internal.handler.blind;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlindHandler} for Window / Door blinds
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BlindHandler extends BaseHandler {
    private final Logger logger = LoggerFactory.getLogger(BlindHandler.class);
    public static final Map<String, Integer> BLIND_STATES = Map.of("stopped", 0, "up", 1, "down", 2);
    public static Map<Integer, String> blindNumberToState = reverseStateMapping(BLIND_STATES);

    public BlindHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        // links of types which can be established towards this device
        linkCandidateTypes = List.of(DEVICE_TYPE_BLIND_CONTROLLER);
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
        String channel = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else {
            String targetProperty = channel2PropertyMap.get(channel);
            if (targetProperty != null) {
                switch (channel) {
                    case CHANNEL_BLIND_STATE:
                        if (command instanceof DecimalType state) {
                            String commandAttribute = blindNumberToState.get(state.intValue());
                            if (commandAttribute != null) {
                                JSONObject attributes = new JSONObject();
                                attributes.put(targetProperty, commandAttribute);
                                super.sendAttributes(attributes);
                            } else {
                                logger.warn("DIRIGERA BLIND_DEVICE Blind state unknown {}", state.intValue());
                            }
                        }
                        break;
                    case CHANNEL_BLIND_LEVEL:
                        if (command instanceof PercentType percent) {
                            JSONObject attributes = new JSONObject();
                            attributes.put("blindsTargetLevel", percent.intValue());
                            super.sendAttributes(attributes);
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        // handle reachable flag
        super.handleUpdate(update);
        // now device specific
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    switch (targetChannel) {
                        case CHANNEL_BLIND_STATE:
                            String blindState = attributes.getString(key);
                            Integer stateValue = BLIND_STATES.get(blindState);
                            if (stateValue != null) {
                                updateState(new ChannelUID(thing.getUID(), targetChannel), new DecimalType(stateValue));
                            } else {
                                logger.warn("DIRIGERA BLIND_DEVICE Blind state unknown {}", blindState);
                            }
                            break;
                        case CHANNEL_BLIND_LEVEL:
                            updateState(new ChannelUID(thing.getUID(), targetChannel),
                                    new PercentType(attributes.getInt(key)));
                            break;
                    }
                }
            }
        }
    }
}
