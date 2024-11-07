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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
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
 * The {@link ColorLightHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ColorLightHandler extends BaseHandler {
    private final Logger logger = LoggerFactory.getLogger(ColorLightHandler.class);

    private List<LightUpdate> lightRequestQueue = new ArrayList<>();
    private int colorBrightnessDelayMS = 1250;
    private HSBType hsbCurrent;

    public ColorLightHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        PercentType pt = new PercentType(50);
        hsbCurrent = new HSBType(new DecimalType(50), pt, pt);
        // links of types which can be established towards this device
        linkCandidateTypes = List.of(DEVICE_TYPE_LIGHT_CONTROLLER, DEVICE_TYPE_MOTION_SENSOR);
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
        logger.trace("DIRIGERA LIGHT_DEVICE handle {} {} {}", channelUID, command, hsbCurrent);
        super.handleCommand(channelUID, command);
        String channel = channelUID.getIdWithoutGroup();

        String targetProperty = channel2PropertyMap.get(channel);
        if (targetProperty != null) {
            if (command instanceof HSBType hsb) {
                boolean colorSendToAPI = false;
                int differenceHue = Math.abs(hsb.getHue().intValue() - hsbCurrent.getHue().intValue());
                int saturationDifference = Math
                        .abs(hsb.getSaturation().intValue() - hsbCurrent.getSaturation().intValue());
                if (differenceHue > 2 || saturationDifference > 2) {
                    JSONObject colorAttributes = new JSONObject();
                    colorAttributes.put("colorHue", hsb.getHue().intValue());
                    colorAttributes.put("colorSaturation", hsb.getSaturation().intValue() / 100.0);
                    synchronized (lightRequestQueue) {
                        lightRequestQueue.add(new LightUpdate(colorAttributes, LightUpdate.Action.COLOR));
                    }
                    scheduler.execute(this::doUpdate);
                    colorSendToAPI = true;
                }
                int requestedBrightness = hsb.getBrightness().intValue();
                int currentBrightness = hsbCurrent.getBrightness().intValue();
                if (Math.abs(requestedBrightness - currentBrightness) > 2 || requestedBrightness == 0) {
                    if (requestedBrightness > 0) {
                        if (!isOn()) {
                            super.handleCommand(new ChannelUID(channelUID.getThingUID(), CHANNEL_POWER_STATE),
                                    OnOffType.ON);
                        }
                        JSONObject brightnessattributes = new JSONObject();
                        brightnessattributes.put("lightLevel", hsb.getBrightness().intValue());
                        synchronized (lightRequestQueue) {
                            lightRequestQueue.add(new LightUpdate(brightnessattributes, LightUpdate.Action.BRIGHTNESS));
                        }
                        if (colorSendToAPI) {
                            /**
                             * IKEA lamps cannot handle consecutive calls for color and brightness due to fading
                             * activity
                             * So first fade to color then fade brightness
                             */
                            scheduler.schedule(this::doUpdate, colorBrightnessDelayMS, TimeUnit.MILLISECONDS);
                        } else {
                            scheduler.execute(this::doUpdate);
                        }
                    } else {
                        super.handleCommand(new ChannelUID(channelUID.getThingUID(), CHANNEL_POWER_STATE),
                                OnOffType.OFF);
                    }

                }
            } else if (command instanceof OnOffType) {
                super.handleCommand(new ChannelUID(channelUID.getThingUID(), CHANNEL_POWER_STATE), command);
            } else if (command instanceof PercentType percent) {
                HSBType newHsb = new HSBType(hsbCurrent.getHue(), hsbCurrent.getSaturation(), percent);
                this.handleCommand(new ChannelUID(channelUID.getThingUID(), CHANNEL_LIGHT_HSB), newHsb);
            } else {
                logger.trace("DIRIGERA LIGHT_DEVICE type not known {}", command.getClass());
            }
        }
    }

    private void doUpdate() {
        LightUpdate request = null;
        synchronized (lightRequestQueue) {
            if (!lightRequestQueue.isEmpty()) {
                request = lightRequestQueue.remove(0);
            } else {
                return;
            }
            if (lightRequestQueue.contains(request)) {
                logger.trace("DIRIGERA LIGHT_DEVICE dismiss light request and wait for next one {}",
                        lightRequestQueue.size());
            }
            if (!lightRequestQueue.isEmpty()) {
                logger.trace("DIRIGERA LIGHT_DEVICE queue size {}", lightRequestQueue.size());
            }
        }
        if (request != null) {
            gateway().api().sendAttributes(config.id, request.request);
        }
    }

    @Override
    public void handleUpdate(JSONObject update) {
        super.handleUpdate(update);
        if (update.has(Model.ATTRIBUTES)) {
            boolean deliverHSB = false;
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    switch (targetChannel) {
                        case CHANNEL_LIGHT_HSB:
                            switch (key) {
                                case "colorHue":
                                    double hueValue = attributes.getInt(key);
                                    hsbCurrent = new HSBType(new DecimalType(hueValue), hsbCurrent.getSaturation(),
                                            hsbCurrent.getBrightness());
                                    deliverHSB = true;
                                    break;
                                case "colorSaturation":
                                    double saturationValue = Math.round(attributes.getDouble(key) * 100);
                                    hsbCurrent = new HSBType(hsbCurrent.getHue(),
                                            new PercentType((int) saturationValue), hsbCurrent.getBrightness());
                                    deliverHSB = true;
                                    break;
                                case "lightLevel":
                                    int brightnessValue = attributes.getInt(key);
                                    hsbCurrent = new HSBType(hsbCurrent.getHue(), hsbCurrent.getSaturation(),
                                            new PercentType(brightnessValue));
                                    deliverHSB = true;
                                    break;
                            }
                            break;
                    }
                }
            }
            if (deliverHSB) {
                updateState(new ChannelUID(thing.getUID(), "hsb"), hsbCurrent);
            }
        }
    }
}
