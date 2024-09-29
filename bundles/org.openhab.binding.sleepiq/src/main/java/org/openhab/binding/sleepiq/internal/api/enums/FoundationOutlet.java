/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.internal.api.enums;

import static org.openhab.binding.sleepiq.internal.SleepIQBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoundationOutlet} represents the outlets available on the foundation.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum FoundationOutlet {
    RIGHT_NIGHT_STAND(1),
    LEFT_NIGHT_STAND(2),
    RIGHT_UNDER_BED_LIGHT(3),
    LEFT_UNDER_BED_LIGHT(4);

    private final int outlet;

    FoundationOutlet(final int outlet) {
        this.outlet = outlet;
    }

    public int value() {
        return outlet;
    }

    public static FoundationOutlet forValue(int value) {
        for (FoundationOutlet s : FoundationOutlet.values()) {
            if (s.outlet == value) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid outlet: " + value);
    }

    public static FoundationOutlet convertFromChannelId(String channelId) {
        FoundationOutlet localOutlet;
        switch (channelId) {
            case CHANNEL_RIGHT_NIGHT_STAND_OUTLET:
                localOutlet = FoundationOutlet.RIGHT_NIGHT_STAND;
                break;
            case CHANNEL_LEFT_NIGHT_STAND_OUTLET:
                localOutlet = FoundationOutlet.LEFT_NIGHT_STAND;
                break;
            case CHANNEL_RIGHT_UNDER_BED_LIGHT:
                localOutlet = FoundationOutlet.RIGHT_UNDER_BED_LIGHT;
                break;
            case CHANNEL_LEFT_UNDER_BED_LIGHT:
                localOutlet = FoundationOutlet.LEFT_UNDER_BED_LIGHT;
                break;
            default:
                throw new IllegalArgumentException("Can't convert channel to outlet: " + channelId);
        }
        return localOutlet;
    }

    @Override
    public String toString() {
        return String.valueOf(outlet);
    }
}
