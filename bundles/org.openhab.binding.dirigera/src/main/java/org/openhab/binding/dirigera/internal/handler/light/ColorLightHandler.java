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

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.model.Model;
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
 * The {@link ColorLightHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ColorLightHandler extends BaseHandler {
    private final Logger logger = LoggerFactory.getLogger(ColorLightHandler.class);

    private HSBType hsbCurrent;

    public ColorLightHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        PercentType pt = new PercentType(50);
        hsbCurrent = new HSBType(new DecimalType(50), pt, pt);
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
        String channel = channelUID.getIdWithoutGroup();
        logger.trace("DIRIGERA LIGHT_DEVICE handle command {} for {}", command, channel);

        String targetProperty = channel2PropertyMap.get(channel);
        if (targetProperty != null) {
            if (command instanceof HSBType hsb) {
                boolean colorSendToAPI = false;
                if (Math.round(hsb.getHue().intValue()) == Math.round(hsbCurrent.getHue().intValue()) && Math
                        .round(hsb.getSaturation().intValue()) == Math.round(hsbCurrent.getSaturation().intValue())) {
                    logger.trace("DIRIGERA LIGHT_DEVICE hno need to update color, it's the same");
                } else {
                    JSONObject colorAttributes = new JSONObject();
                    colorAttributes.put("colorHue", hsb.getHue().intValue());
                    colorAttributes.put("colorSaturation", Math.round(hsb.getSaturation().doubleValue() / 100));
                    logger.trace("DIRIGERA LIGHT_DEVICE send to API {}", colorAttributes);
                    gateway().api().sendPatch(config.id, colorAttributes);
                    colorSendToAPI = true;
                }
                if (hsb.getBrightness().intValue() == hsbCurrent.getBrightness().intValue()) {
                    logger.trace("DIRIGERA LIGHT_DEVICE hno need to update brightness, it's the same");
                } else {
                    if (colorSendToAPI) {
                        // seems that IKEA lamps cannot handle consecutive calls it really short time frame
                        // so give it 100ms pause until next call
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    JSONObject brightnessattributes = new JSONObject();
                    brightnessattributes.put("lightLevel", hsb.getBrightness().intValue());
                    logger.trace("DIRIGERA LIGHT_DEVICE send to API {}", brightnessattributes);
                    gateway().api().sendPatch(config.id, brightnessattributes);
                }
            } else if (command instanceof OnOffType onOff) {
                JSONObject attributes = new JSONObject();
                attributes.put(targetProperty, onOff.equals(OnOffType.ON));
                logger.trace("DIRIGERA LIGHT_DEVICE send to API {}", attributes);
                gateway().api().sendPatch(config.id, attributes);
            }
        } else {
            logger.trace("DIRIGERA LIGHT_DEVICE no property found for channel {}", channel);
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        // handle reachable flag
        super.handleUpdate(update);
        // now device specific
        if (update.has(Model.ATTRIBUTES)) {
            boolean deliverHSB = false;
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    if (CHANNEL_LIGHT_HSB.equals(targetChannel)) {
                        switch (key) {
                            case "colorHue":
                                double hueValue = attributes.getInt(key);
                                hsbCurrent = new HSBType(new DecimalType(hueValue), hsbCurrent.getSaturation(),
                                        hsbCurrent.getBrightness());
                                deliverHSB = true;
                                break;
                            case "colorSaturation":
                                double saturationValue = Math.round(attributes.getDouble(key) * 100);
                                logger.trace("DIRIGERA LIGHT_DEVICE new Saturation value {} {}", saturationValue,
                                        (int) saturationValue);
                                hsbCurrent = new HSBType(hsbCurrent.getHue(), new PercentType((int) saturationValue),
                                        hsbCurrent.getBrightness());
                                deliverHSB = true;
                                break;
                            case "lightLevel":
                                int brightnessValue = attributes.getInt(key);
                                hsbCurrent = new HSBType(hsbCurrent.getHue(), hsbCurrent.getSaturation(),
                                        new PercentType(brightnessValue));
                                deliverHSB = true;
                                break;
                        }
                    } else if (CHANNEL_STATE.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                OnOffType.from(attributes.getBoolean(key)));
                    } else {
                        logger.trace("DIRIGERA LIGHT_DEVICE no channel for {} available", key);
                    }
                } else {
                    logger.trace("DIRIGERA LIGHT_DEVICE no targetChannel for {}", key);
                }
            }
            logger.trace("DIRIGERA LIGHT_DEVICE deliver {} ? {}", hsbCurrent, deliverHSB);
            if (deliverHSB) {
                updateState(new ChannelUID(thing.getUID(), "hsb"), hsbCurrent);
            }
        }
    }
}
