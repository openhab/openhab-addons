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
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
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

    public ColorLightHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
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
        if (CHANNEL_LIGHT_TEMPERATURE.equals(channel)) {
            if (command instanceof PercentType percent) {
                // there are color lights which cannot handle temperature as stored in capabilities
                // in this case calculate color which is fitting to temperature
                // otherwise TemperatureLightHandler will do the command
                if (!receiveCapabilities.contains(Model.COLOR_TEMPERATURE_CAPABILITY)) {
                    int kelvin = super.getKelvin(percent.intValue());
                    HSBType colorTemp = getHSBTemperature(kelvin);
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
    }

    /**
     * Send hue and saturation to light device in case of difference is more than 2%
     *
     * @param hsb as requested color
     * @return true if color request is sent, false otherwise
     */
    private void colorCommand(HSBType hsb) {
        // if (!hsb.closeTo(hsbDevice, 0.02)) {
        /**
         * Why is closeTo not working on target?
         * 2024-11-09 09:28:21.191 [TRACE] [rnal.handler.light.ColorLightHandler] - DIRIGERA COLOR_LIGHT hsb too
         * close Device 63.0,0,33 Requested 63,100,0
         **/
        if (!closeTo(hsb)) {
            JSONObject colorAttributes = new JSONObject();
            colorAttributes.put("colorHue", hsb.getHue().intValue());
            colorAttributes.put("colorSaturation", hsb.getSaturation().intValue() / 100.0);
            super.changeProperty(LightCommand.Action.COLOR, colorAttributes);
        }
    }

    private boolean closeTo(HSBType hsb) {
        return (Math.abs(hsb.getHue().intValue() - hsbDevice.getHue().intValue()) < 3
                && Math.abs(hsb.getSaturation().intValue() - hsbDevice.getSaturation().intValue()) < 3);
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
                                    double saturationValue = Math.round(attributes.getDouble(key) * 100);
                                    hsbDevice = new HSBType(hsbDevice.getHue(), new PercentType((int) saturationValue),
                                            hsbDevice.getBrightness());
                                    hsbStateReflection = new HSBType(hsbStateReflection.getHue(),
                                            new PercentType((int) saturationValue), hsbStateReflection.getBrightness());
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
            }
        }
    }

    /**
     * Simulate color-temperature if color light doesn't have the "canReceive" "colorTemperature" capability
     * https://www.npmjs.com/package/color-temperature?activeTab=code
     *
     * @param kelvin
     * @return color temperature as HSBType
     */
    public static HSBType getHSBTemperature(long kelvin) {
        double temperature = kelvin / 100.0;
        double red;
        double green;
        double blue;

        /* Calculate red */
        if (temperature <= 66.0) {
            red = 255;
        } else {
            red = temperature - 60.0;
            red = 329.698727446 * Math.pow(red, -0.1332047592);
            if (red < 0) {
                red = 0;
            }
            if (red > 255) {
                red = 255;
            }
        }
        /* Calculate green */
        if (temperature <= 66.0) {
            green = temperature;
            green = 99.4708025861 * Math.log(green) - 161.1195681661;
            if (green < 0) {
                green = 0;
            }
            if (green > 255) {
                green = 255;
            }
        } else {
            green = temperature - 60.0;
            green = 288.1221695283 * Math.pow(green, -0.0755148492);
            if (green < 0) {
                green = 0;
            }
            if (green > 255) {
                green = 255;
            }
        }

        /* Calculate blue */
        if (temperature >= 66.0) {
            blue = 255;
        } else {
            if (temperature <= 19.0) {
                blue = 0;
            } else {
                blue = temperature - 10;
                blue = 138.5177312231 * Math.log(blue) - 305.0447927307;
                if (blue < 0) {
                    blue = 0;
                }
                if (blue > 255) {
                    blue = 255;
                }
            }
        }
        return HSBType.fromRGB((int) Math.round(red), (int) Math.round(green), (int) Math.round(blue));
    }
}
