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
package org.openhab.binding.tibberpulselocal.internal.config;

import static org.openhab.binding.tibberpulselocal.internal.TibberPulseLocalBindingConstants.*;

import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration of the {@code meter} thing.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class MeterConfiguration {

    public static final int MIN_STALE_TIMEOUT = 10;
    public static final int DEFAULT_STALE_TIMEOUT = 60;

    private static final Pattern DEVICE_ID = Pattern.compile("[0-9a-fA-F]{16}");

    public String deviceId = "";
    public int staleTimeout = DEFAULT_STALE_TIMEOUT;

    /**
     * @return the device id in lower case, as it appears (normalised) in the stream messages
     */
    public String normalizedDeviceId() {
        return deviceId.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * @return the status description key that names the first invalid parameter, or null if the configuration is valid
     */
    public @Nullable String validate() {
        if (!DEVICE_ID.matcher(deviceId.trim()).matches()) {
            return TEXT_INVALID_DEVICE_ID;
        }
        if (staleTimeout < MIN_STALE_TIMEOUT) {
            return TEXT_INVALID_STALE_TIMEOUT;
        }
        return null;
    }
}
