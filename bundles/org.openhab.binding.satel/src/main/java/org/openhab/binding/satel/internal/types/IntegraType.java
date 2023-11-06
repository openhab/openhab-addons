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
package org.openhab.binding.satel.internal.types;

/**
 * Available Integra types.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public enum IntegraType {
    UNKNOWN(-1, "Unknown", 0, 0, 0),
    I24(0, "Integra 24", 4, 24, 4),
    I32(1, "Integra 32", 16, 32, 8),
    I64(2, "Integra 64", 32, 64, 16),
    I128(3, "Integra 128", 32, 128, 16),
    I128_SIM300(4, "Integra 128-WRL SIM300", 32, 128, 8),
    I128_LEON(132, "Integra 128-WRL LEON", 32, 128, 8),
    I64_PLUS(66, "Integra 64 Plus", 32, 64, 16),
    I128_PLUS(67, "Integra 128 Plus", 32, 128, 16),
    I256_PLUS(72, "Integra 256 Plus", 32, 256, 16, true);

    private int code;
    private String name;
    private int partitions;
    private int zones;
    private int onMainboard;
    private boolean extPayload;

    IntegraType(int code, String name, int partitions, int zones, int onMainboard) {
        this(code, name, partitions, zones, onMainboard, false);
    }

    IntegraType(int code, String name, int partitions, int zones, int onMainboard, boolean extPayload) {
        this.code = code;
        this.name = name;
        this.partitions = partitions;
        this.zones = zones;
        this.onMainboard = onMainboard;
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
     * @return wired zones/outputs available on mainboard
     */
    public int getOnMainboard() {
        return onMainboard;
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
     * @param code code to get type for
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
