/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dreame.internal.util;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.core.thing.Thing;

/**
 * Synchronizes cloud device metadata with openHAB Thing properties.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class DreameDeviceMetadata {

    private DreameDeviceMetadata() {
    }

    public static boolean updateProperties(Map<String, String> properties, DreameDevice device) {
        boolean changed = putIfPresent(properties, "model", device.model());
        return putIfPresent(properties, Thing.PROPERTY_FIRMWARE_VERSION, device.version()) || changed;
    }

    private static boolean putIfPresent(Map<String, String> properties, String key, String value) {
        if (value.isBlank() || value.equals(properties.get(key))) {
            return false;
        }
        properties.put(key, value);
        return true;
    }
}
