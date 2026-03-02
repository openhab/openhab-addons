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
package org.openhab.binding.viessmann.internal.util;

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.viessmann.internal.handler.DeviceHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * The {@link ViessmannUtil} class provides utility methods for the Viessmann binding.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class ViessmannUtil {

    private ViessmannUtil() {
        throw new AssertionError("No instances allowed");
    }

    /**
     * Converts camelCase to hyphenated lowercase.
     *
     * @param input camelCase string
     * @return hyphenated lowercase string
     */
    public static String camelToHyphen(String input) {
        String result = input.replaceAll("([a-z])([A-Z])", "$1-$2").replaceAll("([A-Z])([A-Z][a-z])", "$1-$2");
        result = result.replaceAll("([a-zA-Z])([0-9]+)", "$1-$2").replaceAll("([0-9]+)([a-zA-Z])", "$1-$2");
        return result.toLowerCase(Locale.ROOT);
    }

    /**
     * Converts a hyphen-separated string to camelCase or UpperCamelCase.
     *
     * @param input the hyphen-separated input (e.g. {@code "flow-temperature"})
     * @param capitalizeFirst {@code true} for UpperCamelCase, {@code false} for camelCase
     * @return the converted string, or the input if {@code null} or blank
     */
    public static @Nullable String hyphenToCamel(@Nullable String input, boolean capitalizeFirst) {
        if (input == null || input.isBlank()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean upperNext = capitalizeFirst;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '-') {
                upperNext = true;
                continue;
            }

            if (upperNext) {
                result.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    @SuppressWarnings("null")
    public static ZoneId getOpenHABZoneId() {
        BundleContext ctx = FrameworkUtil.getBundle(DeviceHandler.class).getBundleContext();
        if (ctx == null) {
            return ZoneId.systemDefault();
        }
        ServiceReference<TimeZoneProvider> ref = ctx.getServiceReference(TimeZoneProvider.class);
        if (ref != null) {
            TimeZoneProvider tzProvider = ctx.getService(ref);
            TimeZone tz = TimeZone.getTimeZone(tzProvider.getTimeZone());
            return tz.toZoneId();
        }
        return ZoneId.systemDefault();
    }
}
