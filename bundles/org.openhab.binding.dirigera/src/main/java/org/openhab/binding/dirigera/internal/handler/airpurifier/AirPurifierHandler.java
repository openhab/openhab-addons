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
package org.openhab.binding.dirigera.internal.handler.airpurifier;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link AirPurifierHandler} for handling air cleaning devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AirPurifierHandler extends BaseHandler {

    /**
     * see
     * https://github.com/dvdgeisler/DirigeraClient/blob/a760b4419a8b1adf469d14a6ce4e750e52d4d540/dirigera-client-api/src/main/java/de/dvdgeisler/iot/dirigera/client/api/model/device/airpurifier/AirPurifierFanMode.java#L5
     **/
    public static final Map<String, Integer> FAN_MODES = Map.of("auto", 0, "low", 1, "medium", 2, "high", 3, "on", 4,
            "off", 5);
    /**
     * see
     * https://github.com/Leggin/dirigera/blob/790a3151d8b61151dcd31f2194297dc8d4d89640/src/dirigera/devices/air_purifier.py#L61
     **/
    public static final int FAN_SPEED_MAX = 50;
    public static Map<Integer, String> fanModeToState = reverseStateMapping(FAN_MODES);

    public AirPurifierHandler(Thing thing, Map<String, String> mapping) {
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
                case CHANNEL_CHILD_LOCK:
                case CHANNEL_DISABLE_STATUS_LIGHT:
                    if (command instanceof OnOffType onOff) {
                        JSONObject onOffAttributes = new JSONObject();
                        onOffAttributes.put(targetProperty, OnOffType.ON.equals(onOff));
                        super.sendAttributes(onOffAttributes);
                    }
                    break;
                case CHANNEL_PURIFIER_FAN_SPEED:
                    if (command instanceof PercentType percent) {
                        long speedAbs = Math.round(percent.intValue() * FAN_SPEED_MAX / 100.0);
                        JSONObject fanSpeedAttributes = new JSONObject();
                        fanSpeedAttributes.put(targetProperty, speedAbs);
                        super.sendAttributes(fanSpeedAttributes);
                    }
                    break;
                case CHANNEL_PURIFIER_FAN_MODE:
                    if (command instanceof DecimalType decimal) {
                        int fanMode = decimal.intValue();
                        String fanModeAttribute = fanModeToState.get(fanMode);
                        if (fanModeAttribute != null) {
                            JSONObject fanModeAttributes = new JSONObject();
                            fanModeAttributes.put(targetProperty, fanModeAttribute);
                            super.sendAttributes(fanModeAttributes);
                        }
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
                        case CHANNEL_PURIFIER_FAN_MODE:
                            String fanMode = attributes.getString(key);
                            Integer fanModeNumber = FAN_MODES.get(fanMode);
                            if (fanModeNumber != null) {
                                updateState(new ChannelUID(thing.getUID(), targetChannel),
                                        new DecimalType(fanModeNumber));
                            }
                            break;
                        case CHANNEL_PURIFIER_FAN_SPEED:
                            float speed = attributes.getFloat(key);
                            speed = Math.max(Math.min(speed, FAN_SPEED_MAX), 0);
                            int percent = Math.round(speed * 100 / FAN_SPEED_MAX);
                            updateState(new ChannelUID(thing.getUID(), targetChannel), new PercentType(percent));
                            break;
                        case CHANNEL_PURIFIER_FAN_RUNTIME:
                        case CHANNEL_PURIFIER_FILTER_LIFETIME:
                            updateState(new ChannelUID(thing.getUID(), targetChannel),
                                    QuantityType.valueOf(attributes.getDouble(key), Units.MINUTE));
                            break;
                        case CHANNEL_PURIFIER_FILTER_ELAPSED:
                            updateState(new ChannelUID(thing.getUID(), targetChannel),
                                    QuantityType.valueOf(attributes.getDouble(key), Units.MINUTE));
                            State lifeTimeState = channelStateMap.get(CHANNEL_PURIFIER_FILTER_LIFETIME);
                            if (lifeTimeState != null && lifeTimeState instanceof QuantityType) {
                                int elapsed = attributes.getInt(key);
                                int lifetime = ((QuantityType<?>) lifeTimeState).intValue();
                                updateState(new ChannelUID(thing.getUID(), CHANNEL_PURIFIER_FILTER_REMAIN),
                                        QuantityType.valueOf(lifetime - elapsed, Units.MINUTE));
                            }
                            break;
                        case CHANNEL_PARTICULATE_MATTER:
                            updateState(new ChannelUID(thing.getUID(), targetChannel),
                                    QuantityType.valueOf(attributes.getDouble(key), Units.MICROGRAM_PER_CUBICMETRE));
                            break;
                        case CHANNEL_PURIFIER_FILTER_ALARM:
                        case CHANNEL_CHILD_LOCK:
                        case CHANNEL_DISABLE_STATUS_LIGHT:
                            updateState(new ChannelUID(thing.getUID(), targetChannel),
                                    OnOffType.from(attributes.getBoolean(key)));
                    }
                }
            }
        }
    }
}
