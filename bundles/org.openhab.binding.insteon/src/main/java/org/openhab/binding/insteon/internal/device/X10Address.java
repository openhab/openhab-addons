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
package org.openhab.binding.insteon.internal.device;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * THe {@link X10Address} represents an X10 address
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class X10Address implements DeviceAddress {
    private static final Map<String, Integer> HOUSE_CODES = Map.ofEntries(Map.entry("A", 0x06), Map.entry("B", 0x0E),
            Map.entry("C", 0x02), Map.entry("D", 0x0A), Map.entry("E", 0x01), Map.entry("F", 0x09),
            Map.entry("G", 0x05), Map.entry("H", 0x0D), Map.entry("I", 0x07), Map.entry("J", 0x0F),
            Map.entry("K", 0x03), Map.entry("L", 0x0B), Map.entry("M", 0x00), Map.entry("N", 0x08),
            Map.entry("O", 0x04), Map.entry("P", 0x0C));
    private static final Map<Integer, Integer> UNIT_CODES = Map.ofEntries(Map.entry(1, 0x06), Map.entry(2, 0x0E),
            Map.entry(3, 0x02), Map.entry(4, 0x0A), Map.entry(5, 0x01), Map.entry(6, 0x09), Map.entry(7, 0x05),
            Map.entry(8, 0x0D), Map.entry(9, 0x07), Map.entry(10, 0x0F), Map.entry(11, 0x03), Map.entry(12, 0x0B),
            Map.entry(13, 0x00), Map.entry(14, 0x08), Map.entry(15, 0x04), Map.entry(16, 0x0C));

    private final byte houseCode;
    private final byte unitCode;

    public X10Address(byte address) {
        this.houseCode = (byte) (address >> 4);
        this.unitCode = (byte) (address & 0x0F);
    }

    public X10Address(String house, int unit) throws IllegalArgumentException {
        this.houseCode = (byte) houseStringToCode(house);
        this.unitCode = (byte) unitIntToCode(unit);
    }

    public X10Address(String address) throws IllegalArgumentException {
        String[] parts = address.replace(".", "").split("");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid X10 address format");
        }
        this.houseCode = (byte) houseStringToCode(parts[0]);
        this.unitCode = (byte) unitStringToCode(parts[1]);
    }

    public byte getHouseCode() {
        return houseCode;
    }

    public byte getUnitCode() {
        return unitCode;
    }

    public byte getCode() {
        return (byte) (houseCode << 4 | unitCode);
    }

    @Override
    public String toString() {
        String house = houseCodeToString(houseCode);
        int unit = unitCodeToInt(unitCode);
        return house != null && unit != -1 ? house + unit : "NULL";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        X10Address other = (X10Address) obj;
        return houseCode == other.houseCode && unitCode == other.unitCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + houseCode;
        result = prime * result + unitCode;
        return result;
    }

    /**
     * Returns a house string as code
     *
     * @param house house string
     * @return house string as code if defined, otherwise throw exception
     * @throws IllegalArgumentException
     */
    public static int houseStringToCode(String house) throws IllegalArgumentException {
        int houseCode = HOUSE_CODES.getOrDefault(house, -1);
        if (houseCode == -1) {
            throw new IllegalArgumentException("Invalid X10 house code: " + house);
        }
        return houseCode;
    }

    /**
     * Returns an unit integer as code
     *
     * @param unit unit integer
     * @return unit integer as code if defined, otherwise throw exception
     * @throws IllegalArgumentException
     */
    public static int unitIntToCode(int unit) throws IllegalArgumentException {
        int unitCode = UNIT_CODES.getOrDefault(unit, -1);
        if (unitCode == -1) {
            throw new IllegalArgumentException("Invalid X10 unit code: " + unit);
        }
        return unitCode;
    }

    /**
     * Returns an unit string as code
     *
     * @param unit unit string
     * @return unit string as code if defined, otherwise throw exception
     * @throws IllegalArgumentException
     */
    public static int unitStringToCode(String unit) throws IllegalArgumentException {
        try {
            return unitIntToCode(Integer.parseInt(unit));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid X10 unit code: " + unit);
        }
    }

    /**
     * Returns a house code as string
     *
     * @param code house code
     * @return house code as string if found, otherwise null
     */
    public static @Nullable String houseCodeToString(byte code) {
        return HOUSE_CODES.entrySet().stream().filter(entry -> entry.getValue() == code).map(Entry::getKey).findFirst()
                .orElse(null);
    }

    /**
     * Returns a unit code as integer
     *
     * @param code unit code
     * @return unit code as integer if found, otherwise -1
     */
    public static int unitCodeToInt(byte code) {
        return UNIT_CODES.entrySet().stream().filter(entry -> entry.getValue() == code).map(Entry::getKey).findFirst()
                .orElse(-1);
    }

    /**
     * Returns if a house code is valid
     *
     * @param house house code
     * @return true if valid house code
     */
    public static boolean isValidHouseCode(String house) {
        return HOUSE_CODES.containsKey(house);
    }

    /**
     * Returns if a unit code is valid
     *
     * @param unit unit code
     * @return true if valid unit code
     */
    public static boolean isValidUnitCode(int unit) {
        return UNIT_CODES.containsKey(unit);
    }

    /**
     * Returns if x10 address is valid
     *
     * @return true if address is valid
     */
    public static boolean isValid(@Nullable String address) {
        if (address == null) {
            return false;
        }
        try {
            new X10Address(address);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
