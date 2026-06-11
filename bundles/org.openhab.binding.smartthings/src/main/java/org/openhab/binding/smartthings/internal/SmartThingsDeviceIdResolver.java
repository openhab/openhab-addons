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
package org.openhab.binding.smartthings.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;

/**
 * Resolves SmartThings device identifiers from Thing configuration.
 */
@NonNullByDefault
public final class SmartThingsDeviceIdResolver {
    private SmartThingsDeviceIdResolver() {
    }

    public static String getDeviceId(Thing thing) {
        Object configuredDeviceId = thing.getConfiguration().get(SmartThingsBindingConstants.DEVICE_ID);
        return configuredDeviceId instanceof String value && !value.isBlank() ? value : "";
    }

    public static boolean matches(Thing thing, @Nullable String deviceId) {
        return deviceId != null && !deviceId.isBlank() && deviceId.equals(getDeviceId(thing));
    }
}
