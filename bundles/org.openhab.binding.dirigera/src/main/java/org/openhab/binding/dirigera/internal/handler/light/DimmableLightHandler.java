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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * {@link DimmableLightHandler} for lights with brightness
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DimmableLightHandler extends BaseLight {
    protected int currentBrightness = 0;

    public DimmableLightHandler(Thing thing, Map<String, String> mapping) {
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        String channel = channelUID.getIdWithoutGroup();
        String targetProperty = channel2PropertyMap.get(channel);
        if (targetProperty != null) {
            switch (channel) {
                case CHANNEL_LIGHT_BRIGHTNESS:
                    if (command instanceof PercentType percent) {
                        int percentValue = percent.intValue();
                        // switch on or off depending on brightness ...
                        if (percentValue > 0) {
                            // first change brightness to be stored for power ON ...
                            if (Math.abs(percentValue - currentBrightness) > 1) {
                                JSONObject brightnessAttributes = new JSONObject();
                                brightnessAttributes.put(targetProperty, percent.intValue());
                                super.changeProperty(LightCommand.Action.BRIGHTNESS, brightnessAttributes);
                            }
                            // .. then switch power
                            if (!isPowered()) {
                                super.addOnOffCommand(true);
                            }
                        } else {
                            super.addOnOffCommand(false);
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
                        case CHANNEL_LIGHT_BRIGHTNESS:
                            // set new currentBrightness as received and continue with update depending on power state
                            currentBrightness = attributes.getInt(key);
                        case CHANNEL_POWER_STATE:
                            /**
                             * Power state changed
                             * on - report last received brightness
                             * off - deliver brightness 0
                             */
                            if (isPowered()) {
                                updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_BRIGHTNESS),
                                        new PercentType(currentBrightness));
                            } else {
                                updateState(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_BRIGHTNESS),
                                        new PercentType(0));
                            }
                            break;
                    }
                }
            }
        }
    }
}
