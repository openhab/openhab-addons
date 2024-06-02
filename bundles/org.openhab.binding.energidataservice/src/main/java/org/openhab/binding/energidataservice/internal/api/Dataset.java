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
package org.openhab.binding.energidataservice.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Energi Data Service dataset.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum Dataset {
    SpotPrices("Elspotprices"),
    DatahubPricelist("DatahubPricelist"),
    CO2Emission("CO2Emis"),
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
