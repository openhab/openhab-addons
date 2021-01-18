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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;

/**
 * The {@link Price} is a DTO class representing a price.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class Price {

    public static final Comparator<Price> PRICE_ORDER_ASC = new Comparator<Price>() {
        @Override
        public int compare(Price c1, Price c2) {
            return c1.valueExcVat.compareTo(c2.valueExcVat);
        }
    };

    public static final Comparator<Price> INTERVAL_START_ORDER_ASC = new Comparator<Price>() {
        @Override
        public int compare(Price c1, Price c2) {
            return c1.validFrom.compareTo(c2.validFrom);
        }
    };

    public BigDecimal valueExcVat = BigDecimal.ZERO;
    public BigDecimal valueIncVat = BigDecimal.ZERO;
    public ZonedDateTime validFrom = OctopusEnergyBindingConstants.UNDEFINED_TIME;
    public ZonedDateTime validTo = OctopusEnergyBindingConstants.UNDEFINED_TIME;

    public Price(BigDecimal valueExcVat, BigDecimal valueIncVat, ZonedDateTime validFrom, ZonedDateTime validTo) {
        super();
        this.valueExcVat = valueExcVat;
        this.valueIncVat = valueIncVat;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public Price(double valueExcVat, double valueIncVat, ZonedDateTime validFrom, ZonedDateTime validTo) {
        super();
        this.valueExcVat = BigDecimal.valueOf(valueExcVat);
        this.valueIncVat = BigDecimal.valueOf(valueIncVat);
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    @Override
    public String toString() {
        return "Price [valueExcVat=" + valueExcVat + ", valueIncVat=" + valueIncVat + ", validFrom=" + validFrom
                + ", validTo=" + validTo + "]";
    }
}
