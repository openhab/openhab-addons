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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Weather conditions currently preventing mowing.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class WeatherCondition {
    private @Nullable Boolean toorainy;
    private @Nullable Boolean toocold;
    private @Nullable Boolean toowarm;
    private @Nullable Boolean toodry;
    private @Nullable Boolean toowet;
    private @Nullable Boolean day;
    private @Nullable Boolean night;

    public @Nullable Boolean getTooRainy() {
        return toorainy;
    }

    public @Nullable Boolean getTooCold() {
        return toocold;
    }

    public @Nullable Boolean getTooWarm() {
        return toowarm;
    }

    public @Nullable Boolean getTooDry() {
        return toodry;
    }

    public @Nullable Boolean getTooWet() {
        return toowet;
    }

    public @Nullable Boolean getDay() {
        return day;
    }

    public @Nullable Boolean getNight() {
        return night;
    }
}
