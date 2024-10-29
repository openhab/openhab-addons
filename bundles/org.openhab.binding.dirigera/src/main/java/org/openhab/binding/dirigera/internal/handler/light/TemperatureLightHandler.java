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
package org.openhab.binding.dirigera.internal.handler.light;

import static org.openhab.binding.dirigera.internal.Constants.CHANNEL_LIGHT_TEMPERATURE;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TemperatureLightHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TemperatureLightHandler extends DimmableLightHandler {
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
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readDevice(config.id);

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
            logger.debug("DIRIGERA TEMPERATURE_LIGHT Temperature range from {} to {}", colorTemperatureMin,
                    colorTemperatureMax);
            handleUpdate(values);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("DIRIGERA TEMPERATURE_LIGHT handleCommand {} {}", channelUID, command);
        super.handleCommand(channelUID, command);
        String channel = channelUID.getIdWithoutGroup();
        String targetProperty = channel2PropertyMap.get(channel);
        if (targetProperty != null) {
            switch (channel) {
                case CHANNEL_LIGHT_TEMPERATURE:
                    if (command instanceof PercentType percent) {
                        int kelvin = Math.round(colorTemperatureMin - (range * percent.intValue() / 100));
                        JSONObject attributes = new JSONObject();
                        attributes.put(targetProperty, kelvin);
                        logger.trace("DIRIGERA TEMPERATURE_LIGHT send to API {}", attributes);
                        gateway().api().sendPatch(config.id, attributes);
                    }
                    break;
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
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    switch (targetChannel) {
                        case CHANNEL_LIGHT_TEMPERATURE:
                            int kelvin = attributes.getInt(key);
                            // seems some lamps are delivering temperature values out of range
                            // keep it in range with min/max
                            kelvin = Math.min(kelvin, colorTemperatureMin);
                            kelvin = Math.max(kelvin, colorTemperatureMax);
                            int percent = Math.round(100 - ((kelvin - colorTemperatureMax) * 100 / range));
                            updateState(new ChannelUID(thing.getUID(), targetChannel), new PercentType(percent));
                            break;
                    }
                }
            }
        }
    }
}
