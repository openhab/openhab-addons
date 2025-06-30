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
package org.openhab.binding.energidataservice.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Energi Data Service dataset.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum Dataset {
    /**
     * <a href="https://www.energidataservice.dk/tso-electricity/Elspotprices">Elspot Prices</a>
     */
    SpotPrices("Elspotprices"),
    /**
     * <a href="https://energidataservice.dk/tso-electricity/DayAheadPrices">Day-Ahead Prices</a>
     */
    DayAheadPrices("DayAheadPrices"),
    /**
     * <a href="https://energidataservice.dk/tso-electricity/DatahubPricelist">Datahub Price List</a>
     */
    DatahubPricelist("DatahubPricelist"),
    /**
     * <a href="https://energidataservice.dk/tso-electricity/CO2Emis">CO2 Emission</a>
     */
    CO2Emission("CO2Emis"),
    /**
     * <a href="https://energidataservice.dk/tso-electricity/CO2EmisProg">CO2 Emission Prognosis</a>
     */
    CO2EmissionPrognosis("CO2EmisProg");

    private final String name;

    Dataset(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
