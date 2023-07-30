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
package org.openhab.binding.energidataservice.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Global Location Number.
 * See {@link https://www.gs1.org/standards/id-keys/gln}}
 * The Global Location Number (GLN) can be used by companies to identify their locations.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class GlobalLocationNumber {

    public static final GlobalLocationNumber EMPTY = new GlobalLocationNumber("");

    private static final int MAX_LENGTH = 13;

    private final String gln;

    public GlobalLocationNumber(String gln) {
        if (gln.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Maximum length exceeded: " + gln);
        }
        this.gln = gln;
    }

    @Override
    public String toString() {
        return gln;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isValid() {
        if (gln.length() != 13) {
            return false;
        }

        int checksum = 0;
        for (int i = 13 - 2; i >= 0; i--) {
            int digit = Character.getNumericValue(gln.charAt(i));
            checksum += (i % 2 == 0 ? digit : digit * 3);
        }
        int controlDigit = 10 - (checksum % 10);
        if (controlDigit == 10) {
            controlDigit = 0;
        }

        return controlDigit == Character.getNumericValue(gln.charAt(13 - 1));
    }

    public static GlobalLocationNumber of(String gln) {
        if (gln.isBlank()) {
            return EMPTY;
        }
        return new GlobalLocationNumber(gln);
    }
}
