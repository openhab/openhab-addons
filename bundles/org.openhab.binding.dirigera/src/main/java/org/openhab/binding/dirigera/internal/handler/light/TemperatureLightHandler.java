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
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TemperatureLightHandler} for lights with brightness and color temperature
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TemperatureLightHandler extends DimmableLightHandler {
    private final Logger logger = LoggerFactory.getLogger(TemperatureLightHandler.class);

    // default values of "standard Ikea lamps" from json
    private int colorTemperatureMax = 2202;
    private int colorTemperatureMin = 4000;
    private int range = colorTemperatureMin - colorTemperatureMax;
    private PercentType currentColorTemp = new PercentType();

    public TemperatureLightHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readDevice(config.id);
            JSONObject attributes = values.getJSONObject(Model.ATTRIBUTES);
            // check for settings of color temperature in attributes
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
        super.handleCommand(channelUID, command);
        logger.trace("DIRIGERA TEMPERATURE_LIGHT {} handleCommand {} {} {}", thing.getLabel(), channelUID, command,
                command.getClass());
        String channel = channelUID.getIdWithoutGroup();
        String targetProperty = channel2PropertyMap.get(channel);
        if (targetProperty != null) {
            switch (channel) {
                case CHANNEL_LIGHT_TEMPERATURE:
                    if (command instanceof PercentType percent) {
                        int kelvin = getKelvin(percent.intValue());
                        JSONObject attributes = new JSONObject();
                        attributes.put(targetProperty, kelvin);
                        super.changeProperty(LightCommand.Action.TEMPERARTURE, attributes);
                        if (!isPowered()) {
                            // fake event for power OFF
                            updateState(channelUID, percent);
                        }
                    } else if (command instanceof OnOffType onOff) {
                        super.addOnOffCommand(OnOffType.ON.equals(onOff));
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
                            int percent = getPercent(kelvin);
                            currentColorTemp = new PercentType(percent);
                            updateState(new ChannelUID(thing.getUID(), targetChannel), currentColorTemp);
                            break;
                    }
                }
            }
        }
    }

    protected int getKelvin(int percent) {
        return Math.round(colorTemperatureMin - (range * percent / 100));
    }

    protected int getPercent(int kelvin) {
        return Math.round(100 - ((kelvin - colorTemperatureMax) * 100 / range));
    }
}
