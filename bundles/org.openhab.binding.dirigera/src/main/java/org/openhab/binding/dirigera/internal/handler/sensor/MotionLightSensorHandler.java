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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

/**
 * The {@link MotionLightSensorHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MotionLightSensorHandler extends MotionSensorHandler {

    private TreeMap<String, String> relations = new TreeMap<>();

    public MotionLightSensorHandler(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readDevice(config.id);
            handleUpdate(values);
            // assure deviceType is set from main device
            if (values.has(PROPERTY_DEVICE_TYPE)) {
                deviceType = values.getString(PROPERTY_DEVICE_TYPE);
            }

            // get all relations and register
            String relationId = gateway().model().getRelationId(config.id);
            relations = gateway().model().getRelations(relationId);
            // register for updates of twin devices
            relations.forEach((key, value) -> {
                gateway().registerDevice(this, key);
                JSONObject relationValues = gateway().api().readDevice(key);
                handleUpdate(relationValues);
            });
        }
    }

    @Override
    public void dispose() {
        relations.forEach((key, value) -> {
            gateway().unregisterDevice(this, key);
        });
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        relations.forEach((key, value) -> {
            gateway().deleteDevice(this, key);
        });
        super.handleRemoval();
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
                    if (CHANNEL_ILLUMINANCE.equals(targetChannel)) {
                        updateState(new ChannelUID(thing.getUID(), targetChannel),
                                QuantityType.valueOf(attributes.getInt(key), Units.LUX));
                    }
                }
            }
        }
    }
}
