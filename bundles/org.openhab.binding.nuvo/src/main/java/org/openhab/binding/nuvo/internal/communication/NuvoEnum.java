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
package org.openhab.binding.nuvo.internal.communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the different internal zone and source IDs of the Nuvo Whole House Amplifier
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum NuvoEnum {

    SYSTEM("SYSTEM", "SYSTEM"),
    ZONE1("Z1", "ZCFG1"),
    ZONE2("Z2", "ZCFG2"),
    ZONE3("Z3", "ZCFG3"),
    ZONE4("Z4", "ZCFG4"),
    ZONE5("Z5", "ZCFG5"),
    ZONE6("Z6", "ZCFG6"),
    ZONE7("Z7", "ZCFG7"),
    ZONE8("Z8", "ZCFG8"),
    ZONE9("Z9", "ZCFG9"),
    ZONE10("Z10", "ZCFG10"),
    ZONE11("Z11", "ZCFG11"),
    ZONE12("Z12", "ZCFG12"),
    ZONE13("Z13", "ZCFG13"),
    ZONE14("Z14", "ZCFG14"),
    ZONE15("Z15", "ZCFG15"),
    ZONE16("Z16", "ZCFG16"),
    ZONE17("Z17", "ZCFG17"),
    ZONE18("Z18", "ZCFG18"),
    ZONE19("Z19", "ZCFG19"),
    ZONE20("Z20", "ZCFG20"),
    SOURCE1("S1", "SCFG1"),
    SOURCE2("S2", "SCFG2"),
    SOURCE3("S3", "SCFG3"),
    SOURCE4("S4", "SCFG4"),
    SOURCE5("S5", "SCFG5"),
    SOURCE6("S6", "SCFG6");

    private String id;
    private String cfgId;
    
    // make a map to enable lookup by the zone id value
    public static final Map<String, NuvoEnum> zoneMap = new HashMap<>();
    static {
        zoneMap.put(ZONE1.getId(), ZONE1);
        zoneMap.put(ZONE2.getId(), ZONE2);
        zoneMap.put(ZONE3.getId(), ZONE3);
        zoneMap.put(ZONE4.getId(), ZONE4);
        zoneMap.put(ZONE5.getId(), ZONE5);
        zoneMap.put(ZONE6.getId(), ZONE6);
        zoneMap.put(ZONE7.getId(), ZONE7);
        zoneMap.put(ZONE8.getId(), ZONE8);
        zoneMap.put(ZONE9.getId(), ZONE9);
        zoneMap.put(ZONE10.getId(), ZONE10);
        zoneMap.put(ZONE11.getId(), ZONE11);
        zoneMap.put(ZONE12.getId(), ZONE12);
        zoneMap.put(ZONE13.getId(), ZONE13);
        zoneMap.put(ZONE14.getId(), ZONE14);
        zoneMap.put(ZONE15.getId(), ZONE15);
        zoneMap.put(ZONE16.getId(), ZONE16);
        zoneMap.put(ZONE17.getId(), ZONE17);
        zoneMap.put(ZONE18.getId(), ZONE18);
        zoneMap.put(ZONE19.getId(), ZONE19);
        zoneMap.put(ZONE20.getId(), ZONE20);
    }
    
    // make a map to enable lookup by the source id value
    public static final Map<String, NuvoEnum> sourceMap = new HashMap<>();
    static {
        sourceMap.put(SOURCE1.getId(), SOURCE1);
        sourceMap.put(SOURCE2.getId(), SOURCE2);
        sourceMap.put(SOURCE3.getId(), SOURCE3);
        sourceMap.put(SOURCE4.getId(), SOURCE4);
        sourceMap.put(SOURCE5.getId(), SOURCE5);
        sourceMap.put(SOURCE6.getId(), SOURCE6);
    }
    
    // make a list of all valid source ids
    public static ArrayList<String> validSources = new ArrayList<String>( 
            Arrays.asList(SOURCE1.getId(),SOURCE2.getId(),SOURCE3.getId(),
                          SOURCE4.getId(),SOURCE5.getId(),SOURCE6.getId()));

    NuvoEnum(String id, String cfgId) {
        this.id = id;
        this.cfgId = cfgId;
    }

    /**
     * Get the id
     *
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the config id
     *
     * @return the config id
     */
    public String getConfigId() {
        return cfgId;
    }
}
