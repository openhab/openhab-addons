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
package org.openhab.binding.dirigera.internal.handler;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartPlugHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SmartPlugHandler extends BaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartPlugHandler.class);

    public SmartPlugHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        // handle general initialize like setting bridge
        super.initialize();
        gateway().registerDevice(this);
        // finally get attributes from model in order to get initial values
        JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_DEVICES);
        logger.trace("DIRIGERA MOTION_DEVICE values for initial update {}", values);
        handleUpdate(values);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // only handle RefreshType
        String channel = channelUID.getIdWithoutGroup();
        logger.trace("DIRIGERA MOTION_DEVICE handle command {} for {}", command, channel);
        if (command instanceof RefreshType) {
            JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_DEVICES);
            handleUpdate(values);
        } else {
            String targetProperty = channel2PropertyMap.get(channel);
            if (targetProperty != null) {
                if (CHANNEL_CHILD_LOCK.equals(channel) || CHANNEL_STATE.equals(channel)
                        || CHANNEL_STATUS_LIGHT.equals(channel)) {
                    if (command instanceof OnOffType onOff) {
                        JSONObject attributes = new JSONObject();
                        attributes.put(targetProperty, onOff.equals(OnOffType.ON));
                        logger.trace("DIRIGERA SMART_PLUG send to API {}", attributes);
                        gateway().api().sendPatch(config.id, attributes);
                    }
                } else if (CHANNEL_STATUS_BRIGHTNESS.equals(channel)) {
                    if (command instanceof PercentType percent) {
                        JSONObject attributes = new JSONObject();
                        attributes.put(targetProperty, percent.intValue());
                        logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE send to API {}", attributes);
                        gateway().api().sendPatch(config.id, attributes);
                    } else {
                        logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE command {} doesn't fit to channel {}", command,
                                channel);
                    }
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
            logger.trace("DIRIGERA MOTION_DEVICE update delivered {} attributes", attributes.length());
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    if (CHANNEL_CHILD_LOCK.equals(targetChannel) || CHANNEL_STATE.equals(targetChannel)
                            || CHANNEL_STATUS_LIGHT.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                OnOffType.from(attributes.getBoolean(key)));
                    } else if (CHANNEL_STATUS_BRIGHTNESS.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                new PercentType(attributes.getInt(key)));
                    } else if (CHANNEL_POWER.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                QuantityType.valueOf(attributes.getDouble(key), Units.WATT));
                    } else if (CHANNEL_CURRENT.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                QuantityType.valueOf(attributes.getDouble(key), Units.AMPERE));
                    } else if (CHANNEL_POTENTIAL.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                QuantityType.valueOf(attributes.getDouble(key), Units.VOLT));
                    } else {
                        logger.trace("DIRIGERA MOTION_DEVICE no channel for {} available", key);
                    }
                } else {
                    logger.trace("DIRIGERA MOTION_DEVICE no targetChannel for {}", key);
                }
            }
        }
    }
}
