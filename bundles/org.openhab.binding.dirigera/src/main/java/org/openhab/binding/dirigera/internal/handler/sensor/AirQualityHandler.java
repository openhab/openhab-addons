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
package org.openhab.binding.dirigera.internal.handler.sensor;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link AirQualityHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AirQualityHandler extends BaseHandler {

    public AirQualityHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        // no link support for Scenes
        hardLinks = Arrays.asList();
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
    }

    @Override
    public void handleUpdate(JSONObject update) {
        // handle reachable flag
        super.handleUpdate(update);
        // now device specific
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            Iterator<String> attributesIterator = attributes.keys();
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    switch (targetChannel) {
                        case CHANNEL_TEMPERATURE:
                            double temperature = Math.round(attributes.getDouble(key) * 10) / 10.0;
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_TEMPERATURE),
                                    QuantityType.valueOf(temperature, SIUnits.CELSIUS));
                            break;
                        case CHANNEL_HUMIDITY:
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_HUMIDITY),
                                    QuantityType.valueOf(attributes.getDouble(key), Units.PERCENT));
                            break;
                        case CHANNEL_PARTICULATE_MATTER:
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_PARTICULATE_MATTER),
                                    QuantityType.valueOf(attributes.getDouble(key), Units.MICROGRAM_PER_CUBICMETRE));
                            break;
                        case CHANNEL_VOC_INDEX:
                            updateState(new ChannelUID(thing.getUID(), CHANNEL_VOC_INDEX),
                                    new DecimalType(attributes.getDouble(key)));
                            break;
                    }
                }
            }
        }
    }
}
