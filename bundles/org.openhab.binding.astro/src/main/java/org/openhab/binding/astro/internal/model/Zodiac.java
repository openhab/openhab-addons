/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds the sign of the zodiac.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Zodiac {
    private @Nullable ZodiacSign sign;

    public Zodiac(@Nullable ZodiacSign sign) {
        this.sign = sign;
    }

    /**
     * Returns the sign of the zodiac.
     */
    @Nullable
    public ZodiacSign getSign() {
        return sign;
    }
}
