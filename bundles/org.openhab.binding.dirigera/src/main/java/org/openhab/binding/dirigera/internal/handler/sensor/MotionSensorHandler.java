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
package org.openhab.binding.dirigera.internal.handler.sensor;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link MotionSensorHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MotionSensorHandler extends BaseHandler {

    protected static final String durationUpdate = "{\"attributes\":{\"sensorConfig\":{\"onDuration\":%s}}}";

    public MotionSensorHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        // links of types which can be established towards this device
        linkCandidateTypes = List.of(DEVICE_TYPE_LIGHT, DEVICE_TYPE_OUTLET);
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
        String targetChannel = channelUID.getIdWithoutGroup();
        switch (targetChannel) {
            case CHANNEL_ACTIVE_DURATION:
                int seconds = -1;
                if (command instanceof DecimalType decimal) {
                    seconds = decimal.intValue();
                } else if (command instanceof QuantityType<?> quantity) {
                    QuantityType<?> secondsQunatity = quantity.toUnit(Units.SECOND);
                    if (secondsQunatity != null) {
                        seconds = secondsQunatity.intValue();
                    }
                }
                if (seconds > 0) {
                    String updateData = String.format(durationUpdate, seconds);
                    gateway().api().sendPatch(config.id, new JSONObject(updateData));
                }
                break;
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
            while (attributesIterator.hasNext()) {
                String key = attributesIterator.next();
                String targetChannel = property2ChannelMap.get(key);
                if (targetChannel != null) {
                    switch (targetChannel) {
                        case CHANNEL_DETECTION:
                            updateState(new ChannelUID(thing.getUID(), targetChannel),
                                    OnOffType.from(attributes.getBoolean(key)));
                            break;
                        case CHANNEL_ACTIVE_DURATION:
                            if (attributes.has("sensorConfig")) {
                                JSONObject sensorConfig = attributes.getJSONObject("sensorConfig");
                                if (sensorConfig.has("onDuration")) {
                                    int duration = sensorConfig.getInt("onDuration");
                                    updateState(new ChannelUID(thing.getUID(), targetChannel),
                                            QuantityType.valueOf(duration, Units.SECOND));
                                }
                            }
                            break;
                    }
                }
            }
        }
    }
}
