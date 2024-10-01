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
 * Charge type for DatahubPricelist dataset.
 *
 * @see <a href="https://www.energidataservice.dk/tso-electricity/DatahubPricelist#metadata-info">
 *      https://www.energidataservice.dk/tso-electricity/DatahubPricelist#metadata-info</a>
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum ChargeType {
    Subscription("D01"),
    Fee("D02"),
    Tariff("D03");

    private final String code;

    ChargeType(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
