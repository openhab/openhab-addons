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
package org.openhab.binding.tapocontrol.internal.devices.camera;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;

/**
 * Configuration of a Tapo camera thing.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public record TapoCameraConfiguration(String ipAddress, int httpPort, String username, String password,
        int pollingInterval) {

    public static TapoCameraConfiguration from(Thing thing) {
        var config = thing.getConfiguration();
        return new TapoCameraConfiguration(//
                stringOrEmpty(config.get("ipAddress")), //
                intOr(config.get("httpPort"), 443), //
                defaultIfBlank(stringOrEmpty(config.get("username")), "admin"), //
                stringOrEmpty(config.get("password")), //
                intOr(config.get("pollingInterval"), 15));
    }

    private static String stringOrEmpty(@Nullable Object value) {
        return value instanceof String s ? s : "";
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value.isBlank() ? fallback : value;
    }

    private static int intOr(@Nullable Object value, int fallback) {
        return value instanceof Number n ? n.intValue() : fallback;
    }
}
