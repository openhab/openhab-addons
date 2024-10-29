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
package org.openhab.transform.vat.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a country with VAT rates in different validity periods.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record VATCountry(String country, @JsonProperty("vatPeriod") List<VATPeriod> vatPeriod) {
    @Override
    public String toString() {
        return "CountryVAT{country='" + country + "', period=" + vatPeriod + '}';
    }
}
