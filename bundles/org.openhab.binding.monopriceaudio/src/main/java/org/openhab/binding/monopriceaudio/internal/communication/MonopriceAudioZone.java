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
import java.util.HashMap;
import java.util.Map;

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

    // make a map to enable lookup by the zone id value
    public static final Map<String, MonopriceAudioZone> ZONE_MAP = new HashMap<>();
    static {
        ZONE_MAP.put(ZONE1.getZoneId(), ZONE1);
        ZONE_MAP.put(ZONE2.getZoneId(), ZONE2);
        ZONE_MAP.put(ZONE3.getZoneId(), ZONE3);
        ZONE_MAP.put(ZONE4.getZoneId(), ZONE4);
        ZONE_MAP.put(ZONE5.getZoneId(), ZONE5);
        ZONE_MAP.put(ZONE6.getZoneId(), ZONE6);
        ZONE_MAP.put(ZONE7.getZoneId(), ZONE7);
        ZONE_MAP.put(ZONE8.getZoneId(), ZONE8);
        ZONE_MAP.put(ZONE9.getZoneId(), ZONE9);
        ZONE_MAP.put(ZONE10.getZoneId(), ZONE10);
        ZONE_MAP.put(ZONE11.getZoneId(), ZONE11);
        ZONE_MAP.put(ZONE12.getZoneId(), ZONE12);
        ZONE_MAP.put(ZONE13.getZoneId(), ZONE13);
        ZONE_MAP.put(ZONE14.getZoneId(), ZONE14);
        ZONE_MAP.put(ZONE15.getZoneId(), ZONE15);
        ZONE_MAP.put(ZONE16.getZoneId(), ZONE16);
        ZONE_MAP.put(ZONE17.getZoneId(), ZONE17);
        ZONE_MAP.put(ZONE18.getZoneId(), ZONE18);
    }

    // make a map to enable lookup of controller zone id by binding zone id (1-18)
    public static final Map<String, String> BINDING_ZONE_MAP = new HashMap<>();
    static {
        BINDING_ZONE_MAP.put("1", ZONE1.getZoneId());
        BINDING_ZONE_MAP.put("2", ZONE2.getZoneId());
        BINDING_ZONE_MAP.put("3", ZONE3.getZoneId());
        BINDING_ZONE_MAP.put("4", ZONE4.getZoneId());
        BINDING_ZONE_MAP.put("5", ZONE5.getZoneId());
        BINDING_ZONE_MAP.put("6", ZONE6.getZoneId());
        BINDING_ZONE_MAP.put("7", ZONE7.getZoneId());
        BINDING_ZONE_MAP.put("8", ZONE8.getZoneId());
        BINDING_ZONE_MAP.put("9", ZONE9.getZoneId());
        BINDING_ZONE_MAP.put("10", ZONE10.getZoneId());
        BINDING_ZONE_MAP.put("11", ZONE11.getZoneId());
        BINDING_ZONE_MAP.put("12", ZONE12.getZoneId());
        BINDING_ZONE_MAP.put("13", ZONE13.getZoneId());
        BINDING_ZONE_MAP.put("14", ZONE14.getZoneId());
        BINDING_ZONE_MAP.put("15", ZONE15.getZoneId());
        BINDING_ZONE_MAP.put("16", ZONE16.getZoneId());
        BINDING_ZONE_MAP.put("17", ZONE17.getZoneId());
        BINDING_ZONE_MAP.put("18", ZONE18.getZoneId());
    }

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
