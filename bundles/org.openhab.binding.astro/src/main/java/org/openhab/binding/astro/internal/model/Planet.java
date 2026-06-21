/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * Base class for planets
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public abstract class Planet {
    private @Nullable Zodiac zodiac = null;
    private Range rise = new Range();
    private Range set = new Range();

    /**
     * Returns the zodiac.
     */
    public @Nullable Zodiac getZodiac() {
        return zodiac;
    }

    /**
     * Sets the zodiac.
     */
    public void setZodiac(Zodiac zodiac) {
        this.zodiac = zodiac;
    }

    /**
     * Returns the rise range.
     */
    public Range getRise() {
        return rise;
    }

    /**
     * Sets the rise range.
     */
    public void setRise(Range rise) {
        this.rise = rise;
    }

    /**
     * Returns the set range.
     */
    public Range getSet() {
        return set;
    }

    /**
     * Sets the set range.
     */
    public void setSet(Range set) {
        this.set = set;
    }
}
