/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.monopriceaudio.internal.communication;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.monopriceaudio.internal.MonopriceAudioException;

/**
 * Represents the different internal zone IDs of the Monoprice Whole House Amplifier
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum MonopriceAudioZone {

    ALL("all"),
    ZONE1("11"),
    ZONE2("12"),
    ZONE3("13"),
    ZONE4("14"),
    ZONE5("15"),
    ZONE6("16"),
    ZONE7("21"),
    ZONE8("22"),
    ZONE9("23"),
    ZONE10("24"),
    ZONE11("25"),
    ZONE12("26"),
    ZONE13("31"),
    ZONE14("32"),
    ZONE15("33"),
    ZONE16("34"),
    ZONE17("35"),
    ZONE18("36");

    private final String zoneId;

    // make a list of all valid zone names
    public static final List<String> VALID_ZONES = Arrays.stream(values()).filter(z -> z != ALL)
            .map(MonopriceAudioZone::name).collect(Collectors.toList());

    // make a list of all valid zone ids
    public static final List<String> VALID_ZONE_IDS = Arrays.stream(values()).filter(z -> z != ALL)
            .map(MonopriceAudioZone::getZoneId).collect(Collectors.toList());

    public static MonopriceAudioZone fromZoneId(String zoneId) throws MonopriceAudioException {
        return Arrays.stream(values()).filter(z -> z.zoneId.equalsIgnoreCase(zoneId)).findFirst()
                .orElseThrow(() -> new MonopriceAudioException("Invalid zoneId specified: " + zoneId));
    }

    MonopriceAudioZone(String zoneId) {
        this.zoneId = zoneId;
    }

    /**
     * Get the zone id
     *
     * @return the zone id
     */
    public String getZoneId() {
        return zoneId;
    }
}
