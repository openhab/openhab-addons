/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bydbatterybox.internal.dto;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Identity and topology of the battery system.
 *
 * @param serialNumber system serial number as reported (24 characters, padded by the device)
 * @param bmuFirmwareA BMU firmware of section A, e.g. {@code 3.24}
 * @param bmuFirmwareB BMU firmware of section B
 * @param activeBmuSection {@code 'A'} or {@code 'B'}
 * @param bmsFirmware firmware of the active BMS section
 * @param activeBmsSection {@code 'A'} or {@code 'B'}
 * @param inverterType raw inverter index (the name tables of the references disagree)
 * @param towers number of towers, 1..3
 * @param modulesPerTower number of modules per tower
 * @param batteryType module type
 * @param batteryTypeCode raw module type code
 * @param application raw application code
 * @param clock BMU real-time clock, {@code null} if the reported value is not a valid date
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public record SystemInfo(String serialNumber, String bmuFirmwareA, String bmuFirmwareB, char activeBmuSection,
        String bmsFirmware, char activeBmsSection, int inverterType, int towers, int modulesPerTower,
        BatteryType batteryType, int batteryTypeCode, int application, @Nullable LocalDateTime clock) {

    /** Cell voltages of a tower that the first measurement pass returns. */
    public static final int FIRST_PASS_CELLS = 128;
    /** Cell voltages of a tower that the first and second measurement pass return together. */
    public static final int MAX_CELLS = 160;
    public static final int MAX_TEMPERATURE_SENSORS = 64;

    public String activeBmuFirmware() {
        return (activeBmuSection == 'A' ? bmuFirmwareA : bmuFirmwareB) + "-" + activeBmuSection;
    }

    public int cellsPerTower() {
        return modulesPerTower * batteryType.getCellsPerModule();
    }

    public int firstPassCellsPerTower() {
        return Math.min(FIRST_PASS_CELLS, cellsPerTower());
    }

    /**
     * @return the cells only the second measurement pass returns (cells 129-160 of an HVS tower with 5 modules)
     */
    public int secondPassCellsPerTower() {
        return Math.min(MAX_CELLS, cellsPerTower()) - firstPassCellsPerTower();
    }

    public int temperatureSensorsPerTower() {
        return Math.min(MAX_TEMPERATURE_SENSORS, modulesPerTower * batteryType.getTemperatureSensorsPerModule());
    }
}
