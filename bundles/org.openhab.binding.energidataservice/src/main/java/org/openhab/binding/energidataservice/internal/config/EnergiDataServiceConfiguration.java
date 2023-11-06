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
package org.openhab.binding.energidataservice.internal.config;

import java.util.Currency;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants;
import org.openhab.binding.energidataservice.internal.api.GlobalLocationNumber;

/**
 * The {@link EnergiDataServiceConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class EnergiDataServiceConfiguration {

    /**
     * Price area (DK1 = West of the Great Belt, DK2 = East of the Great Belt).
     */
    public String priceArea = "";

    /**
     * Currency code for the prices.
     */
    public String currencyCode = EnergiDataServiceBindingConstants.CURRENCY_DKK.getCurrencyCode();

    /**
     * Global Location Number of the Grid Company.
     */
    public String gridCompanyGLN = "";

    /**
     * Global Location Number of Energinet.
     */
    public String energinetGLN = "5790000432752";

    /**
     * Reduced electricity tax applies.
     * For electric heating customers only.
     */
    public boolean reducedElectricityTax;

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
