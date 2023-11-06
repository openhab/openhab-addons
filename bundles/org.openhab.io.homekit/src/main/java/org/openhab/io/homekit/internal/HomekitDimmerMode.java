/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.homekit.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Dimmer commands are handled differently by different devices.
 * Some devices expect only the brightness updates, some other expect brightness as well as "On/Off" commands.
 * This enum describes different modes of dimmer handling in the context of HomeKit binding.
 *
 * Following modes are supported:
 * DIMMER_MODE_NORMAL - no filtering. The commands will be sent to device as received from HomeKit.
 * DIMMER_MODE_FILTER_ON - ON events are filtered out. only OFF and brightness information are sent
 * DIMMER_MODE_FILTER_BRIGHTNESS_100 - only Brightness=100% is filtered out. everything else unchanged. This allows
 * custom logic for soft launch in devices.
 * DIMMER_MODE_FILTER_ON_EXCEPT_BRIGHTNESS_100 - ON events are filtered out in all cases except of Brightness = 100%.
 *
 * @author Eugen Freiter - Initial contribution
 */

@NonNullByDefault
public enum HomekitDimmerMode {
    DIMMER_MODE_NORMAL("normal"),
    DIMMER_MODE_FILTER_ON("filterOn"),
    DIMMER_MODE_FILTER_BRIGHTNESS_100("filterBrightness100"),
    DIMMER_MODE_FILTER_ON_EXCEPT_BRIGHTNESS_100("filterOnExceptBrightness100");

    private static final Map<String, HomekitDimmerMode> TAG_MAP = Arrays.stream(HomekitDimmerMode.values())
            .collect(Collectors.toMap(type -> type.tag.toUpperCase(), type -> type));

    private final String tag;

    private HomekitDimmerMode(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static Optional<HomekitDimmerMode> valueOfTag(String tag) {
        return Optional.ofNullable(TAG_MAP.get(tag.toUpperCase()));
    }
}
