/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.types;

import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Define the different Meter manufacturer
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public enum Manufacturer {

    NotAttrib000(0x00, "Unassigned"),

    Crouzet(0x01, "CROUZET / MONETEL"),
    Sagem(0x02, "SAGEM / SAGEMCOM"),
    Schlumberger(0x03, "SCHLUMBERGER / ACTARIS / ITRON"),
    Landis(0x04, "LANDIS ET GYR / SIEMENS METERING / LANDIS+GYR"),
    Sauter(0x05, "SAUTER / STEPPER ENERGIE France / ZELLWEGER"),
    Itron(0x06, "ITRON"),
    Maec(0x07, "MAEC"),
    Matra(0x08, "MATRA-CHAUVIN ARNOUX / ENERDIS"),
    Faure(0x09, "FAURE-HERMAN"),
    Sevme(0x0A, "SEVME / SIS"),
    Magnol(0x0B, "MAGNOL / ELSTER / HONEYWELL"),
    GazThermique(0x0C, "GAZ THERMIQUE"),

    NotAttrib013(0x0D, "Unassigned"),

    Ghielmetti(0x0E, "GHIELMETTI / DIALOG E.S. / MICRONIQUE"),
    Mecelec(0x0F, "MECELEC"),

    Legrand(0x10, "LEGRAND / BACO"),
    Serd(0x11, "SERD-SCHLUMBERGER"),
    Schneider(0x12, "SCHNEIDER / MERLIN GERIN / GARDY"),
    GeneralElectri(0x13, "GENERAL ELECTRIC / POWER CONTROL"),
    NuovoPignone(0x14, "NUOVO PIGNONE / DRESSER"),
    Scle(0x15, "SCLE"),
    Edf(0x16, "EDF"),
    Gdf(0x17, "GDF / GDF-SUEZ"),
    Hager(0x18, "HAGER – GENERAL ELECTRIC"),
    DeltaCore(0x19, "DELTA-DORE"),
    Riz(0x1A, "RIZ"),
    IskraEmeco(0x1B, "ISKRAEMECO"),
    Gmt(0x1C, "GMT"),
    AnalogDevice(0x1D, "ANALOG DEVICE"),
    Michaud(0x1E, "MICHAUD"),
    HexingElectrical(0x1F, "HEXING ELECTRICAL CO. Ltd"),

    Siame(0x20, "SIAME"),
    Larsen(0x21, "LARSEN & TOUBRO Limited"),
    Elster(0x22, "ELSTER / HONEYWELL"),
    ElectronicAfzar(0x23, "ELECTRONIC AFZAR AZMA"),
    AdvancedElectronic(0x24, "ADVANCED ELECTRONIC COMPAGNY Ldt"),
    Aem(0x25, "AEM"),
    Zhejiang(0x26, "ZHEJIANG CHINT INSTRUMENT & METER CO. Ldt"),
    Ziv(0x27, "ZIV"),

    NotAttrib040(0x28, "Unassigned"),
    NotAttrib041(0x29, "Unassigned"),
    NotAttrib042(0x2A, "Unassigned"),
    NotAttrib043(0x2B, "Unassigned"),
    NotAttrib044(0x2C, "Unassigned"),
    NotAttrib045(0x2D, "Unassigned"),
    NotAttrib046(0x2E, "Unassigned"),
    NotAttrib047(0x2F, "Unassigned"),
    NotAttrib048(0x30, "Unassigned"),
    NotAttrib049(0x31, "Unassigned"),
    NotAttrib050(0x32, "Unassigned"),
    NotAttrib051(0x33, "Unassigned"),
    NotAttrib052(0x34, "Unassigned"),
    NotAttrib053(0x35, "Unassigned"),
    NotAttrib054(0x36, "Unassigned"),
    NotAttrib055(0x37, "Unassigned"),
    NotAttrib056(0x38, "Unassigned"),
    NotAttrib057(0x39, "Unassigned"),
    NotAttrib058(0x3A, "Unassigned"),
    NotAttrib059(0x3B, "Unassigned"),
    NotAttrib060(0x3C, "Unassigned"),
    NotAttrib061(0x3D, "Unassigned"),
    NotAttrib062(0x3E, "Unassigned"),
    NotAttrib063(0x3F, "Unassigned"),
    NotAttrib064(0x40, "Unassigned"),
    NotAttrib065(0x41, "Unassigned"),
    NotAttrib066(0x42, "Unassigned"),
    NotAttrib067(0x43, "Unassigned"),
    NotAttrib068(0x44, "Unassigned"),
    NotAttrib069(0x45, "Unassigned"),

    Landis02(0x46, "LANDIS et GYR (export ou régie)"),
    StepperEnergie(0x47, "STEPPER ENERGIE France"),

    NotAttrib072(0x48, "Unassigned"),
    NotAttrib073(0x49, "Unassigned"),
    NotAttrib074(0x4A, "Unassigned"),
    NotAttrib075(0x4B, "Unassigned"),
    NotAttrib076(0x4C, "Unassigned"),
    NotAttrib077(0x4D, "Unassigned"),
    NotAttrib078(0x4E, "Unassigned"),
    NotAttrib079(0x4F, "Unassigned"),
    NotAttrib080(0x50, "Unassigned"),

    Sagem02(0x51, "SAGEM / SAGEMCOM"),
    Landis03(0x52, "LANDIS ET GYR / SIEMENS METERING / LANDIS+GYR"),
    Elster02(0x53, "ELSTER / HONEYWELL"),
    Sagem03(0x54, "SAGEM / SAGEMCOM"),
    Itron02(0x55, "ITRON"),

    NotAttrib086(0x56, "Unassigned"),
    NotAttrib087(0x57, "Unassigned"),
    NotAttrib088(0x58, "Unassigned"),
    NotAttrib089(0x59, "Unassigned"),
    NotAttrib090(0x5A, "Unassigned"),
    NotAttrib091(0x5B, "Unassigned"),
    NotAttrib092(0x5C, "Unassigned"),
    NotAttrib093(0x5D, "Unassigned"),
    NotAttrib094(0x5E, "Unassigned"),
    NotAttrib095(0x5F, "Unassigned"),
    NotAttrib096(0x60, "Unassigned"),
    NotAttrib097(0x61, "Unassigned"),
    NotAttrib098(0x62, "Unassigned"),
    NotAttrib099(0x63, "Unassigned");

    private static Map<Integer, Manufacturer> idToValue = new Hashtable<Integer, Manufacturer>();

    static {
        for (Manufacturer manufacturer : EnumSet.allOf(Manufacturer.class)) {
            // Yes, use some appropriate locale in production code :)
            idToValue.put(manufacturer.id, manufacturer);
        }
    }

    private final int id;
    private final String label;

    Manufacturer(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static @Nullable Manufacturer getManufacturerForId(int id) {
        if (idToValue.containsKey(id)) {
            return idToValue.get(id);
        }

        return null;
    }
}
