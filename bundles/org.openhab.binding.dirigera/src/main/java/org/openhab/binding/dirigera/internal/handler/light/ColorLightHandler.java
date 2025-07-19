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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.DirigeraStateDescriptionProvider;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.binding.dirigera.internal.model.ColorModel;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ColorLightHandler} for lights with hue, saturation and brightness
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ColorLightHandler extends TemperatureLightHandler {
    private final Logger logger = LoggerFactory.getLogger(ColorLightHandler.class);

    private HSBType hsbStateReflection = new HSBType(); // proxy to reflect state to end user
    private HSBType hsbDevice = new HSBType(); // strictly holding values which were received via update
    private String colorMode = "";

    public ColorLightHandler(Thing thing, Map<String, String> mapping, DirigeraStateDescriptionProvider stateProvider) {
        super(thing, mapping, stateProvider);
        super.setChildHandler(this);
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
    public void dispose() {
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        String channel = channelUID.getIdWithoutGroup();
        if (CHANNEL_LIGHT_COLOR.equals(channel)) {
            if (command instanceof HSBType hsb) {
                // respect sequence
                switch (lightConfig.fadeSequence) {
                    case 0:
                        brightnessCommand(hsb);
                        colorCommand(hsb);
                        break;
                    case 1:
                        colorCommand(hsb);
                        brightnessCommand(hsb);
                        break;
                }
                hsbStateReflection = hsb;
                updateState(channelUID, hsb);
            } else if (command instanceof OnOffType) {
                super.addOnOffCommand(OnOffType.ON.equals(command));
            } else if (command instanceof PercentType percent) {
                int requestedBrightness = percent.intValue();
                if (requestedBrightness == 0) {
                    super.addOnOffCommand(false);
                } else {
                    brightnessCommand(new HSBType("0,0," + requestedBrightness));
                    super.addOnOffCommand(true);
                }
            }
        }
        if (CHANNEL_LIGHT_TEMPERATURE.equals(channel) || CHANNEL_LIGHT_TEMPERATURE_ABS.equals(channel)) {
            long kelvin = -1;
            HSBType colorTemp = null;
            if (command instanceof PercentType percent) {
                kelvin = super.getKelvin(percent.intValue());
                colorTemp = ColorModel.kelvin2Hsb(kelvin);
            } else if (command instanceof QuantityType number) {
                kelvin = number.intValue();
                colorTemp = ColorModel.kelvin2Hsb(kelvin);
            }
            // there are color lights which cannot handle tempera HSB {}t ,kelvin,colorTempure as stored in capabilities
            // in this case calculate color which is fitting to temperature
            if (colorTemp != null && !receiveCapabilities.contains(Model.COLOR_TEMPERATURE_CAPABILITY)) {
                HSBType colorTempAdaption = new HSBType(colorTemp.getHue(), colorTemp.getSaturation(),
                        hsbDevice.getBrightness());
                if (customDebug) {
                    logger.info("DIRIGERA COLOR_LIGHT {} handle temperature as color {}", thing.getLabel(),
                            colorTempAdaption);
                }
                colorCommand(colorTempAdaption);
            }
        }
    }

    /**
     * Send hue and saturation to light device in case of difference is more than 2%
     *
     * @param hsb as requested color
     * @return true if color request is sent, false otherwise
     */
    private void colorCommand(HSBType hsb) {
        if (!"color".equals(colorMode) || !ColorModel.closeTo(hsb, hsbDevice, 0.02)) {
            JSONObject colorAttributes = new JSONObject();
            colorAttributes.put("colorHue", hsb.getHue().intValue());
            colorAttributes.put("colorSaturation", hsb.getSaturation().intValue() / 100.0);
            super.changeProperty(LightCommand.Action.COLOR, colorAttributes);
        }
    }

    private void brightnessCommand(HSBType hsb) {
        int requestedBrightness = hsb.getBrightness().intValue();
        int currentBrightness = hsbDevice.getBrightness().intValue();
        if (Math.abs(requestedBrightness - currentBrightness) > 1) {
            if (requestedBrightness > 0) {
                JSONObject brightnessattributes = new JSONObject();
                brightnessattributes.put("lightLevel", hsb.getBrightness().intValue());
                super.changeProperty(LightCommand.Action.BRIGHTNESS, brightnessattributes);
            }
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            boolean deliverHSB = false;
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                if (ATTRIBUTE_COLOR_MODE.equals(key)) {
                    colorMode = attributes.getString(key);
                }
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    // apply and update to hsbCurrent, only in case !isOn deliver fake brightness HSBs
                    switch (targetChannel) {
                        case CHANNEL_LIGHT_COLOR:
                            switch (key) {
                                case "colorHue":
                                    double hueValue = attributes.getInt(key);
                                    hsbDevice = new HSBType(new DecimalType(hueValue), hsbDevice.getSaturation(),
                                            hsbDevice.getBrightness());
                                    hsbStateReflection = new HSBType(new DecimalType(hueValue),
                                            hsbStateReflection.getSaturation(), hsbStateReflection.getBrightness());
                                    deliverHSB = true;
                                    break;
                                case "colorSaturation":
                                    int saturationValue = Math.round(attributes.getFloat(key) * 100);
                                    hsbDevice = new HSBType(hsbDevice.getHue(), new PercentType(saturationValue),
                                            hsbDevice.getBrightness());
                                    hsbStateReflection = new HSBType(hsbStateReflection.getHue(),
                                            new PercentType(saturationValue), hsbStateReflection.getBrightness());
                                    deliverHSB = true;
                                    break;
                                case "lightLevel":
                                    int brightnessValue = attributes.getInt(key);
                                    // device needs the right values
                                    hsbDevice = new HSBType(hsbDevice.getHue(), hsbDevice.getSaturation(),
                                            new PercentType(brightnessValue));
                                    hsbStateReflection = new HSBType(hsbStateReflection.getHue(),
                                            hsbStateReflection.getSaturation(), new PercentType(brightnessValue));
                                    deliverHSB = true;
                                    break;
                            }
                            break;
                    }
                }
            }
            if (deliverHSB) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_COLOR), hsbStateReflection);
                if (!receiveCapabilities.contains(Model.COLOR_TEMPERATURE_CAPABILITY)) {
                    // if color light doesn't support native light temperature converted values are taken
                    long kelvin = Math.min(colorTemperatureMin,
                            Math.max(colorTemperatureMax, ColorModel.hsb2Kelvin(hsbStateReflection)));
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE),
                            new PercentType(getPercent(kelvin)));
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE_ABS),
                            QuantityType.valueOf(kelvin, Units.KELVIN));
                }
            }
        }
    }
}
