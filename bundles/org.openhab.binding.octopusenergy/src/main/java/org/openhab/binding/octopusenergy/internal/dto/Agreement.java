/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;

/**
 * The {@link Agreement} is a DTO class representing an agreement or contract.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class Agreement {
    // {
    // "tariff_code":"E-1R-SUPER-GREEN-12M-20-09-22-A",
    // "valid_from":"2020-11-01T00:00:00Z",
    // "valid_to":"2020-12-11T00:00:00Z"
    // },

    public String tariffCode = OctopusEnergyBindingConstants.UNDEFINED_STRING;
    public ZonedDateTime validFrom = OctopusEnergyBindingConstants.UNDEFINED_TIME;
    public ZonedDateTime validTo = OctopusEnergyBindingConstants.UNDEFINED_TIME;

    public Agreement(String tariffCode, ZonedDateTime validFrom, ZonedDateTime validTo) {
        super();
        this.tariffCode = tariffCode;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public String getProduct() throws RecordNotFoundException {
        String product = tariffCode;
        try {
            // find index of the char after the second hyphen
            int hyphenIndex = product.indexOf('-', product.indexOf('-') + 1) + 1;
            product = product.substring(hyphenIndex);
            hyphenIndex = product.lastIndexOf('-');
            return product.substring(0, hyphenIndex);
        } catch (IndexOutOfBoundsException e) {
            throw new RecordNotFoundException("Can't determine product from tariff - " + tariffCode, e);
        }
    }
}
