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
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TemperatureLightHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TemperatureLightHandler extends BaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(TemperatureLightHandler.class);

    // default values of "standard Ikea lamps" from json
    // "colorTemperatureMin": 4000,
    // "colorTemperatureMax": 2202,
    private int colorTemperatureMax = 2202;
    private int colorTemperatureMin = 4000;
    private int range = colorTemperatureMin - colorTemperatureMax;

    public TemperatureLightHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        PercentType pt = new PercentType(50);
    }

    @Override
    public void initialize() {
        // handle general initialize like setting bridge
        super.initialize();
        // finally get attributes from model in order to get initial values
        JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_DEVICES);
        handleUpdate(values);

        // check for settings of color temperature in attributes
        JSONObject attributes = values.getJSONObject(Model.ATTRIBUTES);
        Iterator<String> attributesIterator = attributes.keys();
        while (attributesIterator.hasNext()) {
            String key = attributesIterator.next();
            if ("colorTemperatureMin".equals(key)) {
                colorTemperatureMin = attributes.getInt(key);
            } else if ("colorTemperatureMax".equals(key)) {
                colorTemperatureMax = attributes.getInt(key);
            }
        }
        range = colorTemperatureMin - colorTemperatureMax;
        logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE new temperatures from {} to {}", colorTemperatureMin,
                colorTemperatureMax);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getIdWithoutGroup();
        logger.trace("DIRIGERA LIGHT_DEVICE handle command {} for {}", command, channel);
        if (command instanceof RefreshType) {
            JSONObject values = gateway().model().getAllFor(config.id, PROPERTY_DEVICES);
            handleUpdate(values);
        } else {
            String targetProperty = channel2PropertyMap.get(channel);
            if (targetProperty != null) {
                switch (channel) {
                    case CHANNEL_STATE:
                        if (command instanceof OnOffType onOff) {
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, onOff.equals(OnOffType.ON));
                            logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        } else {
                            logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE command {} doesn't fit to channel {}",
                                    command, channel);
                        }
                        break;
                    case CHANNEL_LIGHT_BRIGHTNESS:
                        if (command instanceof PercentType percent) {
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, percent.intValue());
                            logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        } else {
                            logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE command {} doesn't fit to channel {}",
                                    command, channel);
                        }
                        break;
                    case CHANNEL_LIGHT_TEMPERATURE:
                        if (command instanceof PercentType percent) {
                            int kelvin = Math.round(colorTemperatureMin - (range * percent.intValue() / 100));
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, kelvin);
                            logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        } else {
                            logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE command {} doesn't fit to channel {}",
                                    command, channel);
                        }
                        break;
                }
            } else {
                logger.trace("DIRIGERA LIGHT_DEVICE no property found for channel {}", channel);
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
            logger.trace("DIRIGERA LIGHT_DEVICE update delivered {} attributes", attributes.length());
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    if (CHANNEL_STATE.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                OnOffType.from(attributes.getBoolean(key)));
                    } else if (CHANNEL_LIGHT_BRIGHTNESS.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                new PercentType(attributes.getInt(key)));
                    } else if (CHANNEL_LIGHT_TEMPERATURE.equals(targetChannel)) {
                        int kelvin = attributes.getInt(key);
                        // seems some lamps are delivering temperature values out of range
                        // keep it in range with min/max
                        kelvin = Math.min(kelvin, colorTemperatureMin);
                        kelvin = Math.max(kelvin, colorTemperatureMax);
                        int percent = Math.round(100 - ((kelvin - colorTemperatureMax) * 100 / range));
                        updateState(new ChannelUID(thing.getUID(), targetChannel), new PercentType(percent));
                    } else {
                        logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE no channel for {} available", key);
                    }
                } else {
                    logger.trace("DIRIGERA TEMPERATURE_LIGHT_DEVICE no targetChannel for {}", key);
                }
            }
        }
    }
}
