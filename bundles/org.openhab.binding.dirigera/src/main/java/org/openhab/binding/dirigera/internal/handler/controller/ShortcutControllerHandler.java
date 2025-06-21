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

import static org.openhab.binding.dirigera.internal.Constants.CHANNEL_BUTTON_1;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Thing;

/**
 * The {@link ShortcutControllerHandler} for triggering scenes
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ShortcutControllerHandler extends BaseShortcutController {

    public ShortcutControllerHandler(Thing thing, Map<String, String> mapping, Storage<String> bindingStorage) {
        super(thing, mapping, bindingStorage);
        super.setChildHandler(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (super.checkHandler()) {
            JSONObject values = gateway().api().readDevice(config.id);
            handleUpdate(values);
            super.initializeScenes(config.id, CHANNEL_BUTTON_1);
        }
    }
}
