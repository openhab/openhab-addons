/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.types;

/**
 * Available Integra types.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.7.0
 */
public enum IntegraType {
    UNKNOWN(-1, "Unknown", 0, 0),
    I24(0, "Integra 24", 4, 24),
    I32(1, "Integra 32", 16, 32),
    I64(2, "Integra 64", 32, 64),
    I128(3, "Integra 128", 32, 128),
    I128_SIM300(4, "Integra 128-WRL SIM300", 32, 128),
    I128_LEON(132, "Integra 128-WRL LEON", 32, 128),
    I64_PLUS(66, "Integra 64 Plus", 32, 64),
    I128_PLUS(67, "Integra 128 Plus", 32, 128),
    I256_PLUS(72, "Integra 256 Plus", 32, 256, true);

    private int code;
    private String name;
    private int partitions;
    private int zones;
    private boolean extPayload;

    IntegraType(int code, String name, int partitions, int zones) {
        this(code, name, partitions, zones, false);
    }

    IntegraType(int code, String name, int partitions, int zones, boolean extPayload) {
        this.code = code;
        this.name = name;
        this.partitions = partitions;
        this.zones = zones;
        this.extPayload = extPayload;
    }

    /**
     * @return name of Integra type
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return max number of partitions
     */
    public int getPartitions() {
        return partitions;
    }

    /**
     * @return max number of zones
     */
    public int getZones() {
        return zones;
    }

    /**
     * @return <code>true</code> if this Integra requires extended message payload
     */
    public boolean hasExtPayload() {
        return this.extPayload;
    }

    /**
     * Returns Integra type for given code.
     *
     * @param code
     *            code to get type for
     * @return Integra type object
     */
    public static IntegraType valueOf(int code) {
        for (IntegraType val : IntegraType.values()) {
            if (val.code == code) {
                return val;
            }
        }
        return UNKNOWN;
    }
}
