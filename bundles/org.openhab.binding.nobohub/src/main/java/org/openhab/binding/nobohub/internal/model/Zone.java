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
package org.openhab.binding.nobohub.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A Zone contains one or more {@link Component}s.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public final class Zone {

    private final int id;
    private final String name;
    private int activeWeekProfileId;
    private int comfortTemperature;
    private int ecoTemperature;
    private final boolean allowOverrides;
    private @Nullable Double temperature;

    public Zone(int id, String name, int activeWeekProfileId, int comfortTemperature, int ecoTemperature,
            boolean allowOverrides) throws NoboDataException {
        this.id = id;
        this.name = name;
        this.activeWeekProfileId = activeWeekProfileId;
        this.comfortTemperature = comfortTemperature;
        this.ecoTemperature = ecoTemperature;
        this.allowOverrides = allowOverrides;
    }

    public static Zone fromH01(String h01) throws NoboDataException {
        String parts[] = h01.split(" ", 8);

        if (parts.length != 8) {
            throw new NoboDataException(
                    String.format("Unexpected number of parts from hub on H1 call: %d", parts.length));
        }

        return new Zone(Integer.parseInt(parts[1]), ModelHelper.toJavaString(parts[2]), Integer.parseInt(parts[3]),
                Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), "1".equals(parts[6]));
    }

    public String generateCommandString(final String command) {
        return String.join(" ", command, Integer.toString(id), ModelHelper.toHubString(name),
                Integer.toString(activeWeekProfileId), Integer.toString(comfortTemperature),
                Integer.toString(ecoTemperature), allowOverrides ? "1" : "0", "-1"); // "Active override id" is
                                                                                     // deprecated
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getActiveWeekProfileId() {
        return activeWeekProfileId;
    }

    public int getComfortTemperature() {
        return comfortTemperature;
    }

    public int getEcoTemperature() {
        return ecoTemperature;
    }

    public boolean getAllowOverrides() {
        return allowOverrides;
    }

    public void setTemperature(@Nullable Double temperature) {
        this.temperature = temperature;
    }

    public @Nullable Double getTemperature() {
        return temperature;
    }

    public void setComfortTemperature(int temp) {
        comfortTemperature = temp;
    }

    public void setEcoTemperature(int temp) {
        ecoTemperature = temp;
    }

    public void setWeekProfile(int weekProfileId) {
        activeWeekProfileId = weekProfileId;
    }
}
