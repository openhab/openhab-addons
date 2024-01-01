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
package org.openhab.binding.paradoxalarm.internal.model;

/**
 * The {@link PanelType} Enum of all panel types
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public enum PanelType {
    EVO48(4, 48, 2, 16),
    EVO96(4, 96, 3, 16),
    EVO192(8, 192, 5, 16),
    EVOHD(8, 192, 5, 16),
    SP5500,
    SP6000,
    SP7000,
    MG5000,
    MG5050,
    SP4000,
    SP65,
    UNKNOWN;

    private int partitions;
    private int zones;
    private int pgms; // Programmable outputs
    private int ramPagesNumber; // Ram pages 64 bytes each

    private PanelType() {
        this(0, 0, 0, 0);
    }

    private PanelType(int numberPartitions, int numberZones, int pgms, int ramPages) {
        this.partitions = numberPartitions;
        this.zones = numberZones;
        this.pgms = pgms;
        this.ramPagesNumber = ramPages;
    }

    public static PanelType from(String panelTypeStr) {
        if (panelTypeStr == null) {
            return PanelType.UNKNOWN;
        }

        try {
            return PanelType.valueOf(panelTypeStr);
        } catch (IllegalArgumentException e) {
            return PanelType.UNKNOWN;
        }
    }

    public static boolean isBigRamEvo(PanelType panelType) {
        return panelType == EVO192 || panelType == EVOHD;
    }

    public int getPartitions() {
        return partitions;
    }

    public int getZones() {
        return zones;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public int getPgms() {
        return pgms;
    }

    public int getRamPagesNumber() {
        return ramPagesNumber;
    }
}
