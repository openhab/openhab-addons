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
package org.openhab.binding.dirigera.internal.handler.light;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.DirigeraStateDescriptionProvider;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

/**
 * {@link TemperatureLightHandler} for lights with brightness and color temperature
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TemperatureLightHandler extends DimmableLightHandler {
    private PercentType currentColorTemp = new PercentType();

    protected final DirigeraStateDescriptionProvider stateProvider;
    // default values of "standard IKEA lamps" from JSON
    protected int colorTemperatureMin = 4000;
    protected int colorTemperatureMax = 2202;
    protected int range = colorTemperatureMin - colorTemperatureMax;

    public TemperatureLightHandler(Thing thing, Map<String, String> mapping,
            DirigeraStateDescriptionProvider stateProvider) {
        super(thing, mapping);
        super.setChildHandler(this);
        this.stateProvider = stateProvider;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readDevice(config.id);
            JSONObject attributes = values.getJSONObject(Model.ATTRIBUTES);
            // check for settings of color temperature in attributes
            TreeMap<String, String> properties = new TreeMap<>(editProperties());
            Iterator<String> attributesIterator = attributes.keys();
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                if ("colorTemperatureMin".equals(key)) {
                    colorTemperatureMin = attributes.getInt(key);
                    properties.put("colorTemperatureMin", String.valueOf(colorTemperatureMin));
                } else if ("colorTemperatureMax".equals(key)) {
                    colorTemperatureMax = attributes.getInt(key);
                    properties.put("colorTemperatureMax", String.valueOf(colorTemperatureMax));
                }
            }
            StateDescriptionFragment fragment = StateDescriptionFragmentBuilder.create()
                    .withMinimum(BigDecimal.valueOf(colorTemperatureMax))
                    .withMaximum(BigDecimal.valueOf(colorTemperatureMin)).withStep(BigDecimal.valueOf(100))
                    .withPattern("%.0f K").withReadOnly(false).build();
            stateProvider.setStateDescription(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE_ABS), fragment);
            updateProperties(properties);
            range = colorTemperatureMin - colorTemperatureMax;
            handleUpdate(values);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        String channel = channelUID.getIdWithoutGroup();
        String targetProperty = channel2PropertyMap.get(channel);
        switch (channel) {
            case CHANNEL_LIGHT_TEMPERATURE_ABS:
                targetProperty = "colorTemperature";
            case CHANNEL_LIGHT_TEMPERATURE:
                long kelvinValue = -1;
                int percentValue = -1;
                if (command instanceof PercentType percent) {
                    percentValue = percent.intValue();
                    kelvinValue = getKelvin(percent.intValue());
                } else if (command instanceof QuantityType number) {
                    kelvinValue = number.intValue();
                    percentValue = getPercent(kelvinValue);
                } else if (command instanceof OnOffType onOff) {
                    super.addOnOffCommand(OnOffType.ON.equals(onOff));
                }
                /*
                 * some color lights which inherit this temperature light don't have the temperature capability.
                 * As workaround child class ColorLightHandler is handling color temperature
                 */
                if (receiveCapabilities.contains(Model.COLOR_TEMPERATURE_CAPABILITY) && percentValue != -1
                        && kelvinValue != -1) {
                    JSONObject attributes = new JSONObject();
                    attributes.put(targetProperty, kelvinValue);
                    super.changeProperty(LightCommand.Action.TEMPERATURE, attributes);
                    if (!isPowered()) {
                        // fake event for power OFF
                        updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE),
                                new PercentType(percentValue));
                        updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE_ABS),
                                QuantityType.valueOf(kelvinValue, Units.KELVIN));
                    }
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
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE_ABS),
                                    QuantityType.valueOf(kelvin, Units.KELVIN));
                            break;
                    }
                }
            }
        }
    }

    protected long getKelvin(int percent) {
        return Math.round(colorTemperatureMin - (range * percent / 100));
    }

    protected int getPercent(long kelvin) {
        return Math.min(100, Math.max(0, Math.round(100 - ((kelvin - colorTemperatureMax) * 100 / range))));
    }
}
