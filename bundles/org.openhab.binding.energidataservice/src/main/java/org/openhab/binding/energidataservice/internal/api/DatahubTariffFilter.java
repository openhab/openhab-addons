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

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Filter for the DatahubPricelist dataset.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DatahubTariffFilter {

    private final Set<ChargeTypeCode> chargeTypeCodes;
    private final Set<String> notes;
    private final DateQueryParameter dateQueryParameter;

    public DatahubTariffFilter(DatahubTariffFilter filter, DateQueryParameter dateQueryParameter) {
        this(filter.chargeTypeCodes, filter.notes, dateQueryParameter);
    }

    public DatahubTariffFilter(Set<ChargeTypeCode> chargeTypeCodes, Set<String> notes) {
        this(chargeTypeCodes, notes, DateQueryParameter.EMPTY);
    }

    public DatahubTariffFilter(Set<ChargeTypeCode> chargeTypeCodes, Set<String> notes,
            DateQueryParameter dateQueryParameter) {
        this.chargeTypeCodes = chargeTypeCodes;
        this.notes = notes;
        this.dateQueryParameter = dateQueryParameter;
    }

    public Collection<String> getChargeTypeCodesAsStrings() {
        return chargeTypeCodes.stream().map(c -> c.toString()).toList();
    }

    public Collection<String> getNotes() {
        return notes;
    }

    public DateQueryParameter getDateQueryParameter() {
        return dateQueryParameter;
    }
}
