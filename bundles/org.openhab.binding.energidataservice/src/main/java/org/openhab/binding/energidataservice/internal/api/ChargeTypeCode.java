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
 * Charge type code for DatahubPricelist dataset.
 *
 * These codes are defined by the individual grid companies.
 * For example, N1 uses "CD" for "Nettarif C" and "CD R" for "Rabat på nettarif N1 A/S".
 *
 * @see <a href="https://www.energidataservice.dk/tso-electricity/DatahubPricelist#metadata-info">
 *      https://www.energidataservice.dk/tso-electricity/DatahubPricelist#metadata-info</a>
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ChargeTypeCode {

    private static final int MAX_LENGTH = 20;

    private final String code;

    public ChargeTypeCode(String code) {
        if (code.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Maximum length exceeded: " + code);
        }
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    public static ChargeTypeCode of(String code) {
        return new ChargeTypeCode(code);
    }
}
