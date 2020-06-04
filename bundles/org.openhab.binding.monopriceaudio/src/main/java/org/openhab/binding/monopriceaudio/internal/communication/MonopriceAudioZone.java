/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

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

    private String zoneId;

    // make a list of all valid zone ids
    public static final ArrayList<String> VALID_ZONES = new ArrayList<String>(
            Arrays.asList(ZONE1.getZoneId(), ZONE2.getZoneId(), ZONE3.getZoneId(), ZONE4.getZoneId(), ZONE5.getZoneId(),
                    ZONE6.getZoneId(), ZONE7.getZoneId(), ZONE8.getZoneId(), ZONE9.getZoneId(), ZONE10.getZoneId(),
                    ZONE11.getZoneId(), ZONE12.getZoneId(), ZONE13.getZoneId(), ZONE14.getZoneId(), ZONE15.getZoneId(),
                    ZONE16.getZoneId(), ZONE17.getZoneId(), ZONE18.getZoneId()));

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
