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
package org.openhab.binding.dirigera.internal.handler.controller;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Thing;

/**
 * The {@link DoubleShortcutControllerHandler} for triggering scenes
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DoubleShortcutControllerHandler extends BaseShortcutController {
    public TreeMap<String, String> relations = new TreeMap<>();

    public DoubleShortcutControllerHandler(Thing thing, Map<String, String> mapping, Storage<String> bindingStorage) {
        super(thing, mapping, bindingStorage);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readDevice(config.id);
            handleUpdate(values);

            // now register at gateway all device and scene ids
            String relationId = gateway().model().getRelationId(config.id);
            relations = gateway().model().getRelations(relationId);
            Entry<String, String> firstEntry = relations.firstEntry();
            String firstDeviceId = firstEntry.getKey();
            super.initializeScenes(firstDeviceId, CHANNEL_BUTTON_1);
            gateway().registerDevice(this, firstDeviceId);
            values = gateway().api().readDevice(firstDeviceId);
            handleUpdate(values);
            // double shortcut controller has 2 devices
            Entry<String, String> secondEntry = relations.higherEntry(firstEntry.getKey());
            String secondDeviceId = secondEntry.getKey();
            super.initializeScenes(secondDeviceId, CHANNEL_BUTTON_2);
            gateway().registerDevice(this, secondDeviceId);
            values = gateway().api().readDevice(secondDeviceId);
            handleUpdate(values);
        }
    }

    @Override
    public void dispose() {
        // remove device mapping
        relations.forEach((key, value) -> {
            gateway().unregisterDevice(this, key);
        });
        // super removes scene mapping
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        // delete device mapping
        relations.forEach((key, value) -> {
            gateway().deleteDevice(this, key);
        });
        // super deletes scenes from model
        super.handleRemoval();
    }
}
