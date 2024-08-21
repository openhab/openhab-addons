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
package org.openhab.binding.astro.internal.model;

/**
 * Holds the sign of the zodiac.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Zodiac {
    private ZodiacSign sign;

    public Zodiac(ZodiacSign sign) {
        this.sign = sign;
    }

    /**
     * Returns the sign of the zodiac.
     */
    public ZodiacSign getSign() {
        return sign;
    }
}
