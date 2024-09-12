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
package org.openhab.binding.nuvo.internal.communication;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the different internal zone and source IDs of the Nuvo Whole House Amplifier
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum NuvoEnum {
    SYSTEM("SYSTEM", "SYSTEM", 0),
    ZONE1("Z1", "ZCFG1", 1),
    ZONE2("Z2", "ZCFG2", 2),
    ZONE3("Z3", "ZCFG3", 3),
    ZONE4("Z4", "ZCFG4", 4),
    ZONE5("Z5", "ZCFG5", 5),
    ZONE6("Z6", "ZCFG6", 6),
    ZONE7("Z7", "ZCFG7", 7),
    ZONE8("Z8", "ZCFG8", 8),
    ZONE9("Z9", "ZCFG9", 9),
    ZONE10("Z10", "ZCFG10", 10),
    ZONE11("Z11", "ZCFG11", 11),
    ZONE12("Z12", "ZCFG12", 12),
    ZONE13("Z13", "ZCFG13", 13),
    ZONE14("Z14", "ZCFG14", 14),
    ZONE15("Z15", "ZCFG15", 15),
    ZONE16("Z16", "ZCFG16", 16),
    ZONE17("Z17", "ZCFG17", 17),
    ZONE18("Z18", "ZCFG18", 18),
    ZONE19("Z19", "ZCFG19", 19),
    ZONE20("Z20", "ZCFG20", 20),
    SOURCE1("S1", "SCFG1", 1),
    SOURCE2("S2", "SCFG2", 2),
    SOURCE3("S3", "SCFG3", 3),
    SOURCE4("S4", "SCFG4", 4),
    SOURCE5("S5", "SCFG5", 5),
    SOURCE6("S6", "SCFG6", 6);

    private final String id;
    private final String cfgId;
    private final int num;

    // make a list of all valid source enums
    public static final List<NuvoEnum> VALID_SOURCES = Arrays.stream(values()).filter(s -> s.name().contains("SOURCE"))
            .toList();

    NuvoEnum(String id, String cfgId, int num) {
        this.id = id;
        this.cfgId = cfgId;
        this.num = num;
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

    /**
     * Get the num
     *
     * @return the num
     */
    public int getNum() {
        return num;
    }
}
