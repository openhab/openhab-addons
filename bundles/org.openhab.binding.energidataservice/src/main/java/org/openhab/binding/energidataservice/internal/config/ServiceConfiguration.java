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
package org.openhab.binding.energidataservice.internal.config;

import java.util.Currency;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants;
import org.openhab.binding.energidataservice.internal.api.GlobalLocationNumber;

/**
 * Configuration for the {@code service} Thing.
 *
 * @param priceArea Price area (DK1 = West of the Great Belt, DK2 = East of the Great Belt)
 * @param currencyCode Currency code for the prices
 * @param gridCompanyGLN Global Location Number of the Grid Company
 * @param energinetGLN Global Location Number of Energinet
 * @param hourlySpotPrices Recalculate spot prices to hourly average based on quarter-hourly.
 * @param reducedElectricityTax Whether reduced electricity tax applies (for electric heating customers only)
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record ServiceConfiguration(String priceArea, String currencyCode, String gridCompanyGLN, String energinetGLN,
        boolean hourlySpotPrices, boolean reducedElectricityTax) {

    private static final String DEFAULT_ENERGINET_GLN = "5790000432752";

    public static final ServiceConfiguration DEFAULT = new ServiceConfiguration("", "", "", DEFAULT_ENERGINET_GLN,
            false, false);

    public ServiceConfiguration {
        priceArea = priceArea != null ? priceArea : "";

        if (currencyCode == null || currencyCode.isBlank()) {
            currencyCode = EnergiDataServiceBindingConstants.CURRENCY_DKK.getCurrencyCode();
        }

        gridCompanyGLN = gridCompanyGLN != null ? gridCompanyGLN : "";

        if (energinetGLN == null || energinetGLN.isBlank()) {
            energinetGLN = DEFAULT_ENERGINET_GLN;
        }
    }

    /**
     * Get {@link Currency} representing the configured currency code.
     * 
     * @return Currency instance
     */
    public Currency getCurrency() {
        return Currency.getInstance(currencyCode);
    }

    public GlobalLocationNumber getGridCompanyGLN() {
        return GlobalLocationNumber.of(gridCompanyGLN);
    }

    public GlobalLocationNumber getEnerginetGLN() {
        return GlobalLocationNumber.of(energinetGLN);
    }
}
