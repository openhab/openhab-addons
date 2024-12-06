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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Global Location Number.
 * 
 * The <a href="https://www.gs1.org/standards/id-keys/gln">Global Location Number (GLN)</a>
 * can be used by companies to identify their locations.
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

    @Override
    public boolean equals(@Nullable Object o) {
        return o == this || (o instanceof GlobalLocationNumber other && gln.equals(other.gln));
    }

    @Override
    public int hashCode() {
        return gln.hashCode();
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
